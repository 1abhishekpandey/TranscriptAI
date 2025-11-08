package com.abhishek.transcriptai.presentation.prompteditor

sealed class PromptEditorUiEvent {
    data class LoadPromptForEdit(val promptId: String) : PromptEditorUiEvent()
    data class UpdateEditorText(val text: String) : PromptEditorUiEvent()
    object SavePrompt : PromptEditorUiEvent()
    data class DeletePrompt(val promptId: String) : PromptEditorUiEvent()
    object ConfirmDelete : PromptEditorUiEvent()
    object CancelDelete : PromptEditorUiEvent()
    object ClearEditor : PromptEditorUiEvent()
    object ConfirmClear : PromptEditorUiEvent()
    object CancelClear : PromptEditorUiEvent()
    object StartNewPrompt : PromptEditorUiEvent()
    object OnBackPressed : PromptEditorUiEvent()
    object ConfirmDiscardChanges : PromptEditorUiEvent()
    object CancelDiscardChanges : PromptEditorUiEvent()
    object SaveAndExit : PromptEditorUiEvent()
}
