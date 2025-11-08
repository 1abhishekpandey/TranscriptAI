package com.abhishek.summaryai.presentation.summariser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.summaryai.data.repository.SubtitleCacheRepository
import com.abhishek.summaryai.domain.repository.PromptRepository
import com.abhishek.summaryai.domain.usecase.config.GetSummariserConfigUseCase
import com.abhishek.summaryai.domain.usecase.config.ToggleAiSummariserUseCase
import com.abhishek.summaryai.domain.usecase.config.UpdateSummariserConfigUseCase
import com.abhishek.summaryai.domain.usecase.prompt.GetPromptsUseCase
import com.abhishek.summaryai.domain.usecase.subtitle.FormatSubtitleForCopyUseCase
import com.abhishek.summaryai.presentation.summariser.components.ShareOption
import com.abhishek.summaryai.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Summariser screen
 * Manages subtitle display, AI prompt selection, and copy functionality
 *
 * @property subtitleCacheRepository Repository for accessing cached subtitle data
 * @property getPromptsUseCase Use case for retrieving available prompts
 * @property getSummariserConfigUseCase Use case for getting AI summariser configuration
 * @property toggleAiSummariserUseCase Use case for toggling AI summariser on/off
 * @property updateSummariserConfigUseCase Use case for updating summariser configuration
 * @property formatSubtitleForCopyUseCase Use case for formatting subtitle with prompt before copying
 */
