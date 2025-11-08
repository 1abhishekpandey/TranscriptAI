package com.abhishek.youtubesubtitledownloader.domain.model

/**
 * Sealed class representing the result of a subtitle download operation
 */
sealed class SubtitleResult {
    /**
     * Successful subtitle download
     * @param text The subtitle text without timestamps
     * @param language The language code of the downloaded subtitle
     */
    data class Success(
        val text: String,
        val language: String
    ) : SubtitleResult()

    /**
     * Error during subtitle download
     * @param type The type of error that occurred
     * @param message Human-readable error message
     * @param throwable Optional throwable for debugging
     */
    data class Error(
        val type: ErrorType,
        val message: String,
        val throwable: Throwable? = null
    ) : SubtitleResult()

    /**
     * Loading state (optional, for UI progress indication)
     * @param progress Optional progress message
     */
    data class Loading(val progress: String? = null) : SubtitleResult()
}

/**
 * Types of errors that can occur during subtitle download
 */
enum class ErrorType {
    /**
     * The provided URL is not a valid YouTube URL
     */
    INVALID_URL,

    /**
     * Failed to extract video ID from URL
     */
    INVALID_VIDEO_ID,

    /**
     * Network error (timeout, no connection, etc.)
     */
    NETWORK_ERROR,

    /**
     * Failed to fetch or extract INNERTUBE_API_KEY
     */
    API_KEY_EXTRACTION_FAILED,

    /**
     * Failed to get caption tracks from InnerTube API
     */
    CAPTION_TRACKS_NOT_FOUND,

    /**
     * No subtitles available for the video
     */
    NO_SUBTITLES_AVAILABLE,

    /**
     * Requested language not available
     */
    LANGUAGE_NOT_AVAILABLE,

    /**
     * Failed to download transcript XML
     */
    TRANSCRIPT_DOWNLOAD_FAILED,

    /**
     * Failed to parse transcript XML
     */
    TRANSCRIPT_PARSE_FAILED,

    /**
     * Unknown error occurred
     */
    UNKNOWN_ERROR
}
