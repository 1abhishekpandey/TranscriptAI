package com.abhishek.transcriptai.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.abhishek.transcriptai.data.local.database.dao.PromptDao
import com.abhishek.transcriptai.data.local.database.entity.PromptEntity
import com.abhishek.transcriptai.data.seed.DefaultPromptsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [PromptEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add lastSelectedAt column to prompts table
                db.execSQL("ALTER TABLE prompts ADD COLUMN lastSelectedAt INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "summary_ai_database"
                )
                    .addCallback(DatabaseCallback())
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.promptDao())
                    }
                }
            }

            private suspend fun populateDatabase(promptDao: PromptDao) {
                // Seed default prompts on first launch
                val defaultPrompts = DefaultPromptsProvider.getDefaultPrompts()
                defaultPrompts.forEach { prompt ->
                    promptDao.insertPrompt(prompt)
                }
            }
        }
    }
}
