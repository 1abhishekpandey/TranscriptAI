package com.abhishek.summaryai.domain.repository

import com.abhishek.summaryai.domain.model.Result
import com.abhishek.summaryai.domain.model.SubtitleResult

/**
 * Repository interface for subtitle operations
 * No Android dependencies - pure Kotlin interface
 */
interface SubtitleRepository {
    /**
     * Download subtitles from a YouTube video URL
     * @param videoUrl The YouTube video URL or video ID
     * @param languagePreferences List of preferred language codes (e.g., ["en", "hi", "auto"])
     * @return Result containing SubtitleResult or error
     */
    suspend fun downloadSubtitles(
        videoUrl: String,
        languagePreferences: List<String> = listOf("en")
    ): Result<SubtitleResult>
}
