package com.abhishek.youtubesubtitledownloader.cache

import android.content.Context
import android.content.SharedPreferences
import com.abhishek.youtubesubtitledownloader.util.SubtitleLogger
import java.util.concurrent.TimeUnit

/**
 * Cache for storing INNERTUBE_API_KEY with TTL (Time To Live)
 * Uses SharedPreferences for persistence across app restarts
 */
class ApiKeyCache(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val ttlMillis: Long = TimeUnit.HOURS.toMillis(DEFAULT_TTL_HOURS)

    /**
     * Get cached API key if valid (not expired)
     * @return The cached API key or null if not cached or expired
     */
    fun getApiKey(): String? {
        val key = prefs.getString(KEY_API_KEY, null)
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)

        if (key == null || timestamp == 0L) {
            SubtitleLogger.d("API key cache miss: No key stored")
            return null
        }

        val currentTime = System.currentTimeMillis()
        val age = currentTime - timestamp
        val isExpired = age > ttlMillis

        return if (isExpired) {
            SubtitleLogger.d("API key cache miss: Key expired (age: ${age / 1000 / 60} minutes)")
            clear() // Clear expired key
            null
        } else {
            val remainingHours = (ttlMillis - age) / 1000 / 60 / 60
            SubtitleLogger.d("API key cache hit: Key valid for $remainingHours more hours")
            key
        }
    }

    /**
     * Store API key with current timestamp
     * @param apiKey The INNERTUBE_API_KEY to cache
     */
    fun putApiKey(apiKey: String) {
        val timestamp = System.currentTimeMillis()
        prefs.edit()
            .putString(KEY_API_KEY, apiKey)
            .putLong(KEY_TIMESTAMP, timestamp)
            .apply()

        SubtitleLogger.d("API key cached successfully (TTL: $DEFAULT_TTL_HOURS hours)")
    }

    /**
     * Clear cached API key
     */
    fun clear() {
        prefs.edit()
            .remove(KEY_API_KEY)
            .remove(KEY_TIMESTAMP)
            .apply()

        SubtitleLogger.d("API key cache cleared")
    }

    /**
     * Check if a valid API key is cached
     */
    fun hasValidKey(): Boolean {
        return getApiKey() != null
    }

    companion object {
        private const val PREFS_NAME = "youtube_subtitle_downloader_prefs"
        private const val KEY_API_KEY = "innertube_api_key"
        private const val KEY_TIMESTAMP = "api_key_timestamp"
        private const val DEFAULT_TTL_HOURS = 24L
    }
}
