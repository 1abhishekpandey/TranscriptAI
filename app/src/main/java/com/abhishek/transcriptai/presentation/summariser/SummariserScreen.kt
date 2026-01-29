package com.abhishek.transcriptai.presentation.summariser

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abhishek.transcriptai.presentation.summariser.components.*
import kotlinx.coroutines.delay

/**
 * Summariser Screen - Main screen for AI summarizer feature
 *
 * Displays subtitle text with AI summarization options:
 * - AI toggle switch
 * - Prompt selector dropdown
 * - Scrollable subtitle text
 * - Share button with multiple options
 *
 * Supports two modes:
 * - Manual mode: videoId provided (loads from cache)
 * - Auto-download mode: autoDownload=true (downloads from URL in repository)
 *
 * @param videoId The YouTube video ID (manual mode)
 * @param autoDownload Whether to auto-download from repository
 * @param onNavigateToPromptEditor Callback to navigate to prompt editor
 * @param onNavigateBack Callback to navigate back
 * @param modifier Optional modifier for customization
 * @param viewModel The ViewModel (injected via Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummariserScreen(
    videoId: String = "",
    autoDownload: Boolean = false,
    onNavigateToPromptEditor: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SummariserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Set navigation callback
    LaunchedEffect(Unit) {
        viewModel.onNavigateToPromptEditor = onNavigateToPromptEditor
    }

    // Set finish activity callback (for closing app after auto-share)
    LaunchedEffect(Unit) {
        viewModel.onFinishActivity = {
            (context as? Activity)?.finish()
        }
    }

    // Handle two initialization modes
    LaunchedEffect(videoId, autoDownload) {
        if (autoDownload) {
            // Auto-download mode - consume pending URL from repository
            viewModel.initializeWithAutoDownloadFromRepository()
        } else if (videoId.isNotEmpty()) {
            // Manual mode - load from cache
            viewModel.loadSubtitle(videoId)
        }
    }

    // Trigger auto-share when subtitle ready
    LaunchedEffect(uiState.subtitleResult, uiState.shouldAutoShare) {
        if (uiState.shouldAutoShare && uiState.subtitleResult != null) {
            // Small delay to let UI settle
            delay(300)
            viewModel.triggerAutoShareIfNeeded()
        }
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
            // Show Share FAB only when subtitle loaded and not auto-sharing
            if (uiState.subtitleResult != null && !uiState.shouldAutoShare) {
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
        Box(modifier = modifier.padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    // Show loading indicator
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Downloading subtitle...")
                    }
                }

                uiState.error != null -> {
                    // Show error with retry option
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            if (autoDownload) {
                                viewModel.initializeWithAutoDownloadFromRepository()
                            } else {
                                onNavigateBack()
                            }
                        }) {
                            Text(if (autoDownload) "Retry" else "Go Back")
                        }
                    }
                }

                uiState.subtitleResult != null -> {
                    // Show subtitle content (existing UI)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
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
        }

        // Share bottom sheet (existing code)
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
