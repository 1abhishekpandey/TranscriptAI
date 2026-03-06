package com.abhishek.transcriptai.presentation.navigation

/**
 * Navigation destinations for the AI Summarizer feature
 * Uses sealed class for type-safe navigation
 */
sealed class SummariserDestination(val route: String) {

    /**
     * Home screen - main entry point
     * Shows YouTube URL input and subtitle extraction
     */
    object Home : SummariserDestination("home")

    /**
     * Summariser screen - shows subtitle with AI summarization options
     * Supports two modes:
     * - Manual mode: videoId provided (subtitle loaded from cache)
     * - Auto-share mode: autoDownload=true (URL retrieved from repository)
     */
    object Summariser : SummariserDestination("summariser?videoId={videoId}&autoDownload={autoDownload}") {
        /**
         * Argument keys
         */
        const val ARG_VIDEO_ID = "videoId"
        const val ARG_AUTO_DOWNLOAD = "autoDownload"

        /**
         * Creates route for manual mode (subtitle already downloaded)
         */
        fun createRoute(videoId: String): String {
            return "summariser?videoId=$videoId&autoDownload=false"
        }

        /**
         * Creates route for auto-download mode (URL stored in repository)
         */
        fun createAutoDownloadRoute(): String {
            return "summariser?videoId=&autoDownload=true"
        }
    }

    /**
     * Prompt Editor screen - allows creating/editing custom prompts
     * Optional promptId query parameter (null for new prompt)
     */
    object PromptEditor : SummariserDestination("prompt_editor?promptId={promptId}") {
        /**
         * Creates route with optional promptId
         */
        fun createRouteWithPromptId(promptId: String?) =
            if (promptId != null) "prompt_editor?promptId=$promptId"
            else "prompt_editor"

        /**
         * Argument key for prompt ID
         */
        const val ARG_PROMPT_ID = "promptId"
    }

    /**
     * Version Input screen - allows user to provide a newer YouTube client version
     * Shown when YouTube rejects all known client versions
     */
    object VersionInput : SummariserDestination("version_input")
}
