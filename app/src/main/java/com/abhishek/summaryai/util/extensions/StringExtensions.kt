package com.abhishek.summaryai.util.extensions

/**
 * Extension functions for String manipulation in the SummaryAI application.
 *
 * Provides utility methods for string truncation, validation, formatting,
 * and YouTube URL processing.
 */

/**
 * Truncates a string to a maximum length with ellipsis.
 *
 * @param maxLength Maximum length of the string (including ellipsis)
 * @return Truncated string with "..." if it exceeds maxLength, otherwise the original string
 */
fun String.truncate(maxLength: Int): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.take(maxLength - 3) + "..."
    }
}

/**
 * Checks if a string is blank or empty after trimming.
 *
 * @return true if the string is blank or empty after trimming, false otherwise
 */
fun String.isBlankOrEmpty(): Boolean {
    return this.trim().isEmpty()
}

/**
 * Validates string length is within a range.
 *
 * The string is trimmed before checking the length.
 *
 * @param minLength Minimum allowed length (inclusive)
 * @param maxLength Maximum allowed length (inclusive)
 * @return true if the string length is within the range, false otherwise
 */
fun String.isLengthInRange(minLength: Int, maxLength: Int): Boolean {
    val trimmed = this.trim()
    return trimmed.length in minLength..maxLength
}

/**
 * Capitalizes the first letter of the string.
 *
 * @return String with first letter capitalized, or empty string if the input is empty
 */
fun String.capitalizeFirst(): String {
    return if (this.isEmpty()) {
        this
    } else {
        this.substring(0, 1).uppercase() + this.substring(1)
    }
}

/**
 * Extracts video ID from various YouTube URL formats.
 *
 * Supports the following URL patterns:
 * - https://www.youtube.com/watch?v=VIDEO_ID
 * - https://youtu.be/VIDEO_ID
 * - https://www.youtube.com/embed/VIDEO_ID
 *
 * @return Video ID or null if not found in the URL
 */
fun String.extractYouTubeVideoId(): String? {
    val patterns = listOf(
        "(?<=watch\\?v=)[^&#]+".toRegex(),
        "(?<=youtu.be/)[^&#]+".toRegex(),
        "(?<=embed/)[^&#]+".toRegex()
    )

    for (pattern in patterns) {
        val match = pattern.find(this)
        if (match != null) {
            return match.value
        }
    }

    return null
}

/**
 * Formats timestamp in milliseconds to human-readable date.
 *
 * @return Formatted date string (e.g., "Jan 15, 2024")
 */
fun Long.toFormattedDate(): String {
    val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date(this))
}

/**
 * Formats timestamp in milliseconds to human-readable time.
 *
 * @return Formatted time string (e.g., "2:30 PM")
 */
fun Long.toFormattedTime(): String {
    val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    return timeFormat.format(java.util.Date(this))
}
