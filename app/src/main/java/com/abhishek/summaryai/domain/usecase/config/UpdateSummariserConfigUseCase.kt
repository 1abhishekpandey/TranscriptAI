package com.abhishek.summaryai.domain.usecase.config

import com.abhishek.summaryai.domain.model.SummariserConfig
import com.abhishek.summaryai.domain.repository.SummariserConfigRepository

/**
 * Use case for updating the AI summariser configuration
 *
 * @property repository The summariser config repository
 */
class UpdateSummariserConfigUseCase(
    private val repository: SummariserConfigRepository
) {
    /**
     * Invoke the use case to update the configuration
     * @param config The new configuration to save
     */
    suspend operator fun invoke(config: SummariserConfig) = repository.updateConfig(config)
}
