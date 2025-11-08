package com.abhishek.summaryai.presentation.prompteditor

import com.abhishek.summaryai.domain.model.EditorMode
import com.abhishek.summaryai.domain.model.Prompt

data class PromptEditorUiState(
    val prompts: List<Prompt> = emptyList(),
    val editorText: String = "",
    val editorMode: EditorMode = EditorMode.New,
    val hasUnsavedChanges: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val promptToDelete: String? = null,
    val showClearConfirmation: Boolean = false,
    val showUnsavedChangesDialog: Boolean = false,
    val validationError: String? = null
)
