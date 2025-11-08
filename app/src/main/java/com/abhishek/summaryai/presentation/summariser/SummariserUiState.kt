package com.abhishek.summaryai.presentation.summariser

import com.abhishek.summaryai.domain.model.Prompt
import com.abhishek.summaryai.domain.model.SubtitleResult

/**
 * UI state for the Summariser screen
 *
 * @property subtitleResult The subtitle result data (includes text, videoId, videoTitle)
 * @property prompts List of available prompts
 * @property selectedPrompt Currently selected prompt for AI summarization
 * @property isAiSummariserEnabled Whether AI summariser feature is enabled
 * @property isLoading Whether the screen is in loading state
 * @property error Error message if any
 */
data class SummariserUiState(
    val subtitleResult: SubtitleResult? = null,
    val prompts: List<Prompt> = emptyList(),
    val selectedPrompt: Prompt? = null,
    val isAiSummariserEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)
