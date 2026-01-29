package com.abhishek.transcriptai.domain.repository

/**
 * Repository interface for managing auto-share configuration preferences.
 *
 * Provides access to user preferences for automatic sharing functionality,
 * allowing the app to automatically share transcripts to ChatGPT when
 * launched via share intent.
 */
interface AutoShareConfigRepository {
    /**
     * Checks if auto-share feature is enabled.
     *
     * @return true if auto-share is enabled, false otherwise
     */
    suspend fun isAutoShareEnabled(): Boolean

    /**
     * Updates the auto-share feature state.
     *
     * @param enabled true to enable auto-share, false to disable
     */
    suspend fun setAutoShareEnabled(enabled: Boolean)
}
