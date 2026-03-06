package com.abhishek.youtubesubtitledownloader.data.repository

import com.abhishek.youtubesubtitledownloader.cache.ApiKeyCache
import com.abhishek.youtubesubtitledownloader.cache.ClientVersionCache
import com.abhishek.youtubesubtitledownloader.data.parser.XmlSubtitleParser
import com.abhishek.youtubesubtitledownloader.data.remote.CaptionTrackDto
import com.abhishek.youtubesubtitledownloader.data.remote.YouTubeApiService
import com.abhishek.youtubesubtitledownloader.domain.model.ErrorType
import com.abhishek.youtubesubtitledownloader.domain.model.SubtitleResult
import com.abhishek.youtubesubtitledownloader.domain.repository.SubtitleRepository
import com.abhishek.youtubesubtitledownloader.util.SubtitleLogger
import com.abhishek.youtubesubtitledownloader.util.UrlValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Implementation of SubtitleRepository
 * Orchestrates the complete subtitle download flow
 */
internal class SubtitleRepositoryImpl(
    private val apiService: YouTubeApiService,
    private val apiKeyCache: ApiKeyCache,
    private val clientVersionCache: ClientVersionCache
) : SubtitleRepository {

    override suspend fun downloadSubtitles(
        youtubeUrl: String,
        languagePreferences: List<String>
    ): SubtitleResult = withContext(Dispatchers.IO) {
        SubtitleLogger.i("=== Starting subtitle download ===")
        SubtitleLogger.i("URL: $youtubeUrl")
        SubtitleLogger.i("Language preferences: $languagePreferences")

        try {
            // Step 1: Validate URL
            if (!UrlValidator.isYouTubeUrl(youtubeUrl)) {
                return@withContext SubtitleResult.Error(
                    type = ErrorType.INVALID_URL,
                    message = "Not a valid YouTube URL"
                )
            }

            // Step 2: Extract video ID
            val videoId = UrlValidator.extractVideoId(youtubeUrl)
            if (videoId == null) {
                return@withContext SubtitleResult.Error(
                    type = ErrorType.INVALID_VIDEO_ID,
                    message = "Failed to extract video ID from URL"
                )
            }

            // Step 3: Get API key (from cache or fetch new)
            val apiKey = getOrFetchApiKey(videoId) ?: return@withContext SubtitleResult.Error(
                type = ErrorType.API_KEY_EXTRACTION_FAILED,
                message = "Failed to fetch INNERTUBE_API_KEY"
            )

            // Step 4: Get caption tracks via InnerTube ANDROID client
            val captionTracks = try {
                getCaptionTracks(apiKey, videoId)
            } catch (e: ClientVersionExhaustedException) {
                return@withContext SubtitleResult.Error(
                    type = ErrorType.CLIENT_VERSION_OUTDATED,
                    message = "YouTube rejected all known client versions. The app may need an update.",
                    throwable = e
                )
            }
            if (captionTracks.isEmpty()) {
                return@withContext SubtitleResult.Error(
                    type = ErrorType.NO_SUBTITLES_AVAILABLE,
                    message = "No subtitles available for this video"
                )
            }

            // Step 5: Select best caption track based on language preferences
            val selectedTrack = selectCaptionTrack(captionTracks, languagePreferences)
                ?: return@withContext SubtitleResult.Error(
                    type = ErrorType.LANGUAGE_NOT_AVAILABLE,
                    message = "None of the preferred languages are available"
                )

            SubtitleLogger.i("Selected caption: ${selectedTrack.name.simpleText} (${selectedTrack.languageCode})")

            // Step 6: Download and parse transcript XML (strip fmt=srv3 if present)
            val transcriptUrl = selectedTrack.baseUrl.replace("&fmt=srv3", "")
            val transcriptXml = apiService.fetchTranscriptXml(transcriptUrl)
            val segments = XmlSubtitleParser.parseXml(transcriptXml)

            // Step 7: Convert to plain text (no timestamps)
            val plainText = XmlSubtitleParser.toPlainText(segments)

            SubtitleLogger.i("=== Subtitle download completed successfully ===")
            SubtitleLogger.i("Final text length: ${plainText.length} characters")

            SubtitleResult.Success(
                text = plainText,
                language = selectedTrack.languageCode
            )
        } catch (e: Exception) {
            SubtitleLogger.e("Subtitle download failed", e)
            SubtitleResult.Error(
                type = ErrorType.UNKNOWN_ERROR,
                message = e.message ?: "Unknown error occurred",
                throwable = e
            )
        }
    }

    /**
     * Get API key from cache or fetch new one
     */
    private suspend fun getOrFetchApiKey(videoId: String): String? {
        val cachedKey = apiKeyCache.getApiKey()
        if (cachedKey != null) {
            return cachedKey
        }
        return fetchAndCacheApiKey(videoId)
    }

    /**
     * Fetch API key from YouTube page and cache it
     */
    private suspend fun fetchAndCacheApiKey(videoId: String): String? {
        SubtitleLogger.i("Fetching new INNERTUBE_API_KEY from YouTube")

        return try {
            val videoUrl = UrlValidator.buildWatchUrl(videoId)
            val html = apiService.fetchVideoPage(videoUrl)
            val apiKey = apiService.extractApiKey(html)

            if (apiKey != null) {
                apiKeyCache.putApiKey(apiKey)
                SubtitleLogger.i("API key fetched and cached successfully")
                apiKey
            } else {
                SubtitleLogger.e("Failed to extract API key from page")
                null
            }
        } catch (e: Exception) {
            SubtitleLogger.e("Failed to fetch API key", e)
            null
        }
    }

    private suspend fun getCaptionTracks(apiKey: String, videoId: String): List<CaptionTrackDto> {
        val cachedVersion = clientVersionCache.getClientVersion()

        // Attempt 1: cached version + current API key
        try {
            val tracks = fetchAndParseTracks(apiKey, videoId, cachedVersion)
            if (tracks.isNotEmpty()) return tracks
        } catch (e: IOException) {
            SubtitleLogger.w("Failed with cached version $cachedVersion, refreshing API key", e)
        }

        // Attempt 2: refresh API key, same version
        apiKeyCache.clear()
        val freshKey = fetchAndCacheApiKey(videoId)
        val workingKey = freshKey ?: apiKey

        try {
            val tracks = fetchAndParseTracks(workingKey, videoId, cachedVersion)
            if (tracks.isNotEmpty()) return tracks
        } catch (e: IOException) {
            SubtitleLogger.w("Failed after API key refresh, starting version probe", e)
        }

        // Attempt 3: probe upward from current major version
        val probedTracks = probeWorkingVersion(workingKey, videoId, cachedVersion)
        if (probedTracks.isNotEmpty()) return probedTracks

        throw ClientVersionExhaustedException("All client versions exhausted for video $videoId")
    }

    private suspend fun probeWorkingVersion(
        apiKey: String,
        videoId: String,
        failedVersion: String
    ): List<CaptionTrackDto> {
        val failedMajor = ClientVersionCache.parseMajorVersion(failedVersion) ?: return emptyList()
        val minorPatch = ClientVersionCache.parseMinorPatch(failedVersion) ?: "10.38"

        SubtitleLogger.i("Starting version probe from major $failedMajor")

        for (majorOffset in 1..5) {
            val candidateVersion = ClientVersionCache.buildVersion(failedMajor + majorOffset, minorPatch)
            SubtitleLogger.i("Probing version: $candidateVersion")
            try {
                val tracks = fetchAndParseTracks(apiKey, videoId, candidateVersion)
                if (tracks.isNotEmpty()) {
                    SubtitleLogger.i("Found working version: $candidateVersion")
                    clientVersionCache.putClientVersion(candidateVersion)
                    return tracks
                }
            } catch (e: IOException) {
                val msg = e.message ?: ""
                if (msg.contains("HTTP 404")) {
                    SubtitleLogger.w("Version $candidateVersion too new (404), stopping probe")
                    break
                }
                SubtitleLogger.w("Version $candidateVersion rejected, continuing probe")
            }
        }

        SubtitleLogger.e("Version probe failed: no working version found in range ${failedMajor + 1}..${failedMajor + 5}")
        return emptyList()
    }

    internal suspend fun testVersion(videoId: String, version: String): Boolean {
        val apiKey = getOrFetchApiKey(videoId) ?: return false
        return try {
            val tracks = fetchAndParseTracks(apiKey, videoId, version)
            tracks.isNotEmpty()
        } catch (e: IOException) {
            false
        }
    }

    private suspend fun fetchAndParseTracks(
        apiKey: String,
        videoId: String,
        clientVersion: String
    ): List<CaptionTrackDto> {
        val response = apiService.getPlayerInfo(apiKey, videoId, clientVersion)
        return apiService.parseCaptionTracks(response)
    }

    /**
     * Select the best caption track based on language preferences
     */
    private fun selectCaptionTrack(
        tracks: List<CaptionTrackDto>,
        preferences: List<String>
    ): CaptionTrackDto? {
        SubtitleLogger.d("Selecting caption track from ${tracks.size} available tracks")

        for (langCode in preferences) {
            val track = when (langCode.lowercase()) {
                "auto" -> tracks.firstOrNull { it.kind == "asr" }
                else -> tracks.firstOrNull { it.languageCode.equals(langCode, ignoreCase = true) }
            }

            if (track != null) {
                SubtitleLogger.d("Matched preference '$langCode'")
                return track
            }
        }

        val fallbackTrack = tracks.firstOrNull()
        if (fallbackTrack != null) {
            SubtitleLogger.d("No preference matched, using first available: ${fallbackTrack.languageCode}")
        }

        return fallbackTrack
    }
}

private class ClientVersionExhaustedException(message: String) : IOException(message)
