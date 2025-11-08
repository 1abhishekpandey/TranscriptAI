package com.abhishek.youtubesubtitledownloader

import android.content.Context
import com.abhishek.youtubesubtitledownloader.cache.ApiKeyCache
import com.abhishek.youtubesubtitledownloader.data.remote.YouTubeApiService
import com.abhishek.youtubesubtitledownloader.data.repository.SubtitleRepositoryImpl
import com.abhishek.youtubesubtitledownloader.domain.model.SubtitleResult
import com.abhishek.youtubesubtitledownloader.domain.usecase.DownloadSubtitlesUseCase
import com.abhishek.youtubesubtitledownloader.util.LogLevel
import com.abhishek.youtubesubtitledownloader.util.SubtitleLogger

/**
 * YouTube Subtitle Downloader - Main Entry Point
 *
 * A self-contained library for downloading YouTube video subtitles.
 * Returns raw subtitle text without timestamps.
 *
 * Features:
 * - Automatic API key caching with TTL
 * - Language preference support (en → hi → auto)
 * - Comprehensive logging with configurable levels
 * - Clean Architecture + MVVM pattern
 *
 * Usage:
 * ```kotlin
 * val downloader = YouTubeSubtitleDownloader.getInstance(context)
 *
 * // Basic usage (defaults to English)
 * val result = downloader.downloadSubtitles("https://www.youtube.com/watch?v=VIDEO_ID")
 *
 * // With language preferences
 * val result = downloader.downloadSubtitles(
 *     url = "https://www.youtube.com/watch?v=VIDEO_ID",
 *     languagePreferences = listOf("hi", "en", "auto")
 * )
 *
 * when (result) {
 *     is SubtitleResult.Success -> println(result.text)
 *     is SubtitleResult.Error -> println("Error: ${result.message}")
 *     is SubtitleResult.Loading -> println("Loading...")
 * }
 * ```
 */
class YouTubeSubtitleDownloader private constructor(context: Context) {

    private val apiKeyCache: ApiKeyCache = ApiKeyCache(context.applicationContext)
    private val apiService: YouTubeApiService = YouTubeApiService()
    private val repository: SubtitleRepositoryImpl = SubtitleRepositoryImpl(apiService, apiKeyCache)
    private val downloadUseCase: DownloadSubtitlesUseCase = DownloadSubtitlesUseCase(repository)

    init {
        SubtitleLogger.i("YouTubeSubtitleDownloader initialized")
    }

    /**
     * Download subtitles from a YouTube video
     *
     * @param url YouTube video URL (required)
     *            Supported formats:
     *            - https://www.youtube.com/watch?v=VIDEO_ID
     *            - https://youtu.be/VIDEO_ID
     *            - https://www.youtube.com/embed/VIDEO_ID
     *            - https://m.youtube.com/watch?v=VIDEO_ID
     *
     * @param languagePreferences List of preferred language codes (optional)
     *            Default: ["en", "hi", "auto"]
     *            - Use ISO 639-1 codes (e.g., "en", "es", "fr", "hi")
     *            - Use "auto" for auto-generated subtitles
     *            - First available language in the list will be used
     *
     * @return SubtitleResult
     *         - Success: Contains raw subtitle text (no timestamps) and language code
     *         - Error: Contains error type, message, and optional throwable
     *         - Loading: Indicates operation in progress (rarely used in this sync API)
     *
     * Example:
     * ```kotlin
     * // English subtitles (default)
     * val result = downloader.downloadSubtitles("https://www.youtube.com/watch?v=abc123")
     *
     * // Hindi, then English fallback
     * val result = downloader.downloadSubtitles(
     *     url = "https://youtu.be/abc123",
     *     languagePreferences = listOf("hi", "en")
     * )
     *
     * // Auto-generated only
     * val result = downloader.downloadSubtitles(
     *     url = "https://www.youtube.com/watch?v=abc123",
     *     languagePreferences = listOf("auto")
     * )
     * ```
     */
    suspend fun downloadSubtitles(
        url: String,
        languagePreferences: List<String>? = null
    ): SubtitleResult {
        return downloadUseCase(url, languagePreferences)
    }

    /**
     * Clear the cached API key
     * Useful if you want to force a fresh API key fetch
     */
    fun clearCache() {
        apiKeyCache.clear()
        SubtitleLogger.i("Cache cleared by user")
    }

    /**
     * Check if a valid API key is cached
     * @return true if a valid (non-expired) API key is cached
     */
    fun hasValidCachedKey(): Boolean {
        return apiKeyCache.hasValidKey()
    }

    companion object {
        @Volatile
        private var instance: YouTubeSubtitleDownloader? = null

        /**
         * Get singleton instance of YouTubeSubtitleDownloader
         * @param context Android context (application context will be used)
         * @return Singleton instance
         */
        fun getInstance(context: Context): YouTubeSubtitleDownloader {
            return instance ?: synchronized(this) {
                instance ?: YouTubeSubtitleDownloader(context).also { instance = it }
            }
        }

        /**
         * Configure log level for the library
         * Default: VERBOSE
         *
         * @param level The minimum log level to output
         *
         * Example:
         * ```kotlin
         * // Show only errors
         * YouTubeSubtitleDownloader.setLogLevel(LogLevel.ERROR)
         *
         * // Show everything (default)
         * YouTubeSubtitleDownloader.setLogLevel(LogLevel.VERBOSE)
         *
         * // Disable logging
         * YouTubeSubtitleDownloader.setLogLevel(LogLevel.NONE)
         * ```
         */
        fun setLogLevel(level: LogLevel) {
            SubtitleLogger.setLogLevel(level)
        }
    }
}
