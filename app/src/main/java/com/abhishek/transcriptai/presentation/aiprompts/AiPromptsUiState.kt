package com.abhishek.transcriptai.presentation.aiprompts

import com.abhishek.transcriptai.domain.model.Prompt

/**
 * UI state for the AI prompts screen
 *
 * @property isAiPromptEnabled Whether the selected prompt is put in front of the transcript
 * @property prompts Saved prompts, most recently used first
 * @property selectedPromptId ID of the selected prompt (null while there are no prompts)
 * @property promptIdToDelete ID of the prompt waiting for the delete confirmation (null when no dialog is open)
 * @property showResetConfirmation Whether the "reset to defaults" confirmation is open
 * @property defaultPromptCount How many default prompts a reset restores
 */
data class AiPromptsUiState(
    val isAiPromptEnabled: Boolean = true,
    val prompts: List<Prompt> = emptyList(),
    val selectedPromptId: String? = null,
    val promptIdToDelete: String? = null,
    val showResetConfirmation: Boolean = false,
    val defaultPromptCount: Int = 0
) {
    /** The last remaining prompt cannot be deleted, because the app always needs one to select */
    val canDeletePrompts: Boolean
        get() = prompts.size > 1
}
