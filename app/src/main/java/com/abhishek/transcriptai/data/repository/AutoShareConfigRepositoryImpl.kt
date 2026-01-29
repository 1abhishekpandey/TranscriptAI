package com.abhishek.transcriptai.data.repository

import com.abhishek.transcriptai.data.local.AutoSharePreferences
import com.abhishek.transcriptai.domain.repository.AutoShareConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AutoShareConfigRepository using SharedPreferences.
 *
 * Handles data layer operations for auto-share configuration, ensuring
 * all preference access happens on the IO dispatcher for optimal performance.
 */
@Singleton
class AutoShareConfigRepositoryImpl @Inject constructor(
    private val preferences: AutoSharePreferences
) : AutoShareConfigRepository {

    override suspend fun isAutoShareEnabled(): Boolean =
        withContext(Dispatchers.IO) {
            preferences.isAutoShareEnabled()
        }

    override suspend fun setAutoShareEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            preferences.setAutoShareEnabled(enabled)
        }
    }
}
