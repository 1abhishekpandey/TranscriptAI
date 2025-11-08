package com.abhishek.summaryai.presentation.home

/**
 * UI State for Home Screen
 * Represents all possible states of the subtitle download UI
 */
sealed class HomeUiState {
    /**
     * Initial/idle state - ready for user input
     */
    data object Idle : HomeUiState()

    /**
     * Loading state with progress message
     * @param message Progress message to display (e.g., "Fetching video info...", "Parsing subtitles...")
     */
    data class Loading(val message: String) : HomeUiState()

    /**
     * Success state with subtitle data
     * @param subtitle The downloaded subtitle text
     * @param videoTitle Optional video title
     */
    data class Success(
        val subtitle: String,
        val videoTitle: String? = null
    ) : HomeUiState()

    /**
     * Error state
     * @param message User-friendly error message
     */
    data class Error(val message: String) : HomeUiState()
}

/**
 * UI Events for Home Screen
 * Represents user actions
 */
sealed class HomeUiEvent {
    /**
     * User clicked the "Extract Subtitle" button
     */
    data class DownloadSubtitle(val videoUrl: String) : HomeUiEvent()

    /**
     * User clicked the "Copy to Clipboard" button
     */
    data object CopyToClipboard : HomeUiEvent()

    /**
     * User updated the video URL input
     */
    data class UpdateVideoUrl(val url: String) : HomeUiEvent()
}
