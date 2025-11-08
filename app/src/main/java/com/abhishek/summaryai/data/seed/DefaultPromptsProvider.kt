package com.abhishek.summaryai.data.seed

import com.abhishek.summaryai.data.local.database.entity.PromptEntity
import java.util.UUID

object DefaultPromptsProvider {
    fun getDefaultPrompts(): List<PromptEntity> {
        val currentTime = System.currentTimeMillis()
        return listOf(
            PromptEntity(
                id = UUID.randomUUID().toString(),
                text = "Summarize the following transcript in bullet points:",
                createdAt = currentTime,
                lastModified = currentTime,
                isDefault = true
            ),
            PromptEntity(
                id = UUID.randomUUID().toString(),
                text = "Extract key insights from this video transcript:",
                createdAt = currentTime,
                lastModified = currentTime,
                isDefault = true
            ),
            PromptEntity(
                id = UUID.randomUUID().toString(),
                text = "Create a detailed summary with main topics:",
                createdAt = currentTime,
                lastModified = currentTime,
                isDefault = true
            ),
            PromptEntity(
                id = UUID.randomUUID().toString(),
                text = "Translate and summarize this transcript:",
                createdAt = currentTime,
                lastModified = currentTime,
                isDefault = true
            )
        )
    }
}
