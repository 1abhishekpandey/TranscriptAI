package com.abhishek.summaryai.domain.repository

import com.abhishek.summaryai.domain.model.Prompt
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
}
