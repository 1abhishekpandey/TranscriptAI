package com.abhishek.summaryai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.abhishek.summaryai.presentation.home.HomeScreen
import com.abhishek.summaryai.presentation.prompteditor.PromptEditorScreen
import com.abhishek.summaryai.util.Logger

/**
 * Navigation graph for the AI Summarizer feature
 *
 * Defines navigation routes and screen composables
 * Handles navigation between Home, Summariser, and Prompt Editor screens
 *
 * @param navController The navigation controller for managing navigation
 * @param modifier Modifier for the NavHost
 * @param startDestination Starting destination (defaults to Home)
 * @param initialUrl Initial YouTube URL from deep link (optional)
 */
@Composable
fun SummariserNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = SummariserDestination.Home.route,
    initialUrl: String? = null
) {
    Logger.logD("SummariserNavGraph: Setting up navigation with start destination: $startDestination")
    if (initialUrl != null) {
        Logger.logI("SummariserNavGraph: Initial URL from deep link: $initialUrl")
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Home Screen - Entry point for subtitle extraction
        composable(route = SummariserDestination.Home.route) {
            Logger.logV("SummariserNavGraph: Navigated to Home screen")

            HomeScreen(
                initialUrl = initialUrl,
                onNavigateToSummariser = { videoId ->
                    Logger.logI("SummariserNavGraph: Navigating from Home to Summariser with videoId: $videoId")
                    navController.navigate(SummariserDestination.Summariser.createRoute(videoId))
                }
            )
        }

        // Summariser Screen - Shows subtitle with AI summarization options
        composable(
            route = SummariserDestination.Summariser.route,
            arguments = listOf(
                navArgument(SummariserDestination.Summariser.ARG_VIDEO_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString(SummariserDestination.Summariser.ARG_VIDEO_ID) ?: ""
            Logger.logV("SummariserNavGraph: Navigated to Summariser screen with videoId: $videoId")

            com.abhishek.summaryai.presentation.summariser.SummariserScreen(
                videoId = videoId,
                onNavigateToPromptEditor = {
                    Logger.logI("SummariserNavGraph: Navigating from Summariser to PromptEditor")
                    navController.navigate(SummariserDestination.PromptEditor.route)
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
    }
}
