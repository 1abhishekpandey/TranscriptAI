package com.abhishek.transcriptai.data.repository

import com.abhishek.transcriptai.data.local.TranscriptPreferences
import com.abhishek.transcriptai.domain.repository.TranscriptDisplayRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Implementation of TranscriptDisplayRepository using SharedPreferences
 */
class TranscriptDisplayRepositoryImpl(
    private val preferences: TranscriptPreferences
) : TranscriptDisplayRepository {

    override fun showTimestamps(): Flow<Boolean> = preferences.showTimestamps

    override suspend fun setShowTimestamps(show: Boolean) {
        withContext(Dispatchers.IO) {
            preferences.setShowTimestamps(show)
        }
    }
}
