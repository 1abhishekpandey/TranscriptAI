package com.abhishek.transcriptai.data.local

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SharedPreferences wrapper for how the transcript is shown and sent.
 *
 * Kept apart from the AI prompt settings, which are saved as one block and would overwrite this switch.
 */
class TranscriptPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _showTimestamps = MutableStateFlow(prefs.getBoolean(KEY_SHOW_TIMESTAMPS, false))

    /** Whether each caption line keeps its time code; off by default */
    val showTimestamps: StateFlow<Boolean> = _showTimestamps.asStateFlow()

    fun setShowTimestamps(show: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_TIMESTAMPS, show) }
        _showTimestamps.value = show
    }

    companion object {
        private const val PREFS_NAME = "transcript_preferences"
        private const val KEY_SHOW_TIMESTAMPS = "show_timestamps"
    }
}
