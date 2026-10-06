package com.abhishek.transcriptai.data.repository

import android.content.SharedPreferences
import com.abhishek.transcriptai.data.local.LanguagePreferences
import com.abhishek.transcriptai.domain.model.LanguageChoice
import com.abhishek.transcriptai.domain.model.LanguageMode
import com.abhishek.transcriptai.domain.repository.LanguagePreferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LanguagePreferenceRepository using SharedPreferences.
 *
 * All preference access happens on the IO dispatcher.
 */
@Singleton
class LanguagePreferenceRepositoryImpl @Inject constructor(
    private val preferences: LanguagePreferences
) : LanguagePreferenceRepository {

    override fun observeLanguageChoice(): Flow<LanguageChoice> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(readChoice())
        }
        preferences.registerListener(listener)
        send(readChoice())
        awaitClose { preferences.unregisterListener(listener) }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    override suspend fun getLanguageChoice(): LanguageChoice =
        withContext(Dispatchers.IO) {
            readChoice()
        }

    override suspend fun setLanguageMode(mode: LanguageMode) {
        withContext(Dispatchers.IO) {
            preferences.setMode(mode.name)
        }
    }

    override suspend fun setOtherLanguage(code: String) {
        withContext(Dispatchers.IO) {
            preferences.setOtherLanguage(code, LanguageMode.OTHER.name)
        }
    }

    private fun readChoice(): LanguageChoice {
        val mode = LanguageMode.values().firstOrNull { it.name == preferences.getMode() }
            ?: LanguageMode.AUTO
        return LanguageChoice(mode = mode, otherLanguageCode = preferences.getOtherLanguageCode())
    }
}
