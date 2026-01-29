package com.abhishek.transcriptai.data.repository

import com.abhishek.transcriptai.domain.model.SubtitleResult
import com.abhishek.transcriptai.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory cache repository for sharing subtitle data between screens
 * Uses singleton pattern via Hilt injection
 *
 * Purpose: Share subtitle data between Home screen and Summariser screen
 * without passing large strings through navigation arguments
 * Also stores pending URLs for auto-share mode
 *
 * Lifecycle: Scoped to Application (singleton)
 * Thread-safety: StateFlow handles concurrent access
 */
class SubtitleCacheRepository {

    private val _cachedSubtitle = MutableStateFlow<SubtitleResult?>(null)
    val cachedSubtitle: StateFlow<SubtitleResult?> = _cachedSubtitle.asStateFlow()

    private var pendingUrl: String? = null
    private var pendingAutoShare: Boolean = false

    init {
        Logger.logD("SubtitleCacheRepository: Initialized")
    }

    /**
     * Caches subtitle data for the given video ID
     *
     * @param subtitleResult The subtitle result to cache
     */
    fun cacheSubtitle(subtitleResult: SubtitleResult) {
        Logger.logI("SubtitleCacheRepository: Caching subtitle for video ${subtitleResult.videoId}")
        Logger.logD("SubtitleCacheRepository: Subtitle length: ${subtitleResult.text.length} chars, Title: ${subtitleResult.videoTitle}")
        _cachedSubtitle.value = subtitleResult
    }

    /**
     * Retrieves cached subtitle by video ID
     *
     * @param videoId The video ID to retrieve subtitle for
     * @return SubtitleResult if found and matches videoId, null otherwise
     */
    fun getCachedSubtitle(videoId: String): SubtitleResult? {
        val cached = _cachedSubtitle.value
        return if (cached?.videoId == videoId) {
            Logger.logI("SubtitleCacheRepository: Retrieved cached subtitle for video $videoId")
            cached
        } else {
            Logger.logW("SubtitleCacheRepository: No cached subtitle found for video $videoId (cached: ${cached?.videoId})")
            null
        }
    }

    /**
     * Clears the cached subtitle
     * Called when user clears content or navigates away from feature
     */
    fun clearCache() {
        Logger.logI("SubtitleCacheRepository: Clearing subtitle cache")
        _cachedSubtitle.value = null
    }

    /**
     * Gets the currently cached subtitle without video ID check
     * Useful for checking if any subtitle is cached
     *
     * @return Current cached SubtitleResult or null
     */
    fun getCurrentCache(): SubtitleResult? {
        return _cachedSubtitle.value
    }

    /**
     * Stores a pending URL for auto-share mode
     * Used when navigating from share intent to avoid URL encoding issues in navigation
     *
     * @param url The YouTube URL to download
     * @param autoShare Whether to auto-share after download
     */
    fun setPendingUrl(url: String, autoShare: Boolean) {
        Logger.logI("SubtitleCacheRepository: Setting pending URL for auto-share: $url (autoShare=$autoShare)")
        pendingUrl = url
        pendingAutoShare = autoShare
    }

    /**
     * Retrieves and clears the pending URL
     * Returns null if no pending URL is set
     *
     * @return Pair of (url, autoShare) or null
     */
    fun consumePendingUrl(): Pair<String, Boolean>? {
        val url = pendingUrl
        val autoShare = pendingAutoShare

        if (url != null) {
            Logger.logI("SubtitleCacheRepository: Consuming pending URL: $url (autoShare=$autoShare)")
            // Clear pending state
            pendingUrl = null
            pendingAutoShare = false
            return Pair(url, autoShare)
        }

        Logger.logD("SubtitleCacheRepository: No pending URL to consume")
        return null
    }
}
