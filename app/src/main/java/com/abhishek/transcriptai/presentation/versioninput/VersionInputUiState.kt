package com.abhishek.transcriptai.presentation.versioninput

/**
 * UI State for Version Input Screen
 * Represents all possible states of the version testing/saving flow
 */
sealed class VersionInputUiState {
    /**
     * Initial/idle state showing the current version
     * @param currentVersion The currently configured client version
     */
    data class Idle(val currentVersion: String) : VersionInputUiState()

    /**
     * Testing a version against YouTube
     * @param version The version being tested
     */
    data class Testing(val version: String) : VersionInputUiState()

    /**
     * Version test succeeded
     * @param version The version that was successfully tested
     */
    data class TestSuccess(val version: String) : VersionInputUiState()

    /**
     * Version test failed
     * @param version The version that failed
     * @param error Human-readable error message
     */
    data class TestFailed(val version: String, val error: String) : VersionInputUiState()

    /**
     * Version saved successfully
     */
    data object Saved : VersionInputUiState()
}

/**
 * UI Events for Version Input Screen
 * Represents user actions
 */
sealed class VersionInputUiEvent {
    /**
     * User updated the version input text
     */
    data class UpdateVersion(val version: String) : VersionInputUiEvent()

    /**
     * User clicked the "Test Version" button
     */
    data class TestVersion(val version: String) : VersionInputUiEvent()

    /**
     * User clicked the "Save & Use" button
     */
    data class SaveVersion(val version: String) : VersionInputUiEvent()
}
