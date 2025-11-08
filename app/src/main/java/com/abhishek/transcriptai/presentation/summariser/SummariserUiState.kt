package com.abhishek.transcriptai.presentation.summariser

import com.abhishek.transcriptai.domain.model.Prompt
import com.abhishek.transcriptai.domain.model.SubtitleResult

/**
 * UI state for the Summariser screen
 *
 * @property subtitleResult The subtitle result data (includes text, videoId, videoTitle)
 * @property prompts List of available prompts
 * @property selectedPrompt Currently selected prompt for AI summarization
 * @property isAiSummariserEnabled Whether AI summariser feature is enabled
 * @property isLoading Whether the screen is in loading state
 * @property error Error message if any
 * @property showShareSheet Whether to show the share bottom sheet
 */
data class SummariserUiState(
    val subtitleResult: SubtitleResult? = null,
    val prompts: List<Prompt> = emptyList(),
    val selectedPrompt: Prompt? = null,
    val isAiSummariserEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showShareSheet: Boolean = false
)
