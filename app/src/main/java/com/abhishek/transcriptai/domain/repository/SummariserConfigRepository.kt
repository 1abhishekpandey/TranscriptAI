package com.abhishek.transcriptai.domain.repository

import com.abhishek.transcriptai.domain.model.SummariserConfig
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing AI summariser configuration
 * This interface is part of the domain layer and should be implemented in the data layer
 */
interface SummariserConfigRepository {
    /**
     * Get the current configuration as a Flow for reactive updates
     * @return Flow of SummariserConfig
     */
    fun getConfig(): Flow<SummariserConfig>

    /**
     * Update the entire configuration
     * @param config The new configuration to save
     */
    suspend fun updateConfig(config: SummariserConfig)

    /**
     * Toggle the AI summariser feature on/off
     * @param enabled Whether the AI summariser should be enabled
     */
    suspend fun toggleAiSummariser(enabled: Boolean)

    /**
     * Set the selected prompt ID
     * @param promptId The ID of the prompt to select
     */
    suspend fun setSelectedPromptId(promptId: String)
}
