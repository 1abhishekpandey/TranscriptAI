package com.abhishek.transcriptai.domain.model

/**
 * Represents the target application for sharing content.
 * Domain layer model with no Android dependencies.
 */
enum class ShareTarget(val packageName: String, val displayName: String) {
    CHATGPT("com.openai.chatgpt", "ChatGPT"),
    CLAUDE("com.anthropic.claude", "Claude"),
    CLIPBOARD("", "Clipboard") // Special case for clipboard-only
}
