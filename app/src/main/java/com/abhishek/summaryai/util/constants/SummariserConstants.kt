package com.abhishek.summaryai.util.constants

/**
 * Constants for the AI Summarizer feature in the SummaryAI application.
 *
 * This object contains all constant values related to prompt validation,
 * default prompts, UI messages, clipboard labels, and navigation arguments.
 */
object SummariserConstants {

    // Prompt validation
    const val MIN_PROMPT_LENGTH = 10
    const val MAX_PROMPT_LENGTH = 500

    // Default prompts text
    val DEFAULT_PROMPTS = listOf(
        "Summarize the following transcript in bullet points:",
        "Extract key insights from this video transcript:",
        "Create a detailed summary with main topics:",
        "Translate and summarize this transcript:"
    )

    // UI messages
    const val MESSAGE_PROMPT_TOO_SHORT = "Prompt must be at least $MIN_PROMPT_LENGTH characters"
    const val MESSAGE_PROMPT_TOO_LONG = "Prompt must not exceed $MAX_PROMPT_LENGTH characters"
    const val MESSAGE_PROMPT_EMPTY = "Prompt cannot be empty"
    const val MESSAGE_PROMPT_DUPLICATE = "This prompt already exists"
    const val MESSAGE_CANNOT_DELETE_LAST = "Cannot delete the last prompt"
    const val MESSAGE_PROMPT_SAVED = "Prompt saved successfully"
    const val MESSAGE_PROMPT_DELETED = "Prompt deleted"
    const val MESSAGE_UNSAVED_CHANGES = "You have unsaved changes"

    // Clipboard
    const val CLIPBOARD_LABEL_SUBTITLE = "Subtitle"
    const val CLIPBOARD_LABEL_FORMATTED = "AI Summarizer"

    // Navigation
    const val NAV_ARG_VIDEO_ID = "videoId"
    const val NAV_ARG_PROMPT_ID = "promptId"
}
