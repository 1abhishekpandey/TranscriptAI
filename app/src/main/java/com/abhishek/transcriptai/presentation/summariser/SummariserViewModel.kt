package com.abhishek.transcriptai.presentation.summariser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.transcriptai.data.repository.SubtitleCacheRepository
import com.abhishek.transcriptai.domain.model.CaptionTrack
import com.abhishek.transcriptai.domain.model.Result
import com.abhishek.transcriptai.domain.model.ShareTarget
import com.abhishek.transcriptai.domain.model.SubtitleResult
import com.abhishek.transcriptai.domain.repository.AIShareRepository
import com.abhishek.transcriptai.domain.repository.AutoShareConfigRepository
import com.abhishek.transcriptai.domain.repository.LanguagePreferenceRepository
import com.abhishek.transcriptai.domain.repository.PromptRepository
import com.abhishek.transcriptai.domain.usecase.DownloadSubtitlesUseCase
import com.abhishek.transcriptai.domain.usecase.config.GetSummariserConfigUseCase
import com.abhishek.transcriptai.domain.usecase.config.ToggleAiSummariserUseCase
import com.abhishek.transcriptai.domain.usecase.config.UpdateSummariserConfigUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.GetPromptsUseCase
import com.abhishek.transcriptai.domain.usecase.subtitle.FormatSubtitleForCopyUseCase
import com.abhishek.transcriptai.presentation.summariser.components.ShareOption
import com.abhishek.transcriptai.util.Logger
import com.abhishek.transcriptai.util.constants.SummariserConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Summariser screen
 * Manages subtitle display, per-video language switching, AI prompt selection, and sharing functionality
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
 * @property autoShareConfigRepository Repository for managing auto-share configuration
 * @property languagePreferenceRepository Repository for the saved language choice (read only here)
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
    private val aiShareRepository: AIShareRepository,
    private val autoShareConfigRepository: AutoShareConfigRepository,
    private val languagePreferenceRepository: LanguagePreferenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummariserUiState())
    val uiState: StateFlow<SummariserUiState> = _uiState.asStateFlow()

    private val _effects = Channel<SummariserUiEffect>(Channel.BUFFERED)

    /** One-time actions for the screen (snackbar messages, clipboard writes) */
    val effects: Flow<SummariserUiEffect> = _effects.receiveAsFlow()

    /** URL and auto-share flag of the last download started here, kept so Retry can run it again */
    private var lastAutoDownload: Pair<String, Boolean>? = null

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

        // The screen asks again when it comes back from the prompt editor; keep what is already shown
        if (_uiState.value.subtitleResult?.videoId == videoId) {
            Logger.logD("SummariserViewModel: Subtitle for video $videoId is already loaded")
            return
        }

        val cached = subtitleCacheRepository.getCachedSubtitle(videoId)

        if (cached != null) {
            Logger.logD("SummariserViewModel: Found cached subtitle - length: ${cached.text.length}, title: ${cached.videoTitle}")
            _uiState.update { it.copy(subtitleResult = cached, isLoading = false, languageNotice = null) }

            if (!cached.requestedLanguageMatched) {
                viewModelScope.launch {
                    val requestedLanguage = languagePreferenceRepository.getLanguageChoice().requestedLanguageName()
                    val notice = languageUnavailableNotice(requestedLanguage, cached)
                    _uiState.update { state ->
                        if (state.subtitleResult == cached) state.copy(languageNotice = notice) else state
                    }
                }
            }
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
        lastAutoDownload = url to autoShare

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val languageChoice = languagePreferenceRepository.getLanguageChoice()
            val languagePreferences = languageChoice.toLanguagePreferences()
            when (val result = downloadSubtitlesUseCase(url, languagePreferences)) {
                is Result.Success -> {
                    Logger.logI("SummariserViewModel: Auto-download successful - videoId: ${result.data.videoId}")

                    // Cache the subtitle for potential navigation
                    subtitleCacheRepository.cacheSubtitle(result.data)

                    _uiState.update {
                        it.copy(
                            subtitleResult = result.data,
                            isLoading = false,
                            shouldAutoShare = autoShare,
                            languageNotice = noticeFor(result.data, languageChoice.requestedLanguageName())
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
        // The screen asks again when it comes back from the prompt editor; the pending URL is already used up
        val current = _uiState.value
        if (lastAutoDownload != null || current.subtitleResult != null || current.isLoading) {
            Logger.logD("SummariserViewModel: Auto-download already started, skipping")
            return
        }

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
     * Run the failed download again
     * Uses the URL of the last download; falls back to the pending URL when none was started yet
     */
    fun retry() {
        val last = lastAutoDownload
        if (last != null) {
            Logger.logI("SummariserViewModel: Retrying download for URL: ${last.first}")
            initializeWithAutoDownload(last.first, last.second)
        } else {
            initializeWithAutoDownloadFromRepository()
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
            // Get selected app from user preferences
            val selectedApp = autoShareConfigRepository.getSelectedApp()
            Logger.logI("SummariserViewModel: Selected auto-share app: $selectedApp")

            // Map selected app string to ShareTarget
            val targetApp = when (selectedApp) {
                "claude" -> ShareTarget.CLAUDE
                else -> ShareTarget.CHATGPT // Default to ChatGPT for "chatgpt" or any other value
            }

            // Check if selected app is installed
            val isAppInstalled = aiShareRepository.isAppInstalled(targetApp)

            if (isAppInstalled) {
                // Auto-share to selected app
                Logger.logI("SummariserViewModel: Auto-sharing to ${targetApp.displayName}")
                val shareOption = when (targetApp) {
                    ShareTarget.CLAUDE -> ShareOption.CLAUDE
                    ShareTarget.CHATGPT -> ShareOption.CHATGPT
                    else -> ShareOption.CHATGPT // Fallback (should never happen)
                }
                handleShareOption(shareOption)

                // Wait for share to complete, then close app
                delay(500)
                Logger.logI("SummariserViewModel: Closing TranscriptAI app")
                onFinishActivity?.invoke()
            } else {
                // Show share options (fallback) - don't close app
                Logger.logI("SummariserViewModel: ${targetApp.displayName} not installed, showing share options")
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
            is SummariserUiEvent.CopyTranscriptClicked -> copyTranscript()
            is SummariserUiEvent.RetryClicked -> retry()
            is SummariserUiEvent.LanguageChipClicked -> showLanguageSheet()
            is SummariserUiEvent.DismissLanguageSheet -> dismissLanguageSheet()
            is SummariserUiEvent.LanguageSelected -> switchLanguage(event.track)
            is SummariserUiEvent.DismissLanguageNotice -> dismissLanguageNotice()
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

        if (option == ShareOption.CLIPBOARD) {
            copyTranscript()
            dismissShareSheet()
            return
        }

        val subtitle = _uiState.value.subtitleResult?.text

        if (subtitle.isNullOrEmpty()) {
            Logger.logW("SummariserViewModel: Cannot share - subtitle is empty")
            return
        }

        viewModelScope.launch {
            val formattedText = formatSubtitleForCopyUseCase(subtitle)
            Logger.logI("SummariserViewModel: Formatted text length: ${formattedText.length}")

            val shareTarget = when (option) {
                ShareOption.CLIPBOARD -> ShareTarget.CLIPBOARD // Handled above; kept so the mapping stays exhaustive
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
     * Copy the transcript to the clipboard, with the AI prompt in front when it is enabled.
     * The screen writes to the clipboard and confirms in the snackbar.
     */
    private fun copyTranscript() {
        val subtitle = _uiState.value.subtitleResult?.text

        if (subtitle.isNullOrBlank()) {
            Logger.logW("SummariserViewModel: Cannot copy - subtitle is empty")
            _effects.trySend(SummariserUiEffect.ShowMessage("Nothing to copy"))
            return
        }

        viewModelScope.launch {
            val formattedText = formatSubtitleForCopyUseCase(subtitle)
            Logger.logI("SummariserViewModel: Copying ${formattedText.length} characters")
            val confirmation = if (formattedText != subtitle) "Copied with AI prompt" else "Transcript copied"
            _effects.send(
                SummariserUiEffect.CopyToClipboard(
                    text = formattedText,
                    label = SummariserConstants.CLIPBOARD_LABEL_FORMATTED,
                    confirmation = confirmation
                )
            )
        }
    }

    /**
     * Show the language picker sheet
     */
    private fun showLanguageSheet() {
        Logger.logI("SummariserViewModel: Showing language sheet")
        _uiState.update { it.copy(showLanguageSheet = true) }
    }

    /**
     * Dismiss the language picker sheet
     */
    private fun dismissLanguageSheet() {
        Logger.logI("SummariserViewModel: Dismissing language sheet")
        _uiState.update { it.copy(showLanguageSheet = false) }
    }

    /**
     * Dismiss the "language isn't available" banner
     */
    private fun dismissLanguageNotice() {
        _uiState.update { it.copy(languageNotice = null) }
    }

    /**
     * Download the transcript again in the chosen language, in place.
     * Only this video is affected; the language saved on the home screen stays as it is.
     * On failure the transcript that was showing stays and a snackbar explains what went wrong.
     *
     * @param track The caption track to download
     */
    private fun switchLanguage(track: CaptionTrack) {
        dismissLanguageSheet()

        val current = _uiState.value.subtitleResult
        if (current == null || _uiState.value.isSwitchingLanguage) return
        if (current.isShowing(track)) {
            Logger.logD("SummariserViewModel: Track ${track.languageCode} is already showing")
            return
        }

        val label = track.label()
        Logger.logI("SummariserViewModel: Switching language to ${track.languageCode} (auto=${track.isAutoGenerated})")

        viewModelScope.launch {
            _uiState.update { it.copy(isSwitchingLanguage = true, switchingLanguageLabel = label) }

            // The downloader only accepts a video link, not a bare video ID
            when (val result = downloadSubtitlesUseCase(youTubeWatchUrl(current.videoId), track.requestPreferences())) {
                is Result.Success -> {
                    // Keep what the new download doesn't repeat, so the header and language list stay intact
                    val updated = result.data.copy(
                        videoTitle = result.data.videoTitle ?: current.videoTitle,
                        availableTracks = result.data.availableTracks.ifEmpty { current.availableTracks }
                    )
                    // The screen reloads from the cache when it comes back from the prompt editor
                    subtitleCacheRepository.cacheSubtitle(updated)

                    // Auto-generated captions have no language name to report, so the notice words it generically
                    val requestedLanguage = if (track.isAutoGenerated) {
                        null
                    } else {
                        findCatalogLanguage(track.languageCode)?.englishName ?: track.name
                    }
                    _uiState.update {
                        it.copy(
                            subtitleResult = updated,
                            isSwitchingLanguage = false,
                            switchingLanguageLabel = null,
                            languageNotice = noticeFor(updated, requestedLanguage)
                        )
                    }
                }
                is Result.Error -> {
                    Logger.logE("SummariserViewModel: Language switch failed - ${result.message}")
                    _uiState.update { it.copy(isSwitchingLanguage = false, switchingLanguageLabel = null) }
                    _effects.send(SummariserUiEffect.ShowMessage("Couldn't load $label: ${result.message}"))
                }
                is Result.Loading -> {
                    Logger.logV("SummariserViewModel: Language switch still in progress")
                    _uiState.update { it.copy(isSwitchingLanguage = false, switchingLanguageLabel = null) }
                }
            }
        }
    }

    /**
     * Banner text for a download that fell back to another language, or null when the requested one was found.
     *
     * @param result The downloaded transcript
     * @param requestedLanguage English name of the requested language, or null for auto-generated captions
     */
    private fun noticeFor(result: SubtitleResult, requestedLanguage: String?): String? =
        if (result.requestedLanguageMatched) null else languageUnavailableNotice(requestedLanguage, result)

    /**
     * Navigate to the Prompt Editor screen
     */
    private fun navigateToPromptEditor() {
        Logger.logI("SummariserViewModel: Navigating to Prompt Editor")
        onNavigateToPromptEditor()
    }
}
