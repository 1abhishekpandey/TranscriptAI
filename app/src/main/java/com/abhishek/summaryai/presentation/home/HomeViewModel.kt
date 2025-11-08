package com.abhishek.summaryai.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.summaryai.domain.model.Result
import com.abhishek.summaryai.domain.usecase.DownloadSubtitlesUseCase
import com.abhishek.summaryai.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home Screen
 * Manages UI state and handles user interactions
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val downloadSubtitlesUseCase: DownloadSubtitlesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _videoUrl = MutableStateFlow("")
    val videoUrl: StateFlow<String> = _videoUrl.asStateFlow()

    private var currentSubtitle: String = ""

    init {
        Logger.logI("HomeViewModel: Initialized")
    }

    /**
     * Handle UI events
     */
    fun onEvent(event: HomeUiEvent) {
        Logger.logD("HomeViewModel: Received event: ${event::class.simpleName}")

        when (event) {
            is HomeUiEvent.DownloadSubtitle -> {
                downloadSubtitle(event.videoUrl)
            }
            is HomeUiEvent.CopyToClipboard -> {
                copyToClipboard()
            }
            is HomeUiEvent.UpdateVideoUrl -> {
                updateVideoUrl(event.url)
            }
        }
    }

    /**
     * Download subtitles for the given video URL
     */
    private fun downloadSubtitle(videoUrl: String) {
        Logger.logI("HomeViewModel: Starting subtitle download for: $videoUrl")

        viewModelScope.launch {
            try {
                // Update to loading state with initial message
                _uiState.value = HomeUiState.Loading("Fetching video info...")
                Logger.logV("HomeViewModel: State changed to Loading - Fetching video info")

                // Simulate progress updates
                kotlinx.coroutines.delay(500)
                _uiState.value = HomeUiState.Loading("Retrieving subtitle tracks...")
                Logger.logV("HomeViewModel: State changed to Loading - Retrieving subtitle tracks")

                kotlinx.coroutines.delay(500)
                _uiState.value = HomeUiState.Loading("Parsing subtitles...")
                Logger.logV("HomeViewModel: State changed to Loading - Parsing subtitles")

                // Call use case
                val result = downloadSubtitlesUseCase(videoUrl)

                // Handle result
                when (result) {
                    is Result.Success -> {
                        currentSubtitle = result.data.text
                        _uiState.value = HomeUiState.Success(
                            subtitle = result.data.text,
                            videoTitle = result.data.videoTitle
                        )
                        Logger.logI("HomeViewModel: Successfully loaded subtitles (${result.data.text.length} chars)")
                    }
                    is Result.Error -> {
                        _uiState.value = HomeUiState.Error(result.message)
                        Logger.logE("HomeViewModel: Error loading subtitles: ${result.message}", result.exception)
                    }
                    is Result.Loading -> {
                        // Already in loading state
                        Logger.logV("HomeViewModel: Result is still loading")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("An unexpected error occurred: ${e.message}")
                Logger.logE("HomeViewModel: Unexpected error in downloadSubtitle", e)
            }
        }
    }

    /**
     * Copy current subtitle to clipboard
     * Returns the text to be copied so the UI can handle the actual clipboard operation
     */
    private fun copyToClipboard() {
        Logger.logI("HomeViewModel: Copy to clipboard requested")

        if (currentSubtitle.isNotEmpty()) {
            Logger.logD("HomeViewModel: Copying ${currentSubtitle.length} characters to clipboard")
            // The UI layer will handle the actual clipboard operation
            // This is just for logging purposes
        } else {
            Logger.logW("HomeViewModel: No subtitle available to copy")
        }
    }

    /**
     * Update the video URL input
     */
    private fun updateVideoUrl(url: String) {
        Logger.logV("HomeViewModel: Video URL updated: $url")
        _videoUrl.value = url
    }

    /**
     * Get the current subtitle text for clipboard operations
     */
    fun getCurrentSubtitle(): String {
        return currentSubtitle
    }

    override fun onCleared() {
        super.onCleared()
        Logger.logI("HomeViewModel: Cleared")
    }
}
