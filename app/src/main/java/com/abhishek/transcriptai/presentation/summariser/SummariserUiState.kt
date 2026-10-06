package com.abhishek.transcriptai.presentation.summariser

import com.abhishek.transcriptai.domain.model.Prompt
import com.abhishek.transcriptai.domain.model.SubtitleResult

/**
 * UI state for the Summariser screen
 *
 * @property subtitleResult The subtitle result data (text, video, and which language track it came from)
 * @property prompts List of available prompts
 * @property selectedPrompt Currently selected prompt for AI summarization
 * @property isAiSummariserEnabled Whether AI summariser feature is enabled
 * @property isLoading Whether the screen is in loading state
 * @property error Error message if any
 * @property showShareSheet Whether to show the share bottom sheet
 * @property shouldAutoShare Whether auto-share should be triggered (auto-download mode)
 * @property isSwitchingLanguage Whether the transcript is being downloaded again in another language
 * @property switchingLanguageLabel Name of the language being loaded, shown while switching
 * @property showLanguageSheet Whether to show the language picker sheet
 * @property languageNotice Message saying the requested language isn't available for the video;
 *           null when there is nothing to say or the user dismissed it
 */
data class SummariserUiState(
    val subtitleResult: SubtitleResult? = null,
    val prompts: List<Prompt> = emptyList(),
    val selectedPrompt: Prompt? = null,
    val isAiSummariserEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showShareSheet: Boolean = false,
    val shouldAutoShare: Boolean = false,
    val isSwitchingLanguage: Boolean = false,
    val switchingLanguageLabel: String? = null,
    val showLanguageSheet: Boolean = false,
    val languageNotice: String? = null
)
