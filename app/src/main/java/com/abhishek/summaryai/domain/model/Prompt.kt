package com.abhishek.summaryai.domain.model

/**
 * Domain model representing a prompt for AI summarization
 *
 * @property id Unique identifier for the prompt
 * @property text The actual prompt text
 * @property createdAt Timestamp when the prompt was created
 * @property lastModified Timestamp when the prompt was last modified
 * @property isDefault Whether this is a default/system prompt
 * @property lastSelectedAt Timestamp when the prompt was last selected (null if never selected)
 */
data class Prompt(
    val id: String,
    val text: String,
    val createdAt: Long,
    val lastModified: Long,
    val isDefault: Boolean,
    val lastSelectedAt: Long? = null
)
