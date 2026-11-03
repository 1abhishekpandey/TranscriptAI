package com.abhishek.transcriptai.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ShareTargetTest {

    @Test
    fun `ai apps list every app in display order and leave out the clipboard`() {
        assertEquals(
            listOf(ShareTarget.CHATGPT, ShareTarget.CLAUDE, ShareTarget.DEEPSEEK, ShareTarget.GEMINI),
            ShareTarget.aiApps
        )
        assertFalse(ShareTarget.aiApps.contains(ShareTarget.CLIPBOARD))
    }

    @Test
    fun `saved ids map back to their app`() {
        assertEquals(ShareTarget.CHATGPT, ShareTarget.aiAppFromId("chatgpt"))
        assertEquals(ShareTarget.CLAUDE, ShareTarget.aiAppFromId("claude"))
        assertEquals(ShareTarget.DEEPSEEK, ShareTarget.aiAppFromId("deepseek"))
        assertEquals(ShareTarget.GEMINI, ShareTarget.aiAppFromId("gemini"))
    }

    @Test
    fun `unknown, missing or clipboard id falls back to the default app`() {
        assertEquals(ShareTarget.DEFAULT_AI_APP, ShareTarget.aiAppFromId("perplexity"))
        assertEquals(ShareTarget.DEFAULT_AI_APP, ShareTarget.aiAppFromId(null))
        assertEquals(ShareTarget.DEFAULT_AI_APP, ShareTarget.aiAppFromId("clipboard"))
    }

    @Test
    fun `ids are unique so a saved choice is never ambiguous`() {
        val ids = ShareTarget.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
