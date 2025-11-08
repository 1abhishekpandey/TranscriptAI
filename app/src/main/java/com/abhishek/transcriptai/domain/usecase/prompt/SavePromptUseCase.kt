package com.abhishek.transcriptai.domain.usecase.prompt

import com.abhishek.transcriptai.domain.model.Prompt
import com.abhishek.transcriptai.domain.model.Result
import com.abhishek.transcriptai.domain.repository.PromptRepository

/**
 * Use case for saving a prompt (create or update)
 * This use case validates the prompt before saving
 *
 * @property repository The prompt repository
 */
class SavePromptUseCase(
    private val repository: PromptRepository
) {
    /**
     * Invoke the use case to save a prompt
     * @param prompt The prompt to save
     * @return Result indicating success or error with message
     */
    suspend operator fun invoke(prompt: Prompt): Result<Unit> {
        return try {
            if (prompt.text.isBlank()) {
                return Result.Error("Prompt text cannot be empty")
            }
            repository.savePrompt(prompt)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save prompt")
        }
    }
}
