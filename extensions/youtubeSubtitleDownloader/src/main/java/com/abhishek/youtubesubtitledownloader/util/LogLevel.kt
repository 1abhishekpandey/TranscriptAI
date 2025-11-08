package com.abhishek.youtubesubtitledownloader.util

/**
 * Log levels for the subtitle downloader
 */
enum class LogLevel(val priority: Int) {
    VERBOSE(2),
    DEBUG(3),
    INFO(4),
    WARN(5),
    ERROR(6),
    NONE(7); // Disable all logging

    fun isEnabled(currentLevel: LogLevel): Boolean {
        return this.priority >= currentLevel.priority
    }
}
