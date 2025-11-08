package com.abhishek.transcriptai.domain.model

/**
 * Domain model representing the AI summariser configuration
 *
 * @property isAiSummariserEnabled Whether the AI summariser feature is enabled
 * @property selectedPromptId The ID of the currently selected prompt (null if none selected)
 */
data class SummariserConfig(
    val isAiSummariserEnabled: Boolean,
    val selectedPromptId: String?
)
