package com.abhishek.summaryai.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val createdAt: Long,
    val lastModified: Long,
    val isDefault: Boolean,
    val lastSelectedAt: Long? = null
)
