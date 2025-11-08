package com.abhishek.transcriptai.data.mapper

import com.abhishek.transcriptai.data.local.preferences.SummariserPreferences
import com.abhishek.transcriptai.domain.model.SummariserConfig

/**
 * Mapper object for converting between data layer and domain layer configuration models
 * Follows the repository pattern for clean separation of concerns
 */
object ConfigMapper {
    /**
     * Convert preferences config to domain model
     *
     * @param prefsConfig Configuration from SharedPreferences
     * @return Domain model representation of the configuration
     */
    fun toDomain(prefsConfig: SummariserPreferences.Config): SummariserConfig {
        return SummariserConfig(
            isAiSummariserEnabled = prefsConfig.isAiSummariserEnabled,
            selectedPromptId = prefsConfig.selectedPromptId
        )
    }

    /**
     * Convert domain config to preferences model
     *
     * @param domainConfig Configuration from domain layer
     * @return Preferences model representation of the configuration
     */
    fun toPrefs(domainConfig: SummariserConfig): SummariserPreferences.Config {
        return SummariserPreferences.Config(
            isAiSummariserEnabled = domainConfig.isAiSummariserEnabled,
            selectedPromptId = domainConfig.selectedPromptId
        )
    }
}
