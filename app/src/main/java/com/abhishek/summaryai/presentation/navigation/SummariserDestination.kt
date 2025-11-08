package com.abhishek.summaryai.presentation.navigation

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
     * Requires videoId as path parameter
     */
    object Summariser : SummariserDestination("summariser/{videoId}") {
        /**
         * Creates route with actual videoId value
         */
        fun createRoute(videoId: String) = "summariser/$videoId"

        /**
         * Argument key for video ID
         */
        const val ARG_VIDEO_ID = "videoId"
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
}
