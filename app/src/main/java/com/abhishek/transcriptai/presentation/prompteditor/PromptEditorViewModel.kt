package com.abhishek.transcriptai.presentation.prompteditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.transcriptai.domain.model.EditorMode
import com.abhishek.transcriptai.domain.model.Prompt
import com.abhishek.transcriptai.domain.model.SummariserConfig
import com.abhishek.transcriptai.domain.repository.SummariserConfigRepository
import com.abhishek.transcriptai.domain.usecase.prompt.*
import com.abhishek.transcriptai.util.Logger
import com.abhishek.transcriptai.util.constants.SummariserConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PromptEditorViewModel @Inject constructor(
    private val getPromptsUseCase: GetPromptsUseCase,
    private val getPromptByIdUseCase: GetPromptByIdUseCase,
    private val savePromptUseCase: SavePromptUseCase,
    private val deletePromptUseCase: DeletePromptUseCase,
    private val validatePromptUseCase: ValidatePromptUseCase,
    private val configRepository: SummariserConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PromptEditorUiState())
    val uiState: StateFlow<PromptEditorUiState> = _uiState.asStateFlow()

    var onNavigateBack: () -> Unit = {}
    var onShowToast: (String) -> Unit = {}

    private var originalPromptText: String = ""

    init {
        Logger.logD("PromptEditorViewModel: Initializing")
        observePrompts()
    }

    fun loadPromptForEditIfProvided(promptId: String?) {
        if (promptId != null) {
            Logger.logI("PromptEditorViewModel: Loading prompt for edit: $promptId")
            viewModelScope.launch {
                val prompt = getPromptByIdUseCase(promptId)
                if (prompt != null) {
                    originalPromptText = prompt.text
                    _uiState.update {
                        it.copy(
                            editorText = prompt.text,
                            editorMode = EditorMode.Edit(promptId),
                            hasUnsavedChanges = false
                        )
                    }
                }
            }
        }
    }

    private fun observePrompts() {
        viewModelScope.launch {
            getPromptsUseCase()
                .catch { e ->
                    Logger.logE("PromptEditorViewModel: Error loading prompts", e)
                    _uiState.update { it.copy(error = "Failed to load prompts: ${e.message}") }
                }
                .collect { prompts ->
                    Logger.logD("PromptEditorViewModel: Loaded ${prompts.size} prompts")
                    _uiState.update { it.copy(prompts = prompts, isLoading = false) }
                }
        }
    }

    fun onEvent(event: PromptEditorUiEvent) {
        when (event) {
            is PromptEditorUiEvent.LoadPromptForEdit -> loadPromptForEdit(event.promptId)
            is PromptEditorUiEvent.UpdateEditorText -> updateEditorText(event.text)
            is PromptEditorUiEvent.SavePrompt -> savePrompt()
            is PromptEditorUiEvent.DeletePrompt -> showDeleteConfirmation(event.promptId)
            is PromptEditorUiEvent.ConfirmDelete -> confirmDelete()
            is PromptEditorUiEvent.CancelDelete -> cancelDelete()
            is PromptEditorUiEvent.ClearEditor -> requestClearEditor()
            is PromptEditorUiEvent.ConfirmClear -> confirmClear()
            is PromptEditorUiEvent.CancelClear -> cancelClear()
            is PromptEditorUiEvent.StartNewPrompt -> startNewPrompt()
            is PromptEditorUiEvent.OnBackPressed -> handleBackPress()
            is PromptEditorUiEvent.ConfirmDiscardChanges -> confirmDiscardChanges()
            is PromptEditorUiEvent.CancelDiscardChanges -> cancelDiscardChanges()
            is PromptEditorUiEvent.SaveAndExit -> saveAndExit()
        }
    }

    private fun loadPromptForEdit(promptId: String) {
        Logger.logI("PromptEditorViewModel: Loading prompt for edit: $promptId")
        viewModelScope.launch {
            val prompt = getPromptByIdUseCase(promptId)
            if (prompt != null) {
                originalPromptText = prompt.text
                _uiState.update {
                    it.copy(
                        editorText = prompt.text,
                        editorMode = EditorMode.Edit(promptId),
                        hasUnsavedChanges = false,
                        validationError = null
                    )
                }
            }
        }
    }

    private fun updateEditorText(text: String) {
        val hasChanges = when (val mode = _uiState.value.editorMode) {
            is EditorMode.New -> text.isNotEmpty()
            is EditorMode.Edit -> text != originalPromptText
        }

        _uiState.update {
            it.copy(
                editorText = text,
                hasUnsavedChanges = hasChanges,
                validationError = null
            )
        }
    }

    private fun savePrompt() {
        val currentState = _uiState.value
        val text = currentState.editorText.trim()

        Logger.logI("PromptEditorViewModel: Attempting to save prompt")

        // Validate
        val validationResult = validatePromptUseCase(
            text = text,
            existingPrompts = currentState.prompts,
            currentPromptId = (currentState.editorMode as? EditorMode.Edit)?.promptId
        )

        when (validationResult) {
            is ValidatePromptUseCase.ValidationResult.Valid -> {
                viewModelScope.launch {
                    val prompt = when (val mode = currentState.editorMode) {
                        is EditorMode.New -> {
                            Prompt(
                                id = UUID.randomUUID().toString(),
                                text = text,
                                createdAt = System.currentTimeMillis(),
                                lastModified = System.currentTimeMillis(),
                                isDefault = false
                            )
                        }
                        is EditorMode.Edit -> {
                            val existing = getPromptByIdUseCase(mode.promptId)
                            existing?.copy(
                                text = text,
                                lastModified = System.currentTimeMillis()
                            ) ?: return@launch
                        }
                    }

                    val result = savePromptUseCase(prompt)
                    if (result is com.abhishek.transcriptai.domain.model.Result.Success) {
                        Logger.logI("PromptEditorViewModel: Prompt saved successfully")
                        onShowToast(SummariserConstants.MESSAGE_PROMPT_SAVED)
                        startNewPrompt()
                    } else if (result is com.abhishek.transcriptai.domain.model.Result.Error) {
                        Logger.logE("PromptEditorViewModel: Failed to save prompt - ${result.message}")
                        _uiState.update { it.copy(validationError = result.message) }
                    }
                }
            }
            is ValidatePromptUseCase.ValidationResult.Empty -> {
                _uiState.update { it.copy(validationError = SummariserConstants.MESSAGE_PROMPT_EMPTY) }
            }
            is ValidatePromptUseCase.ValidationResult.TooShort -> {
                _uiState.update { it.copy(validationError = SummariserConstants.MESSAGE_PROMPT_TOO_SHORT) }
            }
            is ValidatePromptUseCase.ValidationResult.Duplicate -> {
                _uiState.update { it.copy(validationError = SummariserConstants.MESSAGE_PROMPT_DUPLICATE) }
            }
        }
    }

    private fun showDeleteConfirmation(promptId: String) {
        Logger.logI("PromptEditorViewModel: Showing delete confirmation for prompt $promptId")
        _uiState.update {
            it.copy(
                showDeleteConfirmation = true,
                promptToDelete = promptId
            )
        }
    }

    private fun confirmDelete() {
        val promptId = _uiState.value.promptToDelete ?: return
        Logger.logI("PromptEditorViewModel: Confirming delete for prompt $promptId")

        viewModelScope.launch {
            // Check if deleted prompt is the currently selected one
            val currentConfig = configRepository.getConfig().first()
            val isSelectedPrompt = currentConfig.selectedPromptId == promptId

            val result = deletePromptUseCase(promptId, _uiState.value.prompts)

            when (result) {
                is com.abhishek.transcriptai.domain.model.Result.Success -> {
                    Logger.logI("PromptEditorViewModel: Prompt deleted successfully")
                    onShowToast(SummariserConstants.MESSAGE_PROMPT_DELETED)

                    // If the deleted prompt was the selected one, clear selection and auto-select next
                    if (isSelectedPrompt) {
                        Logger.logI("PromptEditorViewModel: Deleted prompt was selected, clearing selection")
                        val remainingPrompts = _uiState.value.prompts.filter { it.id != promptId }
                        val nextPrompt = remainingPrompts.firstOrNull()

                        val newConfig = SummariserConfig(
                            isAiSummariserEnabled = currentConfig.isAiSummariserEnabled,
                            selectedPromptId = nextPrompt?.id
                        )
                        configRepository.updateConfig(newConfig)
                        Logger.logD("PromptEditorViewModel: Auto-selected next prompt: ${nextPrompt?.id}")
                    }

                    // If the deleted prompt was being edited, clear editor
                    if ((_uiState.value.editorMode as? EditorMode.Edit)?.promptId == promptId) {
                        startNewPrompt()
                    }
                }
                is com.abhishek.transcriptai.domain.model.Result.Error -> {
                    Logger.logE("PromptEditorViewModel: Failed to delete prompt - ${result.message}")
                    onShowToast(result.message)
                }
                else -> {}
            }

            _uiState.update {
                it.copy(
                    showDeleteConfirmation = false,
                    promptToDelete = null
                )
            }
        }
    }

    private fun cancelDelete() {
        _uiState.update {
            it.copy(
                showDeleteConfirmation = false,
                promptToDelete = null
            )
        }
    }

    private fun requestClearEditor() {
        if (_uiState.value.editorText.length > 20) {
            _uiState.update { it.copy(showClearConfirmation = true) }
        } else {
            confirmClear()
        }
    }

    private fun confirmClear() {
        Logger.logI("PromptEditorViewModel: Clearing editor")
        startNewPrompt()
        _uiState.update { it.copy(showClearConfirmation = false) }
    }

    private fun cancelClear() {
        _uiState.update { it.copy(showClearConfirmation = false) }
    }

    private fun startNewPrompt() {
        Logger.logI("PromptEditorViewModel: Starting new prompt")
        originalPromptText = ""
        _uiState.update {
            it.copy(
                editorText = "",
                editorMode = EditorMode.New,
                hasUnsavedChanges = false,
                validationError = null
            )
        }
    }

    private fun handleBackPress() {
        if (_uiState.value.hasUnsavedChanges) {
            _uiState.update { it.copy(showUnsavedChangesDialog = true) }
        } else {
            onNavigateBack()
        }
    }

    private fun confirmDiscardChanges() {
        Logger.logI("PromptEditorViewModel: Discarding changes")
        _uiState.update { it.copy(showUnsavedChangesDialog = false) }
        onNavigateBack()
    }

    private fun cancelDiscardChanges() {
        _uiState.update { it.copy(showUnsavedChangesDialog = false) }
    }

    private fun saveAndExit() {
        savePrompt()
        if (_uiState.value.validationError == null) {
            _uiState.update { it.copy(showUnsavedChangesDialog = false) }
            onNavigateBack()
        }
    }
}
