package com.abhishek.transcriptai.domain.usecase.config

import com.abhishek.transcriptai.domain.model.SummariserConfig
import com.abhishek.transcriptai.domain.repository.SummariserConfigRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving the AI summariser configuration
 * This use case provides a reactive stream of configuration changes
 *
 * @property repository The summariser config repository
 */
class GetSummariserConfigUseCase(
    private val repository: SummariserConfigRepository
) {
    /**
     * Invoke the use case to get the current configuration
     * @return Flow of SummariserConfig
     */
    operator fun invoke(): Flow<SummariserConfig> = repository.getConfig()
}
