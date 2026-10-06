package com.abhishek.transcriptai.domain.usecase.prompt

import com.abhishek.transcriptai.domain.model.Prompt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PromptSelectionTest {

    private fun prompt(id: String) = Prompt(
        id = id,
        text = "Prompt $id",
        createdAt = 0L,
        lastModified = 0L,
        isDefault = false
    )

    private val prompts = listOf(prompt("a"), prompt("b"), prompt("c"))

    @Test
    fun resolveSelectedPromptId_keepsTheSavedSelectionWhenThatPromptExists() {
        assertEquals("b", resolveSelectedPromptId(prompts, "b"))
    }

    @Test
    fun resolveSelectedPromptId_fallsBackToTheFirstPromptWhenNothingIsSaved() {
        assertEquals("a", resolveSelectedPromptId(prompts, null))
    }

    @Test
    fun resolveSelectedPromptId_fallsBackToTheFirstPromptWhenTheSavedOneWasDeleted() {
        assertEquals("a", resolveSelectedPromptId(prompts, "gone"))
    }

    @Test
    fun resolveSelectedPromptId_isNullWithoutPrompts() {
        assertNull(resolveSelectedPromptId(emptyList(), "b"))
        assertNull(resolveSelectedPromptId(emptyList(), null))
    }

    @Test
    fun nextPromptIdAfterDelete_takesTheFirstRemainingPrompt() {
        assertEquals("b", nextPromptIdAfterDelete(prompts, "a"))
        assertEquals("a", nextPromptIdAfterDelete(prompts, "b"))
        assertEquals("a", nextPromptIdAfterDelete(prompts, "c"))
    }

    @Test
    fun nextPromptIdAfterDelete_isNullWhenTheDeletedPromptWasTheOnlyOne() {
        assertNull(nextPromptIdAfterDelete(listOf(prompt("a")), "a"))
    }
}
