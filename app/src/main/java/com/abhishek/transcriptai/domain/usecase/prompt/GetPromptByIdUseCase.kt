package com.abhishek.transcriptai.domain.usecase.prompt

import com.abhishek.transcriptai.domain.model.Prompt
import com.abhishek.transcriptai.domain.repository.PromptRepository

/**
 * Use case for retrieving a specific prompt by its ID
 *
 * @property repository The prompt repository
 */
class GetPromptByIdUseCase(
    private val repository: PromptRepository
) {
    /**
     * Invoke the use case to get a prompt by ID
     * @param id The unique identifier of the prompt
     * @return The prompt if found, null otherwise
     */
    suspend operator fun invoke(id: String): Prompt? = repository.getPromptById(id)
}
