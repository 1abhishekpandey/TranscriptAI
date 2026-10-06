package com.abhishek.transcriptai.presentation.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AiPromptsSummaryTest {

    @Test
    fun aiPromptsSummary_isOffWhenTheSwitchIsOffWhateverPromptIsSelected() {
        assertEquals("Off", aiPromptsSummary(isEnabled = false, selectedPromptText = "Summarize this"))
        assertEquals("Off", aiPromptsSummary(isEnabled = false, selectedPromptText = null))
    }

    @Test
    fun aiPromptsSummary_showsTheSelectedPromptWhenOn() {
        assertEquals(
            "On · Summarize the following transcript in bullet points:",
            aiPromptsSummary(isEnabled = true, selectedPromptText = "Summarize the following transcript in bullet points:")
        )
    }

    @Test
    fun aiPromptsSummary_putsAMultiLinePromptOnOneLine() {
        assertEquals(
            "On · First line second line third",
            aiPromptsSummary(isEnabled = true, selectedPromptText = "First line\n\nsecond   line\n third ")
        )
    }

    @Test
    fun aiPromptsSummary_isJustOnWithoutAUsablePrompt() {
        assertEquals("On", aiPromptsSummary(isEnabled = true, selectedPromptText = null))
        assertEquals("On", aiPromptsSummary(isEnabled = true, selectedPromptText = "  \n "))
    }
}
