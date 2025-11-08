package com.abhishek.summaryai.presentation.summariser

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
import com.abhishek.summaryai.presentation.summariser.components.*
import com.abhishek.summaryai.util.clipboard.ClipboardHelper

/**
 * Summariser Screen - Main screen for AI summarizer feature
 *
 * Displays subtitle text with AI summarization options:
 * - AI toggle switch
 * - Prompt selector dropdown
 * - Scrollable subtitle text
 * - Copy to clipboard button
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
                FloatingActionButton(
                    onClick = { viewModel.onEvent(SummariserUiEvent.CopyToClipboard) }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Copy to clipboard")
                }
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            isExpanded = uiState.isPromptExpanded,
                            onSelectPrompt = { viewModel.onEvent(SummariserUiEvent.SelectPrompt(it)) },
                            onEditClick = { viewModel.onEvent(SummariserUiEvent.NavigateToPromptEditor) },
                            onToggleExpansion = { viewModel.onEvent(SummariserUiEvent.TogglePromptExpansion) }
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
}
