package com.abhishek.youtubesubtitledownloader.util

import android.util.Log

/**
 * Centralized logger for YouTube Subtitle Downloader
 * Tag: summaryaiYouTubeSubtitleDownloader
 * Default log level: VERBOSE
 */
object SubtitleLogger {
    private const val TAG = "summaryaiYouTubeSubtitleDownloader"

    @Volatile
    var logLevel: LogLevel = LogLevel.VERBOSE
        private set

    /**
     * Configure the log level
     * @param level The minimum log level to output
     */
    fun setLogLevel(level: LogLevel) {
        logLevel = level
        i("Log level set to: $level")
    }

    fun v(message: String, throwable: Throwable? = null) {
        if (LogLevel.VERBOSE.isEnabled(logLevel)) {
            if (throwable != null) {
                Log.v(TAG, message, throwable)
            } else {
                Log.v(TAG, message)
            }
        }
    }

    fun d(message: String, throwable: Throwable? = null) {
        if (LogLevel.DEBUG.isEnabled(logLevel)) {
            if (throwable != null) {
                Log.d(TAG, message, throwable)
            } else {
                Log.d(TAG, message)
            }
        }
    }

    fun i(message: String, throwable: Throwable? = null) {
        if (LogLevel.INFO.isEnabled(logLevel)) {
            if (throwable != null) {
                Log.i(TAG, message, throwable)
            } else {
                Log.i(TAG, message)
            }
        }
    }

    fun w(message: String, throwable: Throwable? = null) {
        if (LogLevel.WARN.isEnabled(logLevel)) {
            if (throwable != null) {
                Log.w(TAG, message, throwable)
            } else {
                Log.w(TAG, message)
            }
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (LogLevel.ERROR.isEnabled(logLevel)) {
            if (throwable != null) {
                Log.e(TAG, message, throwable)
            } else {
                Log.e(TAG, message)
            }
        }
    }

    /**
     * Log step information for debugging the subtitle download process
     */
    fun logStep(stepName: String, details: String) {
        d("[$stepName] $details")
    }
}
