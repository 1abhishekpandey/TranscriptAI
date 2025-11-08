package com.abhishek.youtubesubtitledownloader.domain.repository

import com.abhishek.youtubesubtitledownloader.domain.model.SubtitleResult

/**
 * Repository interface for subtitle operations
 * Defines the contract for downloading YouTube subtitles
 */
interface SubtitleRepository {

    /**
     * Download subtitles from a YouTube video
     * @param youtubeUrl The YouTube video URL
     * @param languagePreferences List of preferred language codes (e.g., ["en", "hi", "auto"])
     * @return SubtitleResult (Success with text, or Error)
     */
    suspend fun downloadSubtitles(
        youtubeUrl: String,
        languagePreferences: List<String>
    ): SubtitleResult
}
