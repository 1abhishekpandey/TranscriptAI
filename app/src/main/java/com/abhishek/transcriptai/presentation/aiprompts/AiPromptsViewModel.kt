package com.abhishek.transcriptai.presentation.aiprompts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.transcriptai.domain.model.Result
import com.abhishek.transcriptai.domain.usecase.config.GetSummariserConfigUseCase
import com.abhishek.transcriptai.domain.usecase.config.ToggleAiSummariserUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.DeletePromptUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.GetPromptsUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.ResetPromptsUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.SelectPromptUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.nextPromptIdAfterDelete
import com.abhishek.transcriptai.domain.usecase.prompt.resolveSelectedPromptId
import com.abhishek.transcriptai.util.Logger
import com.abhishek.transcriptai.util.constants.SummariserConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the AI prompts screen
 * Shows the AI prompt switch and the saved prompts (selection, delete and reset to the defaults).
 * The switch and the selection are the same saved settings the transcript page uses, so both stay in sync.
 *
 * @property getPromptsUseCase Use case for retrieving the saved prompts
 * @property getSummariserConfigUseCase Use case for getting the AI prompt switch and the selection
 * @property toggleAiSummariserUseCase Use case for turning the AI prompt on or off
 * @property selectPromptUseCase Use case for making a prompt the selected one
 * @property deletePromptUseCase Use case for deleting a prompt
 * @property resetPromptsUseCase Use case for replacing all prompts with the built-in defaults
 */
@HiltViewModel
class AiPromptsViewModel @Inject constructor(
    private val getPromptsUseCase: GetPromptsUseCase,
    private val getSummariserConfigUseCase: GetSummariserConfigUseCase,
    private val toggleAiSummariserUseCase: ToggleAiSummariserUseCase,
    private val selectPromptUseCase: SelectPromptUseCase,
    private val deletePromptUseCase: DeletePromptUseCase,
    private val resetPromptsUseCase: ResetPromptsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AiPromptsUiState(defaultPromptCount = resetPromptsUseCase.defaultPromptCount())
    )
    val uiState: StateFlow<AiPromptsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<AiPromptsUiEffect>(Channel.BUFFERED)

    /** One-time actions for the screen (snackbar messages) */
    val effects: Flow<AiPromptsUiEffect> = _effects.receiveAsFlow()

    init {
        Logger.logD("AiPromptsViewModel: Initializing")
        observePromptsAndConfig()
    }

    /**
     * Handle user events from the UI
     *
     * @param event The user event to handle
     */
    fun onEvent(event: AiPromptsUiEvent) {
        when (event) {
            is AiPromptsUiEvent.ToggleAiPrompt -> toggleAiPrompt(event.enabled)
            is AiPromptsUiEvent.SelectPrompt -> selectPrompt(event.promptId)
            is AiPromptsUiEvent.DeletePromptClicked -> _uiState.update { it.copy(promptIdToDelete = event.promptId) }
            is AiPromptsUiEvent.ConfirmDelete -> confirmDelete()
            is AiPromptsUiEvent.CancelDelete -> _uiState.update { it.copy(promptIdToDelete = null) }
            is AiPromptsUiEvent.ResetClicked -> _uiState.update { it.copy(showResetConfirmation = true) }
            is AiPromptsUiEvent.ConfirmReset -> confirmReset()
            is AiPromptsUiEvent.CancelReset -> _uiState.update { it.copy(showResetConfirmation = false) }
        }
    }

    /**
     * Keep the prompt list, the selection and the AI prompt switch in step with the saved settings
     */
    private fun observePromptsAndConfig() {
        viewModelScope.launch {
            combine(getPromptsUseCase(), getSummariserConfigUseCase()) { prompts, config -> prompts to config }
                .catch { e ->
                    Logger.logE("AiPromptsViewModel: Error loading prompts or config", e)
                    _effects.trySend(AiPromptsUiEffect.ShowMessage("Couldn't load your prompts"))
                }
                .collect { (prompts, config) ->
                    Logger.logD("AiPromptsViewModel: Loaded ${prompts.size} prompts, AI enabled: ${config.isAiSummariserEnabled}")
                    _uiState.update {
                        it.copy(
                            prompts = prompts,
                            selectedPromptId = resolveSelectedPromptId(prompts, config.selectedPromptId),
                            isAiPromptEnabled = config.isAiSummariserEnabled
                        )
                    }
                }
        }
    }

    private fun toggleAiPrompt(enabled: Boolean) {
        Logger.logI("AiPromptsViewModel: Turning the AI prompt ${if (enabled) "on" else "off"}")
        viewModelScope.launch { toggleAiSummariserUseCase(enabled) }
    }

    private fun selectPrompt(promptId: String) {
        if (promptId == _uiState.value.selectedPromptId) return
        Logger.logI("AiPromptsViewModel: Selecting prompt $promptId")
        viewModelScope.launch { selectPromptUseCase(promptId) }
    }

    /**
     * Delete the prompt the dialog asked about. When it was the selected one, the next prompt takes over
     * so there is always a selection.
     */
    private fun confirmDelete() {
        val promptId = _uiState.value.promptIdToDelete ?: return
        _uiState.update { it.copy(promptIdToDelete = null) }
        Logger.logI("AiPromptsViewModel: Deleting prompt $promptId")

        viewModelScope.launch {
            val prompts = _uiState.value.prompts
            val wasSelected = _uiState.value.selectedPromptId == promptId

            when (val result = deletePromptUseCase(promptId, prompts)) {
                is Result.Success -> {
                    if (wasSelected) {
                        nextPromptIdAfterDelete(prompts, promptId)?.let { selectPromptUseCase(it) }
                    }
                    _effects.trySend(AiPromptsUiEffect.ShowMessage(SummariserConstants.MESSAGE_PROMPT_DELETED))
                }
                is Result.Error -> {
                    Logger.logE("AiPromptsViewModel: Failed to delete prompt - ${result.message}")
                    _effects.trySend(AiPromptsUiEffect.ShowMessage(result.message))
                }
                is Result.Loading -> Unit
            }
        }
    }

    /**
     * Replace all prompts with the defaults. The use case also selects the first default.
     */
    private fun confirmReset() {
        _uiState.update { it.copy(showResetConfirmation = false) }
        Logger.logI("AiPromptsViewModel: Resetting prompts to the defaults")

        viewModelScope.launch {
            when (val result = resetPromptsUseCase()) {
                is Result.Success -> {
                    Logger.logI("AiPromptsViewModel: Restored ${result.data} default prompts")
                    _effects.trySend(AiPromptsUiEffect.ShowMessage("Default prompts restored"))
                }
                is Result.Error -> {
                    Logger.logE("AiPromptsViewModel: Failed to reset prompts - ${result.message}")
                    _effects.trySend(AiPromptsUiEffect.ShowMessage("Couldn't reset the prompts"))
                }
                is Result.Loading -> Unit
            }
        }
    }
}
