package com.abhishek.transcriptai.domain.repository

import com.abhishek.transcriptai.domain.model.Prompt
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing prompts
 * This interface is part of the domain layer and should be implemented in the data layer
 */
interface PromptRepository {
    /**
     * Get all prompts as a Flow for reactive updates
     * @return Flow of list of prompts
     */
    fun getPrompts(): Flow<List<Prompt>>

    /**
     * Get a specific prompt by its ID
     * @param id The unique identifier of the prompt
     * @return The prompt if found, null otherwise
     */
    suspend fun getPromptById(id: String): Prompt?

    /**
     * Save a prompt (create new or update existing)
     * @param prompt The prompt to save
     */
    suspend fun savePrompt(prompt: Prompt)

    /**
     * Delete a prompt by its ID
     * @param id The unique identifier of the prompt to delete
     */
    suspend fun deletePrompt(id: String)

    /**
     * Update the last selected timestamp for a prompt
     * @param promptId The unique identifier of the prompt
     * @param timestamp The timestamp when the prompt was selected
     */
    suspend fun updateLastSelectedTimestamp(promptId: String, timestamp: Long)

    /**
     * Replace every saved prompt with the built-in default prompts.
     * The swap is all or nothing, so the list is never left empty or half replaced.
     * @return The restored default prompts, in the order they are meant to be offered (first is the one to select)
     */
    suspend fun resetToDefaultPrompts(): List<Prompt>

    /**
     * How many built-in default prompts the app ships with
     * @return The number of default prompts
     */
    fun getDefaultPromptCount(): Int
}
