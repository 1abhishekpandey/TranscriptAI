package com.abhishek.transcriptai.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SharedPreferences wrapper for the subtitle language choice.
 *
 * Keeps the selected option (auto-generated or a picked language)
 * and the language picked on the settings page across app restarts.
 */
@Singleton
class LanguagePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Gets the selected option.
     *
     * @return "AUTO" or "OTHER", or null if none saved yet
     */
    fun getMode(): String? = prefs.getString(KEY_MODE, null)

    /**
     * Gets the language picked on the settings page.
     *
     * @return language code (e.g., "id"), or null if none picked yet
     */
    fun getOtherLanguageCode(): String? = prefs.getString(KEY_OTHER_LANGUAGE, null)

    /**
     * Saves the selected option.
     */
    fun setMode(mode: String) {
        prefs.edit().putString(KEY_MODE, mode).apply()
    }

    /**
     * Saves a language from the settings page and selects it.
     */
    fun setOtherLanguage(code: String, mode: String) {
        prefs.edit()
            .putString(KEY_OTHER_LANGUAGE, code)
            .putString(KEY_MODE, mode)
            .apply()
    }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        private const val PREFS_NAME = "language_prefs"
        private const val KEY_MODE = "language_mode"
        private const val KEY_OTHER_LANGUAGE = "other_language"
    }
}
