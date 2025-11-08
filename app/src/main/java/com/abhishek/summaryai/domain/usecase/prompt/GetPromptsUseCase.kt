package com.abhishek.summaryai.domain.usecase.prompt

import com.abhishek.summaryai.domain.model.Prompt
import com.abhishek.summaryai.domain.repository.PromptRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving all prompts
 * This use case provides a reactive stream of prompts
 *
 * @property repository The prompt repository
 */
class GetPromptsUseCase(
    private val repository: PromptRepository
) {
    /**
     * Invoke the use case to get all prompts
     * @return Flow of list of prompts
     */
    operator fun invoke(): Flow<List<Prompt>> = repository.getPrompts()
}
