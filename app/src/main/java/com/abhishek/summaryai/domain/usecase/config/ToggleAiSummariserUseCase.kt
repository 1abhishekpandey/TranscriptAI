package com.abhishek.summaryai.domain.usecase.config

import com.abhishek.summaryai.domain.repository.SummariserConfigRepository

/**
 * Use case for toggling the AI summariser feature on/off
 *
 * @property repository The summariser config repository
 */
class ToggleAiSummariserUseCase(
    private val repository: SummariserConfigRepository
) {
    /**
     * Invoke the use case to toggle the AI summariser
     * @param enabled Whether the AI summariser should be enabled
     */
    suspend operator fun invoke(enabled: Boolean) = repository.toggleAiSummariser(enabled)
}
