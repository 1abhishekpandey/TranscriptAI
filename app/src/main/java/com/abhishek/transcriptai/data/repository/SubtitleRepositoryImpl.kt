package com.abhishek.transcriptai.data.repository

import com.abhishek.transcriptai.domain.model.Result
import com.abhishek.transcriptai.domain.model.SubtitleResult
import com.abhishek.transcriptai.domain.repository.SubtitleRepository
import com.abhishek.transcriptai.util.Logger
import com.abhishek.youtubesubtitledownloader.YouTubeSubtitleDownloader
import com.abhishek.youtubesubtitledownloader.domain.model.SubtitleResult as ExtensionSubtitleResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of SubtitleRepository
 * Handles data operations for subtitle downloading using YouTube Subtitle Downloader extension
 */
class SubtitleRepositoryImpl @Inject constructor(
    private val youtubeSubtitleDownloader: YouTubeSubtitleDownloader
) : SubtitleRepository {

    override suspend fun downloadSubtitles(
        videoUrl: String,
        languagePreferences: List<String>
    ): Result<SubtitleResult> = withContext(Dispatchers.IO) {
        try {
            Logger.logI("SubtitleRepositoryImpl: Starting subtitle download for: $videoUrl")
            Logger.logD("SubtitleRepositoryImpl: Language preferences: ${languagePreferences.joinToString(", ")}")

            // Extract video ID from URL for logging
            val videoId = extractVideoId(videoUrl)
            Logger.logD("SubtitleRepositoryImpl: Extracted video ID: $videoId")

            // Use YouTube Subtitle Downloader extension
            Logger.logV("SubtitleRepositoryImpl: Calling YouTube subtitle downloader extension...")
            val extensionResult = youtubeSubtitleDownloader.downloadSubtitles(
                url = videoUrl,
                languagePreferences = languagePreferences
            )

            // Map extension result to app's domain model
            when (extensionResult) {
                is ExtensionSubtitleResult.Success -> {
                    Logger.logI("SubtitleRepositoryImpl: Successfully fetched subtitles (${extensionResult.text.length} chars) in language: ${extensionResult.language}")
                    Result.Success(
                        SubtitleResult(
                            text = extensionResult.text,
                            videoId = videoId,
                            videoTitle = null // Extension doesn't provide video title
                        )
                    )
                }
                is ExtensionSubtitleResult.Error -> {
                    Logger.logE("SubtitleRepositoryImpl: Extension error: ${extensionResult.message}", extensionResult.throwable)
                    Result.Error(
                        message = "Failed to download subtitles: ${extensionResult.message}",
                        exception = extensionResult.throwable
                    )
                }
                is ExtensionSubtitleResult.Loading -> {
                    Logger.logV("SubtitleRepositoryImpl: Extension is loading...")
                    Result.Loading
                }
            }
        } catch (e: Exception) {
            Logger.logE("SubtitleRepositoryImpl: Error downloading subtitles", e)
            Result.Error("Failed to download subtitles: ${e.message}", e)
        }
    }

    /**
     * Extract video ID from YouTube URL
     * Supports formats:
     * - https://www.youtube.com/watch?v=VIDEO_ID
     * - https://youtu.be/VIDEO_ID
     * - VIDEO_ID (direct)
     */
    private fun extractVideoId(videoUrl: String): String {
        Logger.logV("SubtitleRepositoryImpl: Extracting video ID from: $videoUrl")

        return when {
            videoUrl.contains("youtube.com/watch") -> {
                videoUrl.substringAfter("v=").substringBefore("&")
            }
            videoUrl.contains("youtu.be/") -> {
                videoUrl.substringAfter("youtu.be/").substringBefore("?")
            }
            else -> videoUrl // Assume it's already a video ID
        }
    }
}
