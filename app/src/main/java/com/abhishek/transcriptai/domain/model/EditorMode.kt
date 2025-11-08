package com.abhishek.transcriptai.domain.model

/**
 * Sealed class representing the mode of the prompt editor
 */
sealed class EditorMode {
    /**
     * Creating a new prompt
     */
    data object New : EditorMode()

    /**
     * Editing an existing prompt
     * @property promptId The ID of the prompt being edited
     */
    data class Edit(val promptId: String) : EditorMode()
}
