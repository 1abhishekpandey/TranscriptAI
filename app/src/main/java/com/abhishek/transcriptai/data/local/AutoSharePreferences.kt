package com.abhishek.transcriptai.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SharedPreferences wrapper for auto-share configuration.
 *
 * Stores user preference for automatic transcript sharing functionality.
 * Uses SharedPreferences for lightweight, persistent storage of the toggle state.
 */
@Singleton
class AutoSharePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Checks if auto-share feature is enabled.
     *
     * @return true if enabled, false by default
     */
    fun isAutoShareEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_SHARE_ENABLED, false)

    /**
     * Updates the auto-share enabled state.
     *
     * @param enabled true to enable, false to disable
     */
    fun setAutoShareEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SHARE_ENABLED, enabled).apply()
    }

    /**
     * Gets the selected app for auto-sharing.
     *
     * @return app identifier ("chatgpt" or "claude"), defaults to "chatgpt"
     */
    fun getSelectedApp(): String = prefs.getString(KEY_SELECTED_APP, "chatgpt") ?: "chatgpt"

    /**
     * Sets the selected app for auto-sharing.
     *
     * @param app app identifier ("chatgpt" or "claude")
     */
    fun setSelectedApp(app: String) {
        prefs.edit().putString(KEY_SELECTED_APP, app).apply()
    }

    companion object {
        private const val PREFS_NAME = "auto_share_prefs"
        private const val KEY_AUTO_SHARE_ENABLED = "auto_share_enabled"
        private const val KEY_SELECTED_APP = "key_selected_app"
    }
}
