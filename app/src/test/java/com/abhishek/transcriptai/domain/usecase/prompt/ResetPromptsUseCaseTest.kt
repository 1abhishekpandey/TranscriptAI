package com.abhishek.transcriptai.domain.usecase.prompt

import com.abhishek.transcriptai.domain.model.Prompt
import com.abhishek.transcriptai.domain.model.Result
import com.abhishek.transcriptai.domain.model.SummariserConfig
import com.abhishek.transcriptai.domain.repository.PromptRepository
import com.abhishek.transcriptai.domain.repository.SummariserConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResetPromptsUseCaseTest {

    private fun prompt(id: String) = Prompt(
        id = id,
        text = "Default $id",
        createdAt = 0L,
        lastModified = 0L,
        isDefault = true
    )

    /** Records every write, in order, so the test can check what was reset and what was selected */
    private val calls = mutableListOf<String>()

    private class FakePromptRepository(
        private val calls: MutableList<String>,
        private val defaults: List<Prompt>,
        private val failure: Exception? = null
    ) : PromptRepository {
        override fun getPrompts(): Flow<List<Prompt>> = emptyFlow()
        override suspend fun getPromptById(id: String): Prompt? = null
        override suspend fun savePrompt(prompt: Prompt) = Unit
        override suspend fun deletePrompt(id: String) = Unit
        override suspend fun updateLastSelectedTimestamp(promptId: String, timestamp: Long) {
            calls += "timestamp:$promptId"
        }
        override suspend fun resetToDefaultPrompts(): List<Prompt> {
            failure?.let { throw it }
            calls += "reset"
            return defaults
        }
        override fun getDefaultPromptCount(): Int = defaults.size
    }

    private val configRepository = object : SummariserConfigRepository {
        override fun getConfig(): Flow<SummariserConfig> = emptyFlow()
        override suspend fun updateConfig(config: SummariserConfig) = Unit
        override suspend fun toggleAiSummariser(enabled: Boolean) = Unit
        override suspend fun setSelectedPromptId(promptId: String) {
            calls += "selected:$promptId"
        }
    }

    private fun useCase(promptRepository: PromptRepository) =
        ResetPromptsUseCase(promptRepository, SelectPromptUseCase(promptRepository, configRepository))

    @Test
    fun invoke_replacesThePromptsThenSelectsTheFirstDefault() = runBlocking {
        val repository = FakePromptRepository(calls, listOf(prompt("d1"), prompt("d2"), prompt("d3")))

        val result = useCase(repository)()

        assertEquals(Result.Success(3), result)
        assertEquals(listOf("reset", "timestamp:d1", "selected:d1"), calls)
    }

    @Test
    fun invoke_returnsTheErrorAndSelectsNothingWhenTheResetFails() = runBlocking {
        val repository = FakePromptRepository(calls, listOf(prompt("d1")), failure = IllegalStateException("disk full"))

        val result = useCase(repository)()

        assertEquals("disk full", (result as Result.Error).message)
        assertTrue(calls.isEmpty())
    }

    @Test
    fun defaultPromptCount_isTheNumberOfDefaultsTheRepositoryShips() {
        val repository = FakePromptRepository(calls, listOf(prompt("d1"), prompt("d2")))

        assertEquals(2, useCase(repository).defaultPromptCount())
    }
}
