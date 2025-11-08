package com.abhishek.transcriptai.presentation.summariser

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abhishek.transcriptai.presentation.summariser.components.*
import com.abhishek.transcriptai.util.clipboard.ClipboardHelper
import com.abhishek.transcriptai.util.share.ShareHelper

/**
 * Summariser Screen - Main screen for AI summarizer feature
 *
 * Displays subtitle text with AI summarization options:
 * - AI toggle switch
 * - Prompt selector dropdown
 * - Scrollable subtitle text
 * - Share button with multiple options
 *
 * @param videoId The YouTube video ID
 * @param onNavigateToPromptEditor Callback to navigate to prompt editor
 * @param onNavigateBack Callback to navigate back
 * @param modifier Optional modifier for customization
 * @param viewModel The ViewModel (injected via Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummariserScreen(
    videoId: String,
    onNavigateToPromptEditor: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SummariserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Set callbacks
    LaunchedEffect(Unit) {
        viewModel.onNavigateToPromptEditor = onNavigateToPromptEditor
        viewModel.onCopyToClipboard = { text ->
            ClipboardHelper.copyToClipboard(context, text, "AI Summarizer")
        }
        viewModel.onShareToApp = { text, packageName ->
            ShareHelper.shareToApp(context, text, packageName)
        }
        viewModel.loadSubtitle(videoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Summarizer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.subtitleResult != null) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.onEvent(SummariserUiEvent.ShareButtonClicked) },
                    icon = {
                        Icon(Icons.Default.Share, contentDescription = null)
                    },
                    text = { Text("Share") }
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
            }

            uiState.subtitleResult != null -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // AI Toggle Section
                    AiToggleSection(
                        isEnabled = uiState.isAiSummariserEnabled,
                        onToggle = { viewModel.onEvent(SummariserUiEvent.ToggleAiSummariser(it)) }
                    )

                    // Prompt Selector Section (only if AI is enabled)
                    if (uiState.isAiSummariserEnabled) {
                        PromptSelectorSection(
                            prompts = uiState.prompts,
                            selectedPrompt = uiState.selectedPrompt,
                            onSelectPrompt = { viewModel.onEvent(SummariserUiEvent.SelectPrompt(it)) },
                            onEditClick = { viewModel.onEvent(SummariserUiEvent.NavigateToPromptEditor) }
                        )
                    }

                    // Subtitle Display
                    SubtitleDisplaySection(
                        subtitle = uiState.subtitleResult?.text ?: "",
                        videoTitle = uiState.subtitleResult?.videoTitle
                    )
                }
            }
        }

        // Share Bottom Sheet
        if (uiState.showShareSheet) {
            ShareBottomSheet(
                isAiEnabled = uiState.isAiSummariserEnabled,
                onDismiss = { viewModel.onEvent(SummariserUiEvent.DismissShareSheet) },
                onOptionSelected = { option ->
                    viewModel.onEvent(SummariserUiEvent.ShareOptionSelected(option))
                }
            )
        }
    }
}
