package com.abhishek.transcriptai.data.repository

import com.abhishek.transcriptai.data.local.preferences.SummariserPreferences
import com.abhishek.transcriptai.data.mapper.ConfigMapper
import com.abhishek.transcriptai.domain.model.SummariserConfig
import com.abhishek.transcriptai.domain.repository.SummariserConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of SummariserConfigRepository
 * Manages AI summariser configuration using SharedPreferences
 *
 * @property preferences SharedPreferences wrapper for configuration storage
 */
class SummariserConfigRepositoryImpl(
    private val preferences: SummariserPreferences
) : SummariserConfigRepository {

    /**
     * Get the current configuration as a Flow for reactive updates
     * Maps preferences config to domain model
     *
     * @return Flow of SummariserConfig that emits whenever configuration changes
     */
    override fun getConfig(): Flow<SummariserConfig> {
        return preferences.configFlow.map { prefsConfig ->
            ConfigMapper.toDomain(prefsConfig)
        }
    }

    /**
     * Update the entire configuration
     * Updates both enabled state and selected prompt ID
     *
     * @param config The new configuration to save
     */
    override suspend fun updateConfig(config: SummariserConfig) {
        preferences.isAiSummariserEnabled = config.isAiSummariserEnabled
        preferences.selectedPromptId = config.selectedPromptId
    }

    /**
     * Toggle the AI summariser feature on/off
     *
     * @param enabled Whether the AI summariser should be enabled
     */
    override suspend fun toggleAiSummariser(enabled: Boolean) {
        preferences.isAiSummariserEnabled = enabled
    }

    /**
     * Set the selected prompt ID
     *
     * @param promptId The ID of the prompt to select
     */
    override suspend fun setSelectedPromptId(promptId: String) {
        preferences.selectedPromptId = promptId
    }
}
