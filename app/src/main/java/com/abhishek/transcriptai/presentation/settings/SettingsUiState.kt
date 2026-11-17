package com.abhishek.transcriptai.presentation.settings

/**
 * UI state for the Settings screen
 *
 * @property aiPromptsSummary One line telling whether the AI prompt is on and which prompt is selected
 *           (empty until the saved settings have loaded)
 * @property clientVersion YouTube client version the downloader currently uses
 * @property showTimestamps Whether the transcript keeps the time code of each caption line
 */
data class SettingsUiState(
    val aiPromptsSummary: String = "",
    val clientVersion: String = "",
    val showTimestamps: Boolean = false
)
