package com.abhishek.transcriptai.presentation.versioninput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.transcriptai.util.Logger
import com.abhishek.youtubesubtitledownloader.YouTubeSubtitleDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Version Input Screen
 * Manages testing and saving of YouTube client versions
 *
 * @param youtubeSubtitleDownloader The YouTube subtitle downloader extension
 */
@HiltViewModel
class VersionInputViewModel @Inject constructor(
    private val youtubeSubtitleDownloader: YouTubeSubtitleDownloader
) : ViewModel() {

    private val _uiState = MutableStateFlow<VersionInputUiState>(
        VersionInputUiState.Idle(youtubeSubtitleDownloader.getCurrentClientVersion())
    )
    val uiState: StateFlow<VersionInputUiState> = _uiState.asStateFlow()

    private val _versionInput = MutableStateFlow("")
    val versionInput: StateFlow<String> = _versionInput.asStateFlow()

    init {
        Logger.logI("VersionInputViewModel: Initialized with current version: ${youtubeSubtitleDownloader.getCurrentClientVersion()}")
    }

    /**
     * Handle UI events
     */
    fun onEvent(event: VersionInputUiEvent) {
        Logger.logD("VersionInputViewModel: Received event: ${event::class.simpleName}")

        when (event) {
            is VersionInputUiEvent.UpdateVersion -> {
                _versionInput.value = event.version
            }
            is VersionInputUiEvent.TestVersion -> {
                testVersion(event.version)
            }
            is VersionInputUiEvent.SaveVersion -> {
                saveVersion(event.version)
            }
        }
    }

    /**
     * Test a client version against YouTube's InnerTube API
     */
    private fun testVersion(version: String) {
        Logger.logI("VersionInputViewModel: Testing version: $version")
        _uiState.value = VersionInputUiState.Testing(version)

        viewModelScope.launch {
            try {
                // Use a well-known public video with subtitles for testing
                val testVideoId = "dQw4w9WgXcQ"
                val success = youtubeSubtitleDownloader.testClientVersion(testVideoId, version)

                if (success) {
                    Logger.logI("VersionInputViewModel: Version $version works!")
                    _uiState.value = VersionInputUiState.TestSuccess(version)
                } else {
                    Logger.logW("VersionInputViewModel: Version $version failed")
                    _uiState.value = VersionInputUiState.TestFailed(
                        version = version,
                        error = "Version $version was rejected by YouTube. Try a different version."
                    )
                }
            } catch (e: Exception) {
                Logger.logE("VersionInputViewModel: Error testing version", e)
                _uiState.value = VersionInputUiState.TestFailed(
                    version = version,
                    error = "Error testing version: ${e.message}"
                )
            }
        }
    }

    /**
     * Save a tested version as the active client version
     */
    private fun saveVersion(version: String) {
        Logger.logI("VersionInputViewModel: Saving version: $version")
        youtubeSubtitleDownloader.setClientVersion(version)
        _uiState.value = VersionInputUiState.Saved
        Logger.logI("VersionInputViewModel: Version saved successfully")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.logI("VersionInputViewModel: Cleared")
    }
}
