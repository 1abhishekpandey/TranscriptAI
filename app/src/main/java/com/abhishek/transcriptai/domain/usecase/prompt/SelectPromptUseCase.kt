package com.abhishek.transcriptai.domain.usecase.prompt

import com.abhishek.transcriptai.domain.repository.PromptRepository
import com.abhishek.transcriptai.domain.repository.SummariserConfigRepository

/**
 * Use case for making a prompt the selected one
 * The selection is shared by the transcript page, the settings page and the auto-share flow.
 * The prompt also gets a fresh "last selected" time, which keeps the most recently used prompts first in the list.
 *
 * @property promptRepository The prompt repository
 * @property configRepository The summariser config repository
 * @property clock Source of the current time in milliseconds
 */
class SelectPromptUseCase(
    private val promptRepository: PromptRepository,
    private val configRepository: SummariserConfigRepository,
    private val clock: () -> Long = System::currentTimeMillis
) {
    /**
     * Invoke the use case to select a prompt
     * @param promptId The ID of the prompt to select
     */
    suspend operator fun invoke(promptId: String) {
        promptRepository.updateLastSelectedTimestamp(promptId, clock())
        configRepository.setSelectedPromptId(promptId)
    }
}
