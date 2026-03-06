package com.abhishek.youtubesubtitledownloader.cache

import android.content.Context
import android.content.SharedPreferences
import com.abhishek.youtubesubtitledownloader.util.SubtitleLogger
import java.util.concurrent.TimeUnit

internal class ClientVersionCache(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun getClientVersion(): String {
        val version = prefs.getString(KEY_CLIENT_VERSION, null)
        val timestamp = prefs.getLong(KEY_CLIENT_VERSION_TIMESTAMP, 0L)

        if (version == null || timestamp == 0L) {
            SubtitleLogger.d("Client version cache miss: using default $DEFAULT_CLIENT_VERSION")
            return DEFAULT_CLIENT_VERSION
        }

        val ageMillis = System.currentTimeMillis() - timestamp
        val ageDays = TimeUnit.MILLISECONDS.toDays(ageMillis)
        SubtitleLogger.d("Client version cache hit: $version (age: $ageDays days)")

        return version
    }

    fun putClientVersion(version: String) {
        prefs.edit()
            .putString(KEY_CLIENT_VERSION, version)
            .putLong(KEY_CLIENT_VERSION_TIMESTAMP, System.currentTimeMillis())
            .apply()

        SubtitleLogger.d("Client version cached: $version")
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_CLIENT_VERSION)
            .remove(KEY_CLIENT_VERSION_TIMESTAMP)
            .apply()

        SubtitleLogger.d("Client version cache cleared")
    }

    companion object {
        private const val PREFS_NAME = "youtube_subtitle_downloader_prefs"
        private const val KEY_CLIENT_VERSION = "android_client_version"
        private const val KEY_CLIENT_VERSION_TIMESTAMP = "android_client_version_timestamp"
        internal const val DEFAULT_CLIENT_VERSION = "20.10.38"

        fun parseMajorVersion(version: String): Int? {
            return version.split(".").firstOrNull()?.toIntOrNull()
        }

        fun parseMinorPatch(version: String): String? {
            val parts = version.split(".", limit = 2)
            return if (parts.size >= 2) parts[1] else null
        }

        fun buildVersion(major: Int, minorPatch: String): String {
            return "$major.$minorPatch"
        }
    }
}
