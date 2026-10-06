package com.abhishek.transcriptai.domain.usecase.prompt

import com.abhishek.transcriptai.domain.model.Result
import com.abhishek.transcriptai.domain.repository.PromptRepository

/**
 * Use case for resetting the saved prompts to the built-in defaults
 * Every prompt is replaced in one step: your own prompts are deleted and edited defaults go back to their
 * original text. The first default then becomes the selected prompt, so the transcript page and the
 * auto-share flow use it.
 *
 * @property promptRepository The prompt repository
 * @property selectPromptUseCase Use case for making a prompt the selected one
 */
class ResetPromptsUseCase(
    private val promptRepository: PromptRepository,
    private val selectPromptUseCase: SelectPromptUseCase
) {
    /**
     * Invoke the use case to reset the prompts
     * @return Result with the number of default prompts restored, or an error with a message
     */
    suspend operator fun invoke(): Result<Int> {
        return try {
            val defaults = promptRepository.resetToDefaultPrompts()
            defaults.firstOrNull()?.let { selectPromptUseCase(it.id) }
            Result.Success(defaults.size)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to reset prompts")
        }
    }

    /**
     * How many default prompts a reset restores
     * @return The number of default prompts
     */
    fun defaultPromptCount(): Int = promptRepository.getDefaultPromptCount()
}
