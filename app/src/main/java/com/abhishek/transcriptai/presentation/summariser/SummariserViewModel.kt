package com.abhishek.transcriptai.presentation.summariser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.transcriptai.data.repository.SubtitleCacheRepository
import com.abhishek.transcriptai.domain.model.Result
import com.abhishek.transcriptai.domain.model.ShareTarget
import com.abhishek.transcriptai.domain.repository.AIShareRepository
import com.abhishek.transcriptai.domain.repository.PromptRepository
import com.abhishek.transcriptai.domain.usecase.DownloadSubtitlesUseCase
import com.abhishek.transcriptai.domain.usecase.config.GetSummariserConfigUseCase
import com.abhishek.transcriptai.domain.usecase.config.ToggleAiSummariserUseCase
import com.abhishek.transcriptai.domain.usecase.config.UpdateSummariserConfigUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.GetPromptsUseCase
import com.abhishek.transcriptai.domain.usecase.subtitle.FormatSubtitleForCopyUseCase
import com.abhishek.transcriptai.presentation.summariser.components.ShareOption
import com.abhishek.transcriptai.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Summariser screen
 * Manages subtitle display, AI prompt selection, and sharing functionality
 *
 * @property downloadSubtitlesUseCase Use case for downloading subtitles (auto-share mode)
 * @property subtitleCacheRepository Repository for accessing cached subtitle data
 * @property promptRepository Repository for managing prompts
 * @property getPromptsUseCase Use case for retrieving available prompts
 * @property getSummariserConfigUseCase Use case for getting AI summariser configuration
 * @property toggleAiSummariserUseCase Use case for toggling AI summariser on/off
 * @property updateSummariserConfigUseCase Use case for updating summariser configuration
 * @property formatSubtitleForCopyUseCase Use case for formatting subtitle with prompt before copying
 * @property aiShareRepository Repository for sharing content to AI applications
 */
@HiltViewModel
class SummariserViewModel @Inject constructor(
    private val downloadSubtitlesUseCase: DownloadSubtitlesUseCase,
    private val subtitleCacheRepository: SubtitleCacheRepository,
    private val promptRepository: PromptRepository,
    private val getPromptsUseCase: GetPromptsUseCase,
    private val getSummariserConfigUseCase: GetSummariserConfigUseCase,
    private val toggleAiSummariserUseCase: ToggleAiSummariserUseCase,
    private val updateSummariserConfigUseCase: UpdateSummariserConfigUseCase,
    private val formatSubtitleForCopyUseCase: FormatSubtitleForCopyUseCase,
    private val aiShareRepository: AIShareRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummariserUiState())
    val uiState: StateFlow<SummariserUiState> = _uiState.asStateFlow()

    var onNavigateToPromptEditor: () -> Unit = {}
    var onFinishActivity: (() -> Unit)? = null

    init {
        Logger.logD("SummariserViewModel: Initializing")
        observePrompts()
        observeConfig()
    }

    /**
     * Load subtitle data for the given video ID from cache (manual mode)
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
     * Initialize with auto-download mode
     * Downloads subtitle from URL and triggers auto-share when ready
     *
     * @param url YouTube video URL
     * @param autoShare Whether to auto-share after download
     */
    fun initializeWithAutoDownload(url: String, autoShare: Boolean) {
        Logger.logI("SummariserViewModel: Auto-downloading subtitle for URL: $url")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = downloadSubtitlesUseCase(url)) {
                is Result.Success -> {
                    Logger.logI("SummariserViewModel: Auto-download successful - videoId: ${result.data.videoId}")

                    // Cache the subtitle for potential navigation
                    subtitleCacheRepository.cacheSubtitle(result.data)

                    _uiState.update {
                        it.copy(
                            subtitleResult = result.data,
                            isLoading = false,
                            shouldAutoShare = autoShare
                        )
                    }
                }
                is Result.Error -> {
                    Logger.logE("SummariserViewModel: Auto-download failed - ${result.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is Result.Loading -> {
                    // Keep loading state
                    Logger.logV("SummariserViewModel: Download still in progress")
                }
            }
        }
    }

    /**
     * Initialize with auto-download mode from repository
     * Consumes pending URL from SubtitleCacheRepository and downloads
     */
    fun initializeWithAutoDownloadFromRepository() {
        Logger.logI("SummariserViewModel: Consuming pending URL from repository")

        val pendingData = subtitleCacheRepository.consumePendingUrl()

        if (pendingData != null) {
            val (url, autoShare) = pendingData
            Logger.logI("SummariserViewModel: Found pending URL: $url (autoShare=$autoShare)")
            initializeWithAutoDownload(url, autoShare)
        } else {
            Logger.logE("SummariserViewModel: No pending URL found in repository")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "No URL provided for download. Please try sharing again."
                )
            }
        }
    }

    /**
     * Trigger auto-share if needed
     * Called when subtitle is ready and auto-share is enabled
     */
    fun triggerAutoShareIfNeeded() {
        val currentState = _uiState.value

        if (!currentState.shouldAutoShare || currentState.subtitleResult == null) {
            return
        }

        Logger.logI("SummariserViewModel: Triggering auto-share")

        viewModelScope.launch {
            // Check if ChatGPT is installed
            val isChatGptInstalled = aiShareRepository.isAppInstalled(ShareTarget.CHATGPT)

            if (isChatGptInstalled) {
                // Auto-share to ChatGPT
                Logger.logI("SummariserViewModel: Auto-sharing to ChatGPT")
                handleShareOption(ShareOption.CHATGPT)

                // Wait for share to complete, then close app
                delay(500)
                Logger.logI("SummariserViewModel: Closing TranscriptAI app")
                onFinishActivity?.invoke()
            } else {
                // Show share options (fallback) - don't close app
                Logger.logI("SummariserViewModel: ChatGPT not installed, showing share options")
                _uiState.update { it.copy(showShareSheet = true) }
            }

            // Clear auto-share flag (only trigger once)
            _uiState.update { it.copy(shouldAutoShare = false) }
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
                                val config = com.abhishek.transcriptai.domain.model.SummariserConfig(
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
                    com.abhishek.transcriptai.domain.model.SummariserConfig(
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

            val shareTarget = when (option) {
                ShareOption.CLIPBOARD -> ShareTarget.CLIPBOARD
                ShareOption.CHATGPT -> ShareTarget.CHATGPT
                ShareOption.CLAUDE -> ShareTarget.CLAUDE
            }

            Logger.logD("SummariserViewModel: Sharing to ${shareTarget.displayName}")
            val result = aiShareRepository.shareToApp(formattedText, shareTarget)

            if (result.isSuccess) {
                Logger.logI("SummariserViewModel: Successfully shared to ${shareTarget.displayName}")
            } else {
                Logger.logE("SummariserViewModel: Failed to share to ${shareTarget.displayName}: ${result.exceptionOrNull()?.message}")
            }

            dismissShareSheet()
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
