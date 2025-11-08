package com.abhishek.summaryai.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.summaryai.data.repository.SubtitleCacheRepository
import com.abhishek.summaryai.domain.model.Result
import com.abhishek.summaryai.domain.usecase.DownloadSubtitlesUseCase
import com.abhishek.summaryai.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home Screen
 * Manages UI state and handles user interactions
 *
 * @param downloadSubtitlesUseCase Use case for downloading subtitles
 * @param subtitleCacheRepository Repository for caching subtitle data
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val downloadSubtitlesUseCase: DownloadSubtitlesUseCase,
    private val subtitleCacheRepository: SubtitleCacheRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _videoUrl = MutableStateFlow("")
    val videoUrl: StateFlow<String> = _videoUrl.asStateFlow()

    // Selected language (default is English)
    private val _selectedLanguage = MutableStateFlow(SubtitleLanguage.ENGLISH)
    val selectedLanguage: StateFlow<SubtitleLanguage> = _selectedLanguage.asStateFlow()

    // Language selection expansion state
    private val _languageExpanded = MutableStateFlow(false)
    val languageExpanded: StateFlow<Boolean> = _languageExpanded.asStateFlow()

    private var currentSubtitle: String = ""

    // Navigation callback - set from UI layer
    private var onNavigateToSummariser: ((videoId: String) -> Unit)? = null

    init {
        Logger.logI("HomeViewModel: Initialized")
        Logger.logD("HomeViewModel: Default language: ${SubtitleLanguage.ENGLISH.displayName}")
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
            is HomeUiEvent.SelectLanguage -> {
                selectLanguage(event.language)
            }
            is HomeUiEvent.ToggleLanguageExpansion -> {
                toggleLanguageExpansion()
            }
            is HomeUiEvent.ClearContent -> {
                clearContent()
            }
        }
    }

    /**
     * Download subtitles for the given video URL
     */
    private fun downloadSubtitle(videoUrl: String) {
        Logger.logI("HomeViewModel: Starting subtitle download for: $videoUrl")

        viewModelScope.launch {
            try {
                // Update to loading state with initial message
                _uiState.value = HomeUiState.Loading("Fetching video info...")
                Logger.logV("HomeViewModel: State changed to Loading - Fetching video info")

                // Simulate progress updates
                kotlinx.coroutines.delay(500)
                _uiState.value = HomeUiState.Loading("Retrieving subtitle tracks...")
                Logger.logV("HomeViewModel: State changed to Loading - Retrieving subtitle tracks")

                kotlinx.coroutines.delay(500)
                _uiState.value = HomeUiState.Loading("Parsing subtitles...")
                Logger.logV("HomeViewModel: State changed to Loading - Parsing subtitles")

                // Build language preferences and call use case
                val languagePreferences = buildLanguagePreferences()
                val result = downloadSubtitlesUseCase(videoUrl, languagePreferences)

                // Handle result
                when (result) {
                    is Result.Success -> {
                        currentSubtitle = result.data.text
                        _uiState.value = HomeUiState.Success(
                            subtitle = result.data.text,
                            videoTitle = result.data.videoTitle
                        )
                        Logger.logI("HomeViewModel: Successfully loaded subtitles (${result.data.text.length} chars)")

                        // Cache the subtitle data for navigation
                        subtitleCacheRepository.cacheSubtitle(result.data)
                        Logger.logD("HomeViewModel: Subtitle cached for video ${result.data.videoId}")

                        // Trigger navigation to Summariser screen
                        onNavigateToSummariser?.let { callback ->
                            Logger.logI("HomeViewModel: Triggering navigation to Summariser screen for video ${result.data.videoId}")
                            callback(result.data.videoId)
                        } ?: run {
                            Logger.logW("HomeViewModel: Navigation callback not set, staying on Home screen")
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = HomeUiState.Error(result.message)
                        Logger.logE("HomeViewModel: Error loading subtitles: ${result.message}", result.exception)
                    }
                    is Result.Loading -> {
                        // Already in loading state
                        Logger.logV("HomeViewModel: Result is still loading")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("An unexpected error occurred: ${e.message}")
                Logger.logE("HomeViewModel: Unexpected error in downloadSubtitle", e)
            }
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
     * Select a language
     */
    private fun selectLanguage(language: SubtitleLanguage) {
        Logger.logI("HomeViewModel: Language selected: ${language.displayName}")
        _selectedLanguage.value = language
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
        val preferences = listOf(_selectedLanguage.value.code)
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
        _selectedLanguage.value = SubtitleLanguage.ENGLISH
        _languageExpanded.value = false

        // Clear cached subtitle
        subtitleCacheRepository.clearCache()

        Logger.logD("HomeViewModel: All content cleared, reset to initial state")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.logI("HomeViewModel: Cleared")
    }
}
