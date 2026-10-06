package com.abhishek.transcriptai.presentation.aiprompts

/**
 * Sealed class representing the user events handled by the AI prompts ViewModel.
 * Opening the prompt editor is done by the screen itself.
 */
sealed class AiPromptsUiEvent {
    /**
     * Turn the AI prompt on or off
     * @param enabled Whether the prompt goes in front of the transcript
     */
    data class ToggleAiPrompt(val enabled: Boolean) : AiPromptsUiEvent()

    /**
     * Make a prompt the selected one
     * @param promptId ID of the prompt to select
     */
    data class SelectPrompt(val promptId: String) : AiPromptsUiEvent()

    /**
     * Ask for confirmation before deleting a prompt
     * @param promptId ID of the prompt to delete
     */
    data class DeletePromptClicked(val promptId: String) : AiPromptsUiEvent()

    /**
     * Confirm the pending delete
     */
    data object ConfirmDelete : AiPromptsUiEvent()

    /**
     * Close the delete confirmation without deleting
     */
    data object CancelDelete : AiPromptsUiEvent()

    /**
     * Ask for confirmation before replacing all prompts with the defaults
     */
    data object ResetClicked : AiPromptsUiEvent()

    /**
     * Confirm the reset to the default prompts
     */
    data object ConfirmReset : AiPromptsUiEvent()

    /**
     * Close the reset confirmation without resetting
     */
    data object CancelReset : AiPromptsUiEvent()
}
