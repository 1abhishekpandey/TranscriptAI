package com.abhishek.transcriptai.domain.usecase.prompt

import com.abhishek.transcriptai.domain.model.Prompt
import com.abhishek.transcriptai.domain.model.SummariserConfig
import com.abhishek.transcriptai.domain.repository.PromptRepository
import com.abhishek.transcriptai.domain.repository.SummariserConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SelectPromptUseCaseTest {

    /** Records every call, in order, so the test can check what was written and when */
    private val calls = mutableListOf<String>()

    private val promptRepository = object : PromptRepository {
        override fun getPrompts(): Flow<List<Prompt>> = emptyFlow()
        override suspend fun getPromptById(id: String): Prompt? = null
        override suspend fun savePrompt(prompt: Prompt) = Unit
        override suspend fun deletePrompt(id: String) = Unit
        override suspend fun updateLastSelectedTimestamp(promptId: String, timestamp: Long) {
            calls += "timestamp:$promptId:$timestamp"
        }
        override suspend fun resetToDefaultPrompts(): List<Prompt> = emptyList()
        override fun getDefaultPromptCount(): Int = 0
    }

    private val configRepository = object : SummariserConfigRepository {
        override fun getConfig(): Flow<SummariserConfig> = emptyFlow()
        override suspend fun updateConfig(config: SummariserConfig) = Unit
        override suspend fun toggleAiSummariser(enabled: Boolean) = Unit
        override suspend fun setSelectedPromptId(promptId: String) {
            calls += "selected:$promptId"
        }
    }

    @Test
    fun invoke_stampsThePromptAndSavesItAsTheSelection() = runBlocking {
        val useCase = SelectPromptUseCase(promptRepository, configRepository, clock = { 1234L })

        useCase("p1")

        assertEquals(listOf("timestamp:p1:1234", "selected:p1"), calls)
    }
}