@HiltViewModel
class SummariserViewModel @Inject constructor(
    private val subtitleCacheRepository: SubtitleCacheRepository,
    private val promptRepository: PromptRepository,
    private val getPromptsUseCase: GetPromptsUseCase,
    private val getSummariserConfigUseCase: GetSummariserConfigUseCase,
    private val toggleAiSummariserUseCase: ToggleAiSummariserUseCase,
    private val updateSummariserConfigUseCase: UpdateSummariserConfigUseCase,
    private val formatSubtitleForCopyUseCase: FormatSubtitleForCopyUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummariserUiState())
    val uiState: StateFlow<SummariserUiState> = _uiState.asStateFlow()

    var onNavigateToPromptEditor: () -> Unit = {}
    var onCopyToClipboard: (String) -> Unit = {}
    var onShareToApp: (String, String) -> Unit = { _, _ -> } // (text, packageName)

    init {
        Logger.logD("SummariserViewModel: Initializing")
        observePrompts()
        observeConfig()
    }

    /**
     * Load subtitle data for the given video ID from cache
     *
     * @param videoId The YouTube video ID
     */
    fun loadSubtitle(videoId: String) {
        Logger.logI("SummariserViewModel: Loading subtitle for video $videoId")
        val cached = subtitleCacheRepository.getCachedSubtitle(videoId)

        if (cached != null) {
            Logger.logD("SummariserViewModel: Found cached subtitle - length: ${cached.text.length}, title: ${cached.videoTitle}")
            _uiState.update { it.copy(subtitleResult = cached, isLoading = false) }
        } else {
            Logger.logE("SummariserViewModel: No cached subtitle found for video $videoId")
            _uiState.update {
                it.copy(
                    error = "Subtitle not found. Please go back and download again.",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Observe prompts from repository and update UI state
     */
    private fun observePrompts() {
        viewModelScope.launch {
            getPromptsUseCase()
                .catch { e ->
                    Logger.logE("SummariserViewModel: Error loading prompts", e)
                    _uiState.update { it.copy(error = "Failed to load prompts: ${e.message}") }
                }
                .collect { prompts ->
                    Logger.logD("SummariserViewModel: Loaded ${prompts.size} prompts")
                    _uiState.update { currentState ->
                        val selectedPrompt = currentState.selectedPrompt
                            ?: prompts.firstOrNull()

                        // Auto-save first prompt to config if none is selected
                        if (currentState.selectedPrompt == null && selectedPrompt != null) {
                            Logger.logI("SummariserViewModel: Auto-selecting first prompt: ${selectedPrompt.id}")
                            viewModelScope.launch {
                                val config = com.abhishek.summaryai.domain.model.SummariserConfig(
                                    isAiSummariserEnabled = currentState.isAiSummariserEnabled,
                                    selectedPromptId = selectedPrompt.id
                                )
                                updateSummariserConfigUseCase(config)
                            }
                        }

                        currentState.copy(
                            prompts = prompts,
                            selectedPrompt = selectedPrompt
                        )
                    }
                }
        }
    }

    /**
     * Observe summariser configuration and update UI state
     */
    private fun observeConfig() {
        viewModelScope.launch {
            getSummariserConfigUseCase()
                .catch { e ->
                    Logger.logE("SummariserViewModel: Error loading config", e)
                }
                .collect { config ->
                    Logger.logD("SummariserViewModel: Config loaded - AI enabled: ${config.isAiSummariserEnabled}, selectedPromptId: ${config.selectedPromptId}")

                    val selectedPrompt = if (config.selectedPromptId != null) {
                        _uiState.value.prompts.find { it.id == config.selectedPromptId }
                    } else {
                        _uiState.value.prompts.firstOrNull()
                    }

                    _uiState.update {
                        it.copy(
                            isAiSummariserEnabled = config.isAiSummariserEnabled,
                            selectedPrompt = selectedPrompt
                        )
                    }
                }
        }
    }

    /**
     * Handle user events from the UI
     *
     * @param event The user event to handle
     */
    fun onEvent(event: SummariserUiEvent) {
        when (event) {
            is SummariserUiEvent.ToggleAiSummariser -> toggleAiSummariser(event.enabled)
            is SummariserUiEvent.SelectPrompt -> selectPrompt(event.promptId)
            is SummariserUiEvent.ShareButtonClicked -> showShareSheet()
            is SummariserUiEvent.ShareOptionSelected -> handleShareOption(event.option)
            is SummariserUiEvent.DismissShareSheet -> dismissShareSheet()
            is SummariserUiEvent.NavigateToPromptEditor -> navigateToPromptEditor()
        }
    }

    /**
     * Toggle the AI summariser feature on/off
     *
     * @param enabled Whether to enable AI summariser
     */
    private fun toggleAiSummariser(enabled: Boolean) {
        Logger.logI("SummariserViewModel: Toggling AI Summariser to $enabled")
        viewModelScope.launch {
            toggleAiSummariserUseCase(enabled)
        }
    }

    /**
     * Select a prompt by ID and update configuration
     *
     * @param promptId ID of the prompt to select
     */
    private fun selectPrompt(promptId: String) {
        Logger.logI("SummariserViewModel: Selecting prompt $promptId")
        val prompt = _uiState.value.prompts.find { it.id == promptId }

        if (prompt != null) {
            _uiState.update { it.copy(selectedPrompt = prompt) }

            viewModelScope.launch {
                // Update the last selected timestamp
                val timestamp = System.currentTimeMillis()
                promptRepository.updateLastSelectedTimestamp(promptId, timestamp)
                Logger.logD("SummariserViewModel: Updated lastSelectedAt timestamp for prompt $promptId")

                // Update config with selected prompt
                val config = _uiState.value.run {
                    com.abhishek.summaryai.domain.model.SummariserConfig(
                        isAiSummariserEnabled = isAiSummariserEnabled,
                        selectedPromptId = promptId
                    )
                }
                updateSummariserConfigUseCase(config)
                Logger.logD("SummariserViewModel: Updated config with new prompt")
            }
        } else {
            Logger.logW("SummariserViewModel: Prompt $promptId not found in available prompts")
        }
    }

    /**
     * Show the share bottom sheet
     */
    private fun showShareSheet() {
        Logger.logI("SummariserViewModel: Showing share sheet")
        _uiState.update { it.copy(showShareSheet = true) }
    }

    /**
     * Dismiss the share bottom sheet
     */
    private fun dismissShareSheet() {
        Logger.logI("SummariserViewModel: Dismissing share sheet")
        _uiState.update { it.copy(showShareSheet = false) }
    }

    /**
     * Handle the selected share option from the bottom sheet
     *
     * @param option The selected share option
     */
    private fun handleShareOption(option: ShareOption) {
        Logger.logI("SummariserViewModel: Handling share option: $option")
        val subtitle = _uiState.value.subtitleResult?.text

        if (subtitle.isNullOrEmpty()) {
            Logger.logW("SummariserViewModel: Cannot share - subtitle is empty")
            return
        }

        viewModelScope.launch {
            val formattedText = formatSubtitleForCopyUseCase(subtitle)
            Logger.logI("SummariserViewModel: Formatted text length: ${formattedText.length}")

            when (option) {
                ShareOption.CLIPBOARD -> {
                    Logger.logD("SummariserViewModel: Copying to clipboard")
                    onCopyToClipboard(formattedText)
                }
                ShareOption.CHATGPT -> {
                    Logger.logD("SummariserViewModel: Sharing to ChatGPT")
                    onShareToApp(formattedText, "com.openai.chatgpt")
                }
                ShareOption.CLAUDE -> {
                    Logger.logD("SummariserViewModel: Sharing to Claude")
                    onShareToApp(formattedText, "com.anthropic.claude")
                }
            }
        }
    }

    /**
     * Navigate to the Prompt Editor screen
     */
    private fun navigateToPromptEditor() {
        Logger.logI("SummariserViewModel: Navigating to Prompt Editor")
        onNavigateToPromptEditor()
    }
}
