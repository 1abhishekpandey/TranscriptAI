package com.abhishek.transcriptai.presentation.aiprompts

/**
 * One-time actions the ViewModel asks the AI prompts screen to carry out.
 * Unlike [AiPromptsUiState], an effect is delivered once and then forgotten.
 */
sealed class AiPromptsUiEffect {
    /**
     * Show a short message in the snackbar
     * @param message Text to show
     */
    data class ShowMessage(val message: String) : AiPromptsUiEffect()
}
