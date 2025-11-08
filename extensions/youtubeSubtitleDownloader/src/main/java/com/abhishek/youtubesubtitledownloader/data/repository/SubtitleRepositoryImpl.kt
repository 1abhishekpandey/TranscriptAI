package com.abhishek.youtubesubtitledownloader.data.repository

import com.abhishek.youtubesubtitledownloader.cache.ApiKeyCache
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

/**
 * Implementation of SubtitleRepository
 * Orchestrates the complete subtitle download flow
 */
internal class SubtitleRepositoryImpl(
    private val apiService: YouTubeApiService,
    private val apiKeyCache: ApiKeyCache
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

            // Step 4: Get caption tracks
            val captionTracks = getCaptionTracks(apiKey, videoId)
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

            // Step 6: Download and parse transcript XML
            val transcriptXml = apiService.fetchTranscriptXml(selectedTrack.baseUrl)
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
     * Implements retry logic: if cached key fails, fetch fresh key
     */
    private suspend fun getOrFetchApiKey(videoId: String): String? {
        // Try cached key first
        val cachedKey = apiKeyCache.getApiKey()
        if (cachedKey != null) {
            return cachedKey
        }

        // Fetch new key
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

    /**
     * Get caption tracks using InnerTube API
     * If fails, tries to refresh API key once and retry
     */
    private suspend fun getCaptionTracks(apiKey: String, videoId: String): List<CaptionTrackDto> {
        return try {
            val response = apiService.getPlayerInfo(apiKey, videoId)
            apiService.parseCaptionTracks(response)
        } catch (e: Exception) {
            SubtitleLogger.w("Failed with current API key, attempting to refresh", e)

            // Clear cache and try with fresh key
            apiKeyCache.clear()
            val freshKey = fetchAndCacheApiKey(videoId)

            if (freshKey != null) {
                SubtitleLogger.i("Retrying with fresh API key")
                val response = apiService.getPlayerInfo(freshKey, videoId)
                apiService.parseCaptionTracks(response)
            } else {
                emptyList()
            }
        }
    }

    /**
     * Select the best caption track based on language preferences
     * Priority: specified language → en → hi → auto → first available
     */
    private fun selectCaptionTrack(
        tracks: List<CaptionTrackDto>,
        preferences: List<String>
    ): CaptionTrackDto? {
        SubtitleLogger.d("Selecting caption track from ${tracks.size} available tracks")

        // Try each preference in order
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

        // Fallback: return first available track
        val fallbackTrack = tracks.firstOrNull()
        if (fallbackTrack != null) {
            SubtitleLogger.d("No preference matched, using first available: ${fallbackTrack.languageCode}")
        }

        return fallbackTrack
    }
}
