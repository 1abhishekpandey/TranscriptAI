package com.abhishek.summaryai.domain.usecase

import com.abhishek.summaryai.domain.model.Result
import com.abhishek.summaryai.domain.model.SubtitleResult
import com.abhishek.summaryai.domain.repository.SubtitleRepository
import com.abhishek.summaryai.util.Logger
import javax.inject.Inject

/**
 * Use case for downloading YouTube subtitles
 * Encapsulates business logic for subtitle download operation
 */
class DownloadSubtitlesUseCase @Inject constructor(
    private val repository: SubtitleRepository
) {
    /**
     * Execute the subtitle download operation
     * @param videoUrl YouTube video URL or ID
     * @return Result containing subtitle data or error
     */
    suspend operator fun invoke(videoUrl: String): Result<SubtitleResult> {
        Logger.logI("DownloadSubtitlesUseCase: Starting subtitle download for URL: $videoUrl")

        // Validate input
        if (videoUrl.isBlank()) {
            Logger.logW("DownloadSubtitlesUseCase: Empty video URL provided")
            return Result.Error("Please enter a valid YouTube URL")
        }

        Logger.logD("DownloadSubtitlesUseCase: Calling repository to download subtitles")
        val result = repository.downloadSubtitles(videoUrl)

        when (result) {
            is Result.Success -> {
                Logger.logI("DownloadSubtitlesUseCase: Successfully downloaded subtitles")
                Logger.logV("DownloadSubtitlesUseCase: Subtitle length: ${result.data.text.length} characters")
            }
            is Result.Error -> {
                Logger.logE("DownloadSubtitlesUseCase: Error downloading subtitles: ${result.message}", result.exception)
            }
            is Result.Loading -> {
                Logger.logV("DownloadSubtitlesUseCase: Loading state")
            }
        }

        return result
    }
}
