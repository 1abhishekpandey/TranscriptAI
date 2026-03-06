package com.abhishek.youtubesubtitledownloader.data.remote

import com.abhishek.youtubesubtitledownloader.util.SubtitleLogger
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Service for making API calls to YouTube
 * Uses InnerTube ANDROID client to fetch captions
 */
internal class YouTubeApiService {

    private val client: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            SubtitleLogger.v("OkHttp: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Fetch YouTube video page HTML to extract INNERTUBE_API_KEY
     * @param videoUrl The YouTube video URL
     * @return HTML page content
     */
    suspend fun fetchVideoPage(videoUrl: String): String {
        SubtitleLogger.logStep("Fetch Video Page", "URL: $videoUrl")

        val request = Request.Builder()
            .url(videoUrl)
            .header("User-Agent", WEB_USER_AGENT)
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()

        return executeRequest(request) { response ->
            response.body?.string() ?: throw IOException("Empty response body")
        }
    }

    /**
     * Call InnerTube Player API to get caption tracks
     * Uses ANDROID client which returns caption URLs without PO token requirement
     * @param apiKey The INNERTUBE_API_KEY extracted from page
     * @param videoId The YouTube video ID
     * @return Player API response as JSON string
     */
    suspend fun getPlayerInfo(apiKey: String, videoId: String, clientVersion: String): String {
        SubtitleLogger.logStep("InnerTube API Call", "Video ID: $videoId, clientVersion: $clientVersion")

        val url = "$INNERTUBE_API_URL?key=$apiKey"

        val requestBody = JSONObject().apply {
            put("context", JSONObject().apply {
                put("client", JSONObject().apply {
                    put("clientName", "ANDROID")
                    put("clientVersion", clientVersion)
                })
            })
            put("videoId", videoId)
        }.toString()

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody(JSON_MEDIA_TYPE))
            .header("Content-Type", "application/json")
            .build()

        return executeRequest(request) { response ->
            response.body?.string() ?: throw IOException("Empty response body")
        }
    }

    /**
     * Fetch transcript XML from baseUrl
     * @param baseUrl The URL from caption track (includes signature)
     * @return Transcript XML string
     */
    suspend fun fetchTranscriptXml(baseUrl: String): String {
        SubtitleLogger.logStep("Fetch Transcript XML", "URL: ${baseUrl.take(100)}...")

        val request = Request.Builder()
            .url(baseUrl)
            .build()

        return executeRequest(request) { response ->
            val xml = response.body?.string() ?: throw IOException("Empty response body")
            SubtitleLogger.d("Received XML: ${xml.length} characters")
            xml
        }
    }

    /**
     * Execute HTTP request with error handling
     */
    private suspend fun <T> executeRequest(
        request: Request,
        parser: (okhttp3.Response) -> T
    ): T {
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code}: ${response.message}")
                }
                parser(response)
            }
        } catch (e: IOException) {
            SubtitleLogger.e("Network request failed: ${e.message}", e)
            throw e
        }
    }

    /**
     * Extract INNERTUBE_API_KEY from HTML page
     * @param html The YouTube page HTML
     * @return The API key or null if not found
     */
    fun extractApiKey(html: String): String? {
        SubtitleLogger.logStep("API Key Extraction", "Searching in HTML (${html.length} chars)")

        val regex = """"INNERTUBE_API_KEY":\s*"([a-zA-Z0-9_-]+)"""".toRegex()
        val match = regex.find(html)

        return match?.groupValues?.getOrNull(1)?.also {
            SubtitleLogger.i("INNERTUBE_API_KEY extracted: ${it.take(10)}...")
        }
    }

    /**
     * Parse caption tracks from InnerTube API response
     * @param json The JSON response from InnerTube API
     * @return List of caption tracks or empty list
     */
    fun parseCaptionTracks(json: String): List<CaptionTrackDto> {
        SubtitleLogger.logStep("Parse Caption Tracks", "Parsing JSON response")

        return try {
            val jsonObject = JSONObject(json)
            val captionTracks = jsonObject
                .optJSONObject("captions")
                ?.optJSONObject("playerCaptionsTracklistRenderer")
                ?.optJSONArray("captionTracks")

            val playability = jsonObject.optJSONObject("playabilityStatus")
            val status = playability?.optString("status")
            if (status != null && status != "OK") {
                val reason = playability.optString("reason", "Unknown reason")
                SubtitleLogger.w("playabilityStatus: $status - $reason")
                throw IOException("PlayabilityStatus: $status - $reason")
            }

            if (captionTracks == null) {
                SubtitleLogger.w("No caption tracks found in response")
                return emptyList()
            }

            val tracks = mutableListOf<CaptionTrackDto>()
            for (i in 0 until captionTracks.length()) {
                val track = captionTracks.getJSONObject(i)
                val nameText = extractNameFromTrack(track)

                tracks.add(
                    CaptionTrackDto(
                        baseUrl = track.getString("baseUrl"),
                        name = NameDto(nameText),
                        languageCode = track.getString("languageCode"),
                        kind = track.optString("kind", null),
                        isTranslatable = track.optBoolean("isTranslatable", false)
                    )
                )
            }

            SubtitleLogger.i("Found ${tracks.size} caption tracks")
            tracks.forEach { track ->
                SubtitleLogger.d("  - ${track.name.simpleText} (${track.languageCode}) ${if (track.kind == "asr") "[AUTO]" else "[MANUAL]"}")
            }

            tracks
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            SubtitleLogger.e("Failed to parse caption tracks", e)
            emptyList()
        }
    }

    /**
     * Extract name text from track JSON, handling multiple formats
     */
    private fun extractNameFromTrack(track: JSONObject): String {
        return try {
            val nameObj = track.optJSONObject("name")
            when {
                nameObj?.has("simpleText") == true -> {
                    nameObj.getString("simpleText")
                }
                nameObj?.has("runs") == true -> {
                    val runs = nameObj.getJSONArray("runs")
                    if (runs.length() > 0) {
                        runs.getJSONObject(0).optString("text", track.getString("languageCode"))
                    } else {
                        track.getString("languageCode")
                    }
                }
                else -> track.getString("languageCode")
            }
        } catch (e: Exception) {
            track.optString("languageCode", "unknown")
        }
    }

    companion object {
        private const val INNERTUBE_API_URL = "https://www.youtube.com/youtubei/v1/player"
        private const val WEB_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
