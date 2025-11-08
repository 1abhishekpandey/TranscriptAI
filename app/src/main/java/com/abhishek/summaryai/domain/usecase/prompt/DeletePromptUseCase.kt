package com.abhishek.summaryai.domain.usecase.prompt

import com.abhishek.summaryai.domain.model.Prompt
import com.abhishek.summaryai.domain.model.Result
import com.abhishek.summaryai.domain.repository.PromptRepository

/**
 * Use case for deleting a prompt
 * This use case validates that at least one prompt remains after deletion
 *
 * @property repository The prompt repository
 */
class DeletePromptUseCase(
    private val repository: PromptRepository
) {
    /**
     * Invoke the use case to delete a prompt
     * @param promptId The ID of the prompt to delete
     * @param allPrompts All existing prompts (used to validate minimum count)
     * @return Result indicating success or error with message
     */
    suspend operator fun invoke(promptId: String, allPrompts: List<Prompt>): Result<Unit> {
        return try {
            // Prevent deletion if it's the last prompt
            if (allPrompts.size <= 1) {
                return Result.Error("Cannot delete the last prompt")
            }
            repository.deletePrompt(promptId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete prompt")
        }
    }
}
