package com.abhishek.transcriptai.presentation.settings

/**
 * One line for the "AI prompts" row on the Settings screen: whether the prompt is on and which one is selected.
 * The prompt text is flattened to a single line; the row cuts it off to fit the screen.
 *
 * @param isEnabled Whether the AI prompt is put in front of the transcript
 * @param selectedPromptText Text of the selected prompt (null when there are no prompts)
 * @return "Off", "On", or "On · " followed by the prompt text
 */
fun aiPromptsSummary(isEnabled: Boolean, selectedPromptText: String?): String {
    if (!isEnabled) return "Off"
    val singleLine = selectedPromptText?.split(Regex("\\s+"))?.filter { it.isNotEmpty() }?.joinToString(" ")
    return if (singleLine.isNullOrEmpty()) "On" else "On · $singleLine"
}
