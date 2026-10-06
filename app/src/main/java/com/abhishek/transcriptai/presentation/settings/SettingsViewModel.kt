package com.abhishek.transcriptai.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.transcriptai.domain.usecase.config.GetSummariserConfigUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.GetPromptsUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.resolveSelectedPromptId
import com.abhishek.transcriptai.util.Logger
import com.abhishek.youtubesubtitledownloader.YouTubeSubtitleDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings screen
 * Keeps the summary line of the "AI prompts" row up to date, so it changes as soon as the AI prompts screen
 * changes a setting, and provides the YouTube client version.
 *
 * @property getPromptsUseCase Use case for retrieving the saved prompts
 * @property getSummariserConfigUseCase Use case for getting the AI prompt switch and the selection
 * @property youtubeSubtitleDownloader The YouTube subtitle downloader extension, source of the client version
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getPromptsUseCase: GetPromptsUseCase,
    private val getSummariserConfigUseCase: GetSummariserConfigUseCase,
    private val youtubeSubtitleDownloader: YouTubeSubtitleDownloader
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(clientVersion = youtubeSubtitleDownloader.getCurrentClientVersion())
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        Logger.logD("SettingsViewModel: Initializing")
        observeAiPromptsSummary()
    }

    /**
     * Read the client version again, because the Version Input screen may have changed it
     */
    fun refreshClientVersion() {
        val version = youtubeSubtitleDownloader.getCurrentClientVersion()
        _uiState.update { it.copy(clientVersion = version) }
    }

    /**
     * Build the summary line from the saved prompts and settings, and rebuild it whenever either changes
     */
    private fun observeAiPromptsSummary() {
        viewModelScope.launch {
            combine(getPromptsUseCase(), getSummariserConfigUseCase()) { prompts, config -> prompts to config }
                .catch { e -> Logger.logE("SettingsViewModel: Error loading prompts or config", e) }
                .collect { (prompts, config) ->
                    val selectedId = resolveSelectedPromptId(prompts, config.selectedPromptId)
                    val selectedText = prompts.firstOrNull { it.id == selectedId }?.text
                    _uiState.update {
                        it.copy(aiPromptsSummary = aiPromptsSummary(config.isAiSummariserEnabled, selectedText))
                    }
                }
        }
    }
}
