package com.abhishek.transcriptai.domain.repository

import com.abhishek.transcriptai.domain.model.LanguageChoice
import com.abhishek.transcriptai.domain.model.LanguageMode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for the user's subtitle language choice.
 */
interface LanguagePreferenceRepository {
    /**
     * Observes the saved choice, emitting again whenever it changes.
     */
    fun observeLanguageChoice(): Flow<LanguageChoice>

    /**
     * Gets the saved choice. Defaults to auto-generated captions.
     */
    suspend fun getLanguageChoice(): LanguageChoice

    /**
     * Selects an option on the home screen.
     */
    suspend fun setLanguageMode(mode: LanguageMode)

    /**
     * Saves a language from the settings page and selects the "Other" option.
     *
     * @param code language code (e.g., "id")
     */
    suspend fun setOtherLanguage(code: String)
}
