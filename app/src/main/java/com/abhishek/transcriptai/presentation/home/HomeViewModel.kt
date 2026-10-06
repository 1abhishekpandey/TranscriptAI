package com.abhishek.transcriptai.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.transcriptai.data.repository.SubtitleCacheRepository
import com.abhishek.transcriptai.domain.model.LanguageChoice
import com.abhishek.transcriptai.domain.model.LanguageMode
import com.abhishek.transcriptai.domain.model.Result
import com.abhishek.transcriptai.domain.repository.AutoShareConfigRepository
import com.abhishek.transcriptai.domain.repository.LanguagePreferenceRepository
import com.abhishek.transcriptai.domain.usecase.DownloadSubtitlesUseCase
import com.abhishek.transcriptai.util.Logger
import com.abhishek.youtubesubtitledownloader.YouTubeSubtitleDownloader
import com.abhishek.youtubesubtitledownloader.domain.model.ErrorType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel for Home Screen
 * Manages UI state and handles user interactions
 *
 * @param downloadSubtitlesUseCase Use case for downloading subtitles
 * @param subtitleCacheRepository Repository for caching subtitle data
 * @param autoShareConfigRepository Repository for auto-share configuration
 * @param languagePreferenceRepository Repository for the saved subtitle language choice
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val downloadSubtitlesUseCase: DownloadSubtitlesUseCase,
    private val subtitleCacheRepository: SubtitleCacheRepository,
    private val autoShareConfigRepository: AutoShareConfigRepository,
    private val languagePreferenceRepository: LanguagePreferenceRepository,
    private val youtubeSubtitleDownloader: YouTubeSubtitleDownloader
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _videoUrl = MutableStateFlow("")
    val videoUrl: StateFlow<String> = _videoUrl.asStateFlow()

    // Saved language choice (default is auto-generated captions)
    private val _languageChoice = MutableStateFlow(LanguageChoice())
    val languageChoice: StateFlow<LanguageChoice> = _languageChoice.asStateFlow()

    // Language selection expansion state
    private val _languageExpanded = MutableStateFlow(false)
    val languageExpanded: StateFlow<Boolean> = _languageExpanded.asStateFlow()

    // Auto-share enabled state
    private val _autoShareEnabled = MutableStateFlow(false)
    val autoShareEnabled: StateFlow<Boolean> = _autoShareEnabled.asStateFlow()

    // Selected app for auto-sharing (default is ChatGPT)
    private val _selectedApp = MutableStateFlow("chatgpt")
    val selectedApp: StateFlow<String> = _selectedApp.asStateFlow()

    private var currentSubtitle: String = ""

    // Navigation callback - set from UI layer
    private var onNavigateToSummariser: ((videoId: String) -> Unit)? = null

    // Version input navigation callback
    private var onNavigateToVersionInput: (() -> Unit)? = null

    init {
        Logger.logI("HomeViewModel: Initialized")

        // Load auto-share preferences
        viewModelScope.launch {
            _autoShareEnabled.value = autoShareConfigRepository.isAutoShareEnabled()
            _selectedApp.value = autoShareConfigRepository.getSelectedApp()
            Logger.logI("HomeViewModel: Auto-share preference loaded: ${_autoShareEnabled.value}")
            Logger.logI("HomeViewModel: Selected app loaded: ${_selectedApp.value}")
        }

        // Follow the saved language choice, including changes made on the language settings page
        viewModelScope.launch {
            languagePreferenceRepository.observeLanguageChoice().collect {
                _languageChoice.value = it
                Logger.logI("HomeViewModel: Language choice: ${it.mode}, other language: ${it.otherLanguageCode}")
            }
        }
    }

    /**
     * Set navigation callback for navigating to Summariser screen
     * Called from UI layer after successful subtitle download
     *
     * @param callback Navigation callback function
     */
    fun setNavigationCallback(callback: (videoId: String) -> Unit) {
        Logger.logD("HomeViewModel: Navigation callback set")
        onNavigateToSummariser = callback
    }

    /**
     * Set navigation callback for navigating to Version Input screen
     * Called from UI layer when client version is outdated
     *
     * @param callback Navigation callback function
     */
    fun setVersionInputNavigationCallback(callback: () -> Unit) {
        Logger.logD("HomeViewModel: Version input navigation callback set")
        onNavigateToVersionInput = callback
    }

    /**
     * Navigate to Version Input screen
     */
    fun navigateToVersionInput() {
        onNavigateToVersionInput?.invoke()
    }

    /**
     * Handle UI events
     */
    fun onEvent(event: HomeUiEvent) {
        Logger.logD("HomeViewModel: Received event: ${event::class.simpleName}")

        when (event) {
            is HomeUiEvent.DownloadSubtitle -> {
                downloadSubtitle(event.videoUrl)
            }
            is HomeUiEvent.CopyToClipboard -> {
                copyToClipboard()
            }
            is HomeUiEvent.UpdateVideoUrl -> {
                updateVideoUrl(event.url)
            }
            is HomeUiEvent.SelectLanguageMode -> {
                selectLanguageMode(event.mode)
            }
            is HomeUiEvent.ToggleLanguageExpansion -> {
                toggleLanguageExpansion()
            }
            is HomeUiEvent.ClearContent -> {
                clearContent()
            }
            is HomeUiEvent.ToggleAutoShare -> {
                toggleAutoShare(event.enabled)
            }
            is HomeUiEvent.SelectAutoShareApp -> {
                selectAutoShareApp(event.app)
            }
        }
    }

    /**
     * Download subtitles for the given video URL
     */
    private fun downloadSubtitle(videoUrl: String) {
        if (_uiState.value is HomeUiState.Loading) {
            Logger.logW("HomeViewModel: Download already in progress, ignoring request")
            return
        }

        Logger.logI("HomeViewModel: Starting subtitle download for: $videoUrl")

        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading(LOADING_MESSAGE)
                Logger.logV("HomeViewModel: State changed to Loading")

                // Build language preferences and call use case
                val languagePreferences = buildLanguagePreferences()
                val result = downloadSubtitlesUseCase(videoUrl, languagePreferences)

                // Handle result
                when (result) {
                    is Result.Success -> {
                        currentSubtitle = result.data.text
                        Logger.logI("HomeViewModel: Successfully loaded subtitles (${result.data.text.length} chars)")

                        // Cache the subtitle data for navigation
                        subtitleCacheRepository.cacheSubtitle(result.data)
                        Logger.logD("HomeViewModel: Subtitle cached for video ${result.data.videoId}")

                        // Trigger navigation to Summariser screen immediately
                        onNavigateToSummariser?.let { callback ->
                            Logger.logI("HomeViewModel: Triggering navigation to Summariser screen for video ${result.data.videoId}")
                            callback(result.data.videoId)

                            // Reset to idle state after navigation to clear any displayed content
                            _uiState.value = HomeUiState.Idle
                            Logger.logD("HomeViewModel: Reset to Idle state after navigation")
                        } ?: run {
                            Logger.logW("HomeViewModel: Navigation callback not set, staying on Home screen")
                            // Only show success state if navigation callback is not set
                            _uiState.value = HomeUiState.Success(
                                subtitle = result.data.text,
                                videoTitle = result.data.videoTitle
                            )
                        }
                    }
                    is Result.Error -> {
                        if (result.errorType == ErrorType.CLIENT_VERSION_OUTDATED.name) {
                            val currentVersion = youtubeSubtitleDownloader.getCurrentClientVersion()
                            _uiState.value = HomeUiState.VersionOutdated(
                                message = result.message,
                                currentVersion = currentVersion
                            )
                            Logger.logW("HomeViewModel: Client version outdated. Current: $currentVersion")
                        } else {
                            _uiState.value = HomeUiState.Error(toFriendlyMessage(result))
                            Logger.logE("HomeViewModel: Error loading subtitles: ${result.message}", result.exception)
                        }
                    }
                    is Result.Loading -> {
                        // Already in loading state
                        Logger.logV("HomeViewModel: Result is still loading")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(GENERIC_ERROR_MESSAGE)
                Logger.logE("HomeViewModel: Unexpected error in downloadSubtitle", e)
            }
        }
    }

    /**
     * Turn a download error into a short message the user can act on.
     * Known error types get fixed wording; anything else keeps the reported
     * reason without the repository's "Failed to download subtitles:" prefix.
     */
    private fun toFriendlyMessage(error: Result.Error): String {
        if (error.exception is IOException || error.errorType == ErrorType.NETWORK_ERROR.name) {
            return "Couldn't reach YouTube. Check your internet connection and try again."
        }

        return when (error.errorType) {
            ErrorType.INVALID_URL.name,
            ErrorType.INVALID_VIDEO_ID.name ->
                "That doesn't look like a YouTube link. Check the URL and try again."
            ErrorType.NO_SUBTITLES_AVAILABLE.name,
            ErrorType.CAPTION_TRACKS_NOT_FOUND.name ->
                "This video has no subtitles available."
            ErrorType.LANGUAGE_NOT_AVAILABLE.name ->
                "This video has no subtitles in the selected language. Try Auto-generated or another language."
            ErrorType.API_KEY_EXTRACTION_FAILED.name,
            ErrorType.TRANSCRIPT_DOWNLOAD_FAILED.name ->
                "Couldn't fetch subtitles from YouTube. Please try again."
            ErrorType.TRANSCRIPT_PARSE_FAILED.name ->
                "Couldn't read this video's subtitles. Please try again."
            else -> error.message
                .removePrefix(DOWNLOAD_FAILED_PREFIX)
                .trim()
                .takeIf { it.isNotEmpty() && it != "null" }
                ?: GENERIC_ERROR_MESSAGE
        }
    }

    /**
     * Copy current subtitle to clipboard
     * Returns the text to be copied so the UI can handle the actual clipboard operation
     */
    private fun copyToClipboard() {
        Logger.logI("HomeViewModel: Copy to clipboard requested")

        if (currentSubtitle.isNotEmpty()) {
            Logger.logD("HomeViewModel: Copying ${currentSubtitle.length} characters to clipboard")
            // The UI layer will handle the actual clipboard operation
            // This is just for logging purposes
        } else {
            Logger.logW("HomeViewModel: No subtitle available to copy")
        }
    }

    /**
     * Update the video URL input
     */
    private fun updateVideoUrl(url: String) {
        Logger.logV("HomeViewModel: Video URL updated: $url")
        _videoUrl.value = url

        // A failure message belongs to the previous URL, so drop it once the URL changes
        if (_uiState.value is HomeUiState.Error || _uiState.value is HomeUiState.VersionOutdated) {
            _uiState.value = HomeUiState.Idle
        }
    }

    /**
     * Set initial URL from deep link
     */
    fun setInitialUrl(url: String?) {
        url?.let {
            Logger.logI("HomeViewModel: Setting initial URL from deep link: $it")
            _videoUrl.value = it
        }
    }

    /**
     * Select a language option and save it
     */
    private fun selectLanguageMode(mode: LanguageMode) {
        Logger.logI("HomeViewModel: Language option selected: $mode")
        _languageChoice.value = _languageChoice.value.copy(mode = mode)

        viewModelScope.launch {
            languagePreferenceRepository.setLanguageMode(mode)
        }
    }

    /**
     * Toggle language selection expansion
     */
    private fun toggleLanguageExpansion() {
        _languageExpanded.value = !_languageExpanded.value
        Logger.logD("HomeViewModel: Language selection ${if (_languageExpanded.value) "expanded" else "collapsed"}")
    }

    /**
     * Get the current subtitle text for clipboard operations
     */
    fun getCurrentSubtitle(): String {
        return currentSubtitle
    }

    /**
     * Build language preferences list based on user selection
     */
    private fun buildLanguagePreferences(): List<String> {
        val preferences = _languageChoice.value.toLanguagePreferences()
        Logger.logD("HomeViewModel: Language preference: ${preferences.joinToString(", ")}")
        return preferences
    }

    /**
     * Clear all content and reset to initial state
     */
    private fun clearContent() {
        Logger.logI("HomeViewModel: Clearing all content")

        _uiState.value = HomeUiState.Idle
        _videoUrl.value = ""
        currentSubtitle = ""

        // Clear cached subtitle
        subtitleCacheRepository.clearCache()

        Logger.logD("HomeViewModel: All content cleared, reset to initial state")
    }

    /**
     * Toggle auto-share preference
     */
    private fun toggleAutoShare(enabled: Boolean) {
        Logger.logI("HomeViewModel: Auto-share toggled to $enabled")

        viewModelScope.launch {
            autoShareConfigRepository.setAutoShareEnabled(enabled)
            _autoShareEnabled.value = enabled
            Logger.logD("HomeViewModel: Auto-share preference saved")
        }
    }

    /**
     * Select target app for auto-sharing
     */
    private fun selectAutoShareApp(app: String) {
        Logger.logI("HomeViewModel: Auto-share app selected: $app")

        viewModelScope.launch {
            autoShareConfigRepository.setSelectedApp(app)
            _selectedApp.value = app
            Logger.logD("HomeViewModel: Auto-share app preference saved")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.logI("HomeViewModel: Cleared")
    }

    private companion object {
        const val LOADING_MESSAGE = "Fetching subtitles..."
        const val GENERIC_ERROR_MESSAGE = "Something went wrong. Please try again."
        const val DOWNLOAD_FAILED_PREFIX = "Failed to download subtitles:"
    }
}
