package com.abhishek.transcriptai.presentation.summariser

import com.abhishek.transcriptai.domain.model.CaptionTrack
import com.abhishek.transcriptai.presentation.summariser.components.ShareOption

/**
 * Sealed class representing all possible user events in the Summariser screen
 */
sealed class SummariserUiEvent {
    /**
     * Toggle the AI summariser feature on/off
     * @param enabled Whether AI summariser should be enabled
     */
    data class ToggleAiSummariser(val enabled: Boolean) : SummariserUiEvent()

    /**
     * Select a prompt from the dropdown
     * @param promptId ID of the prompt to select
     */
    data class SelectPrompt(val promptId: String) : SummariserUiEvent()

    /**
     * User clicked the share button
     * Opens the share bottom sheet
     */
    data object ShareButtonClicked : SummariserUiEvent()

    /**
     * User selected a share option from the bottom sheet
     * @param option The selected share option (CLIPBOARD, CHATGPT, or CLAUDE)
     */
    data class ShareOptionSelected(val option: ShareOption) : SummariserUiEvent()

    /**
     * Dismiss the share bottom sheet
     */
    data object DismissShareSheet : SummariserUiEvent()

    /**
     * Navigate to the Prompt Editor screen
     */
    data object NavigateToPromptEditor : SummariserUiEvent()

    /**
     * User tapped the copy button on the transcript
     * Copies the transcript, with the AI prompt in front when it is enabled
     */
    data object CopyTranscriptClicked : SummariserUiEvent()

    /**
     * User tapped Retry after the download failed
     */
    data object RetryClicked : SummariserUiEvent()

    /**
     * User tapped the language chip
     * Opens the sheet listing the languages the video offers
     */
    data object LanguageChipClicked : SummariserUiEvent()

    /**
     * Dismiss the language sheet without picking a language
     */
    data object DismissLanguageSheet : SummariserUiEvent()

    /**
     * User picked a language from the language sheet
     * @param track The caption track to download
     */
    data class LanguageSelected(val track: CaptionTrack) : SummariserUiEvent()

    /**
     * User closed the "language isn't available" banner
     */
    data object DismissLanguageNotice : SummariserUiEvent()
}
