package com.abhishek.transcriptai.domain.repository

/**
 * Repository interface for managing auto-share configuration preferences.
 *
 * Provides access to user preferences for automatic sharing functionality,
 * allowing the app to automatically share transcripts to an AI app when
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

    /**
     * Gets the selected app for auto-sharing.
     *
     * @return app id, a ShareTarget.id such as "chatgpt", "claude" or "deepseek"
     */
    suspend fun getSelectedApp(): String

    /**
     * Sets the selected app for auto-sharing.
     *
     * @param app app id, a ShareTarget.id such as "chatgpt", "claude" or "deepseek"
     */
    suspend fun setSelectedApp(app: String)
}
