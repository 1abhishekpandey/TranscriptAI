package com.abhishek.summaryai.presentation.summariser

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
     * Copy formatted text to clipboard
     * If AI enabled: copies "[prompt]\n\n[subtitle]"
     * If AI disabled: copies "[subtitle]"
     */
    data object CopyToClipboard : SummariserUiEvent()

    /**
     * Navigate to the Prompt Editor screen
     */
    data object NavigateToPromptEditor : SummariserUiEvent()

    /**
     * Toggle the expansion state of the prompt text display
     */
    data object TogglePromptExpansion : SummariserUiEvent()
}
