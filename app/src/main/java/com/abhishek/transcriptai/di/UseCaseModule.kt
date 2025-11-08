package com.abhishek.transcriptai.di

import com.abhishek.transcriptai.domain.repository.PromptRepository
import com.abhishek.transcriptai.domain.repository.SummariserConfigRepository
import com.abhishek.transcriptai.domain.usecase.config.GetSummariserConfigUseCase
import com.abhishek.transcriptai.domain.usecase.config.ToggleAiSummariserUseCase
import com.abhishek.transcriptai.domain.usecase.config.UpdateSummariserConfigUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.DeletePromptUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.GetPromptByIdUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.GetPromptsUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.SavePromptUseCase
import com.abhishek.transcriptai.domain.usecase.prompt.ValidatePromptUseCase
import com.abhishek.transcriptai.domain.usecase.subtitle.FormatSubtitleForCopyUseCase
import com.abhishek.transcriptai.util.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Hilt module for providing use cases
 * Use cases are scoped to ViewModel lifecycle
 */
@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    // Prompt Use Cases

    /**
     * Provides GetPromptsUseCase
     * Use case for retrieving all prompts
     */
    @Provides
    @ViewModelScoped
    fun provideGetPromptsUseCase(
        repository: PromptRepository
    ): GetPromptsUseCase {
        Logger.logD("UseCaseModule: Providing GetPromptsUseCase")
        return GetPromptsUseCase(repository)
    }

    /**
     * Provides GetPromptByIdUseCase
     * Use case for retrieving a single prompt by ID
     */
    @Provides
    @ViewModelScoped
    fun provideGetPromptByIdUseCase(
        repository: PromptRepository
    ): GetPromptByIdUseCase {
        Logger.logD("UseCaseModule: Providing GetPromptByIdUseCase")
        return GetPromptByIdUseCase(repository)
    }

    /**
     * Provides SavePromptUseCase
     * Use case for saving/updating a prompt
     */
    @Provides
    @ViewModelScoped
    fun provideSavePromptUseCase(
        repository: PromptRepository
    ): SavePromptUseCase {
        Logger.logD("UseCaseModule: Providing SavePromptUseCase")
        return SavePromptUseCase(repository)
    }

    /**
     * Provides DeletePromptUseCase
     * Use case for deleting a prompt
     */
    @Provides
    @ViewModelScoped
    fun provideDeletePromptUseCase(
        repository: PromptRepository
    ): DeletePromptUseCase {
        Logger.logD("UseCaseModule: Providing DeletePromptUseCase")
        return DeletePromptUseCase(repository)
    }

    /**
     * Provides ValidatePromptUseCase
     * Use case for validating prompt text
     * Note: This use case has no dependencies
     */
    @Provides
    @ViewModelScoped
    fun provideValidatePromptUseCase(): ValidatePromptUseCase {
        Logger.logD("UseCaseModule: Providing ValidatePromptUseCase")
        return ValidatePromptUseCase()
    }

    // Config Use Cases

    /**
     * Provides GetSummariserConfigUseCase
     * Use case for retrieving AI summariser configuration
     */
    @Provides
    @ViewModelScoped
    fun provideGetSummariserConfigUseCase(
        repository: SummariserConfigRepository
    ): GetSummariserConfigUseCase {
        Logger.logD("UseCaseModule: Providing GetSummariserConfigUseCase")
        return GetSummariserConfigUseCase(repository)
    }

    /**
     * Provides UpdateSummariserConfigUseCase
     * Use case for updating AI summariser configuration
     */
    @Provides
    @ViewModelScoped
    fun provideUpdateSummariserConfigUseCase(
        repository: SummariserConfigRepository
    ): UpdateSummariserConfigUseCase {
        Logger.logD("UseCaseModule: Providing UpdateSummariserConfigUseCase")
        return UpdateSummariserConfigUseCase(repository)
    }

    /**
     * Provides ToggleAiSummariserUseCase
     * Use case for toggling AI summariser on/off
     */
    @Provides
    @ViewModelScoped
    fun provideToggleAiSummariserUseCase(
        repository: SummariserConfigRepository
    ): ToggleAiSummariserUseCase {
        Logger.logD("UseCaseModule: Providing ToggleAiSummariserUseCase")
        return ToggleAiSummariserUseCase(repository)
    }

    // Subtitle Use Cases

    /**
     * Provides FormatSubtitleForCopyUseCase
     * Use case for formatting subtitle text with AI prompt before copying
     */
    @Provides
    @ViewModelScoped
    fun provideFormatSubtitleForCopyUseCase(
        promptRepository: PromptRepository,
        configRepository: SummariserConfigRepository
    ): FormatSubtitleForCopyUseCase {
        Logger.logD("UseCaseModule: Providing FormatSubtitleForCopyUseCase")
        return FormatSubtitleForCopyUseCase(promptRepository, configRepository)
    }
}
