package com.abhishek.transcriptai.domain.model

/**
 * Represents the target application for sharing content.
 * Domain layer model with no Android dependencies.
 *
 * To support a new AI app, add an entry here and its package to the
 * `<queries>` block in AndroidManifest.xml. The share sheet and the
 * auto-forward picker both list every app in [aiApps].
 *
 * @property id Stable identifier saved in preferences (auto-forward choice). Never change it.
 * @property packageName Android package of the app; empty for the clipboard.
 * @property displayName Name shown to the user.
 */
enum class ShareTarget(val id: String, val packageName: String, val displayName: String) {
    CHATGPT("chatgpt", "com.openai.chatgpt", "ChatGPT"),
    CLAUDE("claude", "com.anthropic.claude", "Claude"),
    DEEPSEEK("deepseek", "com.deepseek.chat", "DeepSeek"),
    GEMINI("gemini", "com.google.android.apps.bard", "Gemini"),
    CLIPBOARD("clipboard", "", "Clipboard"); // Special case for clipboard-only

    companion object {
        /** AI apps the transcript can be sent to, in the order they are shown. */
        val aiApps: List<ShareTarget> = entries.filter { it != CLIPBOARD }

        /** App used for auto-forward when nothing, or an unknown id, is saved. */
        val DEFAULT_AI_APP: ShareTarget = CHATGPT

        /** The AI app with this saved [id], or [DEFAULT_AI_APP] when none matches. */
        fun aiAppFromId(id: String?): ShareTarget =
            aiApps.firstOrNull { it.id == id } ?: DEFAULT_AI_APP
    }
}
