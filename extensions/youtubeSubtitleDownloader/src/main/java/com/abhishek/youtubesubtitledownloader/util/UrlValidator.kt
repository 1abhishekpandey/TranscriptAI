package com.abhishek.youtubesubtitledownloader.util

/**
 * Utility for validating and extracting video IDs from YouTube URLs
 */
object UrlValidator {
    // Supported YouTube URL patterns
    private val urlPatterns = listOf(
        // Standard watch URL: https://www.youtube.com/watch?v=VIDEO_ID
        """(?:https?://)?(?:www\.)?youtube\.com/watch\?(?:.*&)?v=([a-zA-Z0-9_-]{11})""".toRegex(),
        // Short URL: https://youtu.be/VIDEO_ID
        """(?:https?://)?youtu\.be/([a-zA-Z0-9_-]{11})""".toRegex(),
        // Embed URL: https://www.youtube.com/embed/VIDEO_ID
        """(?:https?://)?(?:www\.)?youtube\.com/embed/([a-zA-Z0-9_-]{11})""".toRegex(),
        // Mobile URL: https://m.youtube.com/watch?v=VIDEO_ID
        """(?:https?://)?m\.youtube\.com/watch\?(?:.*&)?v=([a-zA-Z0-9_-]{11})""".toRegex()
    )

    /**
     * Validate if the URL is a YouTube URL
     * @param url The URL to validate
     * @return true if the URL is a valid YouTube URL
     */
    fun isYouTubeUrl(url: String): Boolean {
        if (url.isBlank()) {
            SubtitleLogger.d("URL validation failed: URL is blank")
            return false
        }

        val isValid = url.contains("youtube.com") || url.contains("youtu.be")

        if (!isValid) {
            SubtitleLogger.d("URL validation failed: Not a YouTube URL - $url")
        }

        return isValid
    }

    /**
     * Extract video ID from YouTube URL
     * @param url The YouTube URL
     * @return The 11-character video ID or null if extraction fails
     */
    fun extractVideoId(url: String): String? {
        SubtitleLogger.logStep("Video ID Extraction", "Attempting to extract from: $url")

        for (pattern in urlPatterns) {
            val match = pattern.find(url)
            if (match != null && match.groupValues.size > 1) {
                val videoId = match.groupValues[1]
                SubtitleLogger.i("Video ID extracted successfully: $videoId")
                return videoId
            }
        }

        SubtitleLogger.e("Failed to extract video ID from URL: $url")
        return null
    }

    /**
     * Build full YouTube watch URL from video ID
     * @param videoId The 11-character video ID
     * @return Full YouTube watch URL
     */
    fun buildWatchUrl(videoId: String): String {
        return "https://www.youtube.com/watch?v=$videoId"
    }
}
