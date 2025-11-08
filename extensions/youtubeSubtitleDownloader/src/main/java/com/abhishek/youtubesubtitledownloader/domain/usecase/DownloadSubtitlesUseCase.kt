package com.abhishek.youtubesubtitledownloader.domain.usecase

import com.abhishek.youtubesubtitledownloader.domain.model.ErrorType
import com.abhishek.youtubesubtitledownloader.domain.model.SubtitleResult
import com.abhishek.youtubesubtitledownloader.domain.repository.SubtitleRepository
import com.abhishek.youtubesubtitledownloader.util.SubtitleLogger

/**
 * Use case for downloading YouTube subtitles
 * Encapsulates business logic and validation
 */
internal class DownloadSubtitlesUseCase(
    private val repository: SubtitleRepository
) {

    /**
     * Download subtitles from YouTube video
     * @param url The YouTube video URL (required)
     * @param languagePreferences List of preferred language codes (optional)
     *        Default: ["en", "hi", "auto"] (English → Hindi → Auto-generated)
     * @return SubtitleResult (Success with raw text, or Error)
     */
    suspend operator fun invoke(
        url: String,
        languagePreferences: List<String>? = null
    ): SubtitleResult {
        // Validate input
        if (url.isBlank()) {
            SubtitleLogger.e("Download failed: URL is blank")
            return SubtitleResult.Error(
                type = ErrorType.INVALID_URL,
                message = "URL cannot be empty"
            )
        }

        // Use default language preferences if not provided
        val preferences = languagePreferences ?: DEFAULT_LANGUAGE_PREFERENCES

        SubtitleLogger.v("Use case invoked with URL: $url")
        SubtitleLogger.v("Language preferences: $preferences")

        return repository.downloadSubtitles(url, preferences)
    }

    companion object {
        /**
         * Default language preference order:
         * 1. English (en)
         * 2. Hindi (hi)
         * 3. Auto-generated (auto)
         */
        private val DEFAULT_LANGUAGE_PREFERENCES = listOf("en", "hi", "auto")
    }
}
