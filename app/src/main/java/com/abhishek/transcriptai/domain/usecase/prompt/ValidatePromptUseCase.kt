package com.abhishek.transcriptai.domain.usecase.prompt

import com.abhishek.transcriptai.domain.model.Prompt

/**
 * Use case for validating prompt text
 * This use case checks for empty text, minimum length, and duplicates
 */
class ValidatePromptUseCase {
    /**
     * Invoke the use case to validate a prompt text
     * @param text The prompt text to validate
     * @param existingPrompts List of existing prompts to check for duplicates
     * @param currentPromptId The ID of the current prompt being edited (null if creating new)
     * @return ValidationResult indicating the validation status
     */
    operator fun invoke(text: String, existingPrompts: List<Prompt>, currentPromptId: String?): ValidationResult {
        return when {
            text.isBlank() -> ValidationResult.Empty
            text.length < 10 -> ValidationResult.TooShort
            existingPrompts.any { it.text.trim().equals(text.trim(), ignoreCase = true) && it.id != currentPromptId } ->
                ValidationResult.Duplicate
            else -> ValidationResult.Valid
        }
    }

    /**
     * Sealed class representing the result of prompt validation
     */
    sealed class ValidationResult {
        /**
         * The prompt text is valid
         */
        data object Valid : ValidationResult()

        /**
         * The prompt text is empty
         */
        data object Empty : ValidationResult()

        /**
         * The prompt text is too short (less than 10 characters)
         */
        data object TooShort : ValidationResult()

        /**
         * The prompt text is a duplicate of an existing prompt
         */
        data object Duplicate : ValidationResult()
    }
}
