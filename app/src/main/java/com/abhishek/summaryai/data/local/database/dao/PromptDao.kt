package com.abhishek.summaryai.data.local.database.dao

import androidx.room.*
import com.abhishek.summaryai.data.local.database.entity.PromptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY createdAt DESC")
    fun getAllPrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE id = :id")
    suspend fun getPromptById(id: String): PromptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptEntity)

    @Update
    suspend fun updatePrompt(prompt: PromptEntity)

    @Query("DELETE FROM prompts WHERE id = :id")
    suspend fun deletePrompt(id: String)

    @Query("SELECT COUNT(*) FROM prompts")
    suspend fun getPromptCount(): Int
}
