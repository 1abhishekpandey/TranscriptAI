package com.abhishek.transcriptai.presentation.summariser

/**
 * One-time actions the ViewModel asks the Summariser screen to carry out.
 * Unlike [SummariserUiState], an effect is delivered once and then forgotten.
 */
sealed class SummariserUiEffect {
    /**
     * Show a short message in the snackbar
     * @param message Text to show
     */
    data class ShowMessage(val message: String) : SummariserUiEffect()

    /**
     * Put text on the system clipboard, then show a confirmation in the snackbar
     * @param text Text to copy
     * @param label Label stored with the clipboard entry
     * @param confirmation Message to show once the text is copied
     */
    data class CopyToClipboard(
        val text: String,
        val label: String,
        val confirmation: String
    ) : SummariserUiEffect()
}
