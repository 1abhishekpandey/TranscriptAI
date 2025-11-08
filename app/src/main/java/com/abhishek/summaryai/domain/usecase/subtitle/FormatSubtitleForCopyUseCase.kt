package com.abhishek.summaryai.domain.usecase.subtitle

import com.abhishek.summaryai.domain.repository.PromptRepository
import com.abhishek.summaryai.domain.repository.SummariserConfigRepository
import com.abhishek.summaryai.util.Logger
import kotlinx.coroutines.flow.first

/**
 * Use case for formatting subtitle text with AI prompt before copying
 * This use case prepends the selected AI prompt to the subtitle text if AI summariser is enabled
 *
 * @property promptRepository The prompt repository
 * @property configRepository The summariser config repository
 */
class FormatSubtitleForCopyUseCase(
    private val promptRepository: PromptRepository,
    private val configRepository: SummariserConfigRepository
) {
    /**
     * Invoke the use case to format subtitle text
     * @param subtitleText The raw subtitle text
     * @return Formatted text with AI prompt prepended if enabled, otherwise raw text
     */
    suspend operator fun invoke(subtitleText: String): String {
        val config = configRepository.getConfig().first()

        Logger.logD("FormatSubtitleForCopyUseCase: AI enabled=${config.isAiSummariserEnabled}, selectedPromptId=${config.selectedPromptId}")

        return if (config.isAiSummariserEnabled && config.selectedPromptId != null) {
            val prompt = promptRepository.getPromptById(config.selectedPromptId)
            Logger.logD("FormatSubtitleForCopyUseCase: Found prompt ID=${prompt?.id}, text length=${prompt?.text?.length}")
            if (prompt != null) {
                val formatted = "${prompt.text}\n\n$subtitleText"
                Logger.logI("FormatSubtitleForCopyUseCase: Formatted text with prompt. Total length=${formatted.length}, prompt length=${prompt.text.length}, subtitle length=${subtitleText.length}")
                formatted
            } else {
                Logger.logW("FormatSubtitleForCopyUseCase: Prompt not found for ID=${config.selectedPromptId}, returning raw subtitle")
                subtitleText
            }
        } else {
            Logger.logI("FormatSubtitleForCopyUseCase: AI disabled or no prompt selected, returning raw subtitle")
            subtitleText
        }
    }
}
