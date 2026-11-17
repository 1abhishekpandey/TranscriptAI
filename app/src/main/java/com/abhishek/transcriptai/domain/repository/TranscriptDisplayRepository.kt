package com.abhishek.transcriptai.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * How the transcript is shown on screen and sent to other apps
 */
interface TranscriptDisplayRepository {
    /**
     * Whether each caption line keeps its time code, on screen and in what is copied or shared.
     * Emits the saved value first, then every change.
     */
    fun showTimestamps(): Flow<Boolean>

    /**
     * Save whether time codes are kept
     */
    suspend fun setShowTimestamps(show: Boolean)
}
