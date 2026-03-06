package com.abhishek.transcriptai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.abhishek.transcriptai.data.repository.SubtitleCacheRepository
import com.abhishek.transcriptai.presentation.home.HomeScreen
import com.abhishek.transcriptai.presentation.prompteditor.PromptEditorScreen
import com.abhishek.transcriptai.presentation.versioninput.VersionInputScreen
import com.abhishek.transcriptai.util.Logger

/**
 * Navigation graph for the AI Summarizer feature
 *
 * Defines navigation routes and screen composables
 * Handles navigation between Home, Summariser, and Prompt Editor screens
 * Supports conditional routing based on auto-share preference
 *
 * @param navController The navigation controller for managing navigation
 * @param modifier Modifier for the NavHost
 * @param initialUrl Initial YouTube URL from deep link (optional)
 * @param isFromShareIntent Whether the app was launched via share intent
 * @param autoShareEnabled Whether auto-share feature is enabled
 * @param subtitleCacheRepository Repository for caching URLs
 */
@Composable
fun SummariserNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    initialUrl: String? = null,
    isFromShareIntent: Boolean = false,
    autoShareEnabled: Boolean = false,
    subtitleCacheRepository: SubtitleCacheRepository
) {
    // Auto-navigate to Summariser if auto-share is enabled
    LaunchedEffect(isFromShareIntent, autoShareEnabled, initialUrl) {
        if (isFromShareIntent && autoShareEnabled && initialUrl != null) {
            Logger.logI("SummariserNavGraph: Auto-share mode - storing URL and navigating: $initialUrl")
            // Store URL in repository
            subtitleCacheRepository.setPendingUrl(initialUrl, autoShare = true)

            // Navigate to Summariser with auto-download flag
            navController.navigate(SummariserDestination.Summariser.createAutoDownloadRoute()) {
                // Pop everything off the back stack
                popUpTo(SummariserDestination.Home.route) { inclusive = false }
            }
        }
    }

    Logger.logI("SummariserNavGraph: autoShare=$autoShareEnabled, isFromShareIntent=$isFromShareIntent")
    if (initialUrl != null) {
        Logger.logI("SummariserNavGraph: Initial URL from deep link: $initialUrl")
    }

    NavHost(
        navController = navController,
        startDestination = SummariserDestination.Home.route,
        modifier = modifier
    ) {
        // Home Screen - Entry point for subtitle extraction (manual flow)
        composable(route = SummariserDestination.Home.route) {
            Logger.logV("SummariserNavGraph: Navigated to Home screen")

            HomeScreen(
                initialUrl = if (!autoShareEnabled) initialUrl else null,
                onNavigateToSummariser = { videoId ->
                    Logger.logI("SummariserNavGraph: Navigating from Home to Summariser with videoId: $videoId")
                    navController.navigate(SummariserDestination.Summariser.createRoute(videoId))
                },
                onNavigateToVersionInput = {
                    Logger.logI("SummariserNavGraph: Navigating from Home to VersionInput")
                    navController.navigate(SummariserDestination.VersionInput.route)
                }
            )
        }

        // Summariser Screen - Shows subtitle with AI summarization options
        // Supports both auto-download (from repository) and manual (from cache) modes
        composable(
            route = SummariserDestination.Summariser.route,
            arguments = listOf(
                navArgument(SummariserDestination.Summariser.ARG_VIDEO_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(SummariserDestination.Summariser.ARG_AUTO_DOWNLOAD) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString(SummariserDestination.Summariser.ARG_VIDEO_ID) ?: ""
            val autoDownload = backStackEntry.arguments?.getBoolean(SummariserDestination.Summariser.ARG_AUTO_DOWNLOAD) ?: false

            Logger.logV("SummariserNavGraph: Navigated to Summariser screen - videoId: $videoId, autoDownload: $autoDownload")

            com.abhishek.transcriptai.presentation.summariser.SummariserScreen(
                videoId = videoId,
                autoDownload = autoDownload,
                onNavigateToPromptEditor = {
                    Logger.logI("SummariserNavGraph: Navigating from Summariser to PromptEditor")
                    navController.navigate(SummariserDestination.PromptEditor.createRouteWithPromptId(null))
                },
                onNavigateBack = {
                    Logger.logI("SummariserNavGraph: Navigating back from Summariser")
                    navController.popBackStack()
                }
            )
        }

        // Prompt Editor Screen - Create/edit custom prompts
        composable(
            route = SummariserDestination.PromptEditor.route,
            arguments = listOf(
                navArgument(SummariserDestination.PromptEditor.ARG_PROMPT_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val promptId = backStackEntry.arguments?.getString(SummariserDestination.PromptEditor.ARG_PROMPT_ID)
            Logger.logV("SummariserNavGraph: Navigated to PromptEditor screen with promptId: $promptId")

            PromptEditorScreen(
                promptId = promptId,
                onNavigateBack = {
                    Logger.logI("SummariserNavGraph: Navigating back from PromptEditor")
                    navController.popBackStack()
                }
            )
        }

        // Version Input Screen - Fix outdated YouTube client version
        composable(route = SummariserDestination.VersionInput.route) {
            Logger.logV("SummariserNavGraph: Navigated to VersionInput screen")
            VersionInputScreen(
                onNavigateBack = {
                    Logger.logI("SummariserNavGraph: Navigating back from VersionInput")
                    navController.popBackStack()
                }
            )
        }
    }
}
