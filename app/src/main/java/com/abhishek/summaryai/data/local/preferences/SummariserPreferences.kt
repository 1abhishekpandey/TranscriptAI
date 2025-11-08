package com.abhishek.summaryai.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SharedPreferences wrapper for managing AI summariser configuration
 * Provides reactive updates via Flow and thread-safe operations
 */
class SummariserPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _configFlow = MutableStateFlow(getCurrentConfig())
    val configFlow: Flow<Config> = _configFlow.asStateFlow()

    /**
     * Data class representing the AI summariser configuration
     *
     * @property isAiSummariserEnabled Whether the AI summariser feature is enabled
     * @property selectedPromptId The ID of the currently selected prompt (null if none selected)
     */
    data class Config(
        val isAiSummariserEnabled: Boolean,
        val selectedPromptId: String?
    )

    /**
     * Get/Set whether the AI summariser is enabled
     * Default: true (enabled by default)
     */
    var isAiSummariserEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_AI_SUMMARISER_ENABLED, DEFAULT_AI_SUMMARISER_ENABLED)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_AI_SUMMARISER_ENABLED, value).apply()
            _configFlow.value = getCurrentConfig()
        }

    /**
     * Get/Set the selected prompt ID
     * Default: null (no prompt selected)
     */
    var selectedPromptId: String?
        get() = sharedPreferences.getString(KEY_SELECTED_PROMPT_ID, null)
        set(value) {
            sharedPreferences.edit().putString(KEY_SELECTED_PROMPT_ID, value).apply()
            _configFlow.value = getCurrentConfig()
        }

    /**
     * Get the current configuration snapshot
     * @return Current Config containing all preference values
     */
    private fun getCurrentConfig(): Config {
        return Config(
            isAiSummariserEnabled = isAiSummariserEnabled,
            selectedPromptId = selectedPromptId
        )
    }

    companion object {
        private const val PREFS_NAME = "summariser_preferences"
        private const val KEY_AI_SUMMARISER_ENABLED = "ai_summariser_enabled"
        private const val KEY_SELECTED_PROMPT_ID = "selected_prompt_id"
        private const val DEFAULT_AI_SUMMARISER_ENABLED = true
    }
}
