package com.abhishek.transcriptai.presentation.summariser

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abhishek.transcriptai.presentation.summariser.components.LanguageBottomSheet
import com.abhishek.transcriptai.presentation.summariser.components.ShareBottomSheet
import com.abhishek.transcriptai.util.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Summariser Screen - Shows a video's transcript with AI summarization options
 *
 * One list scrolls the whole page. From top to bottom:
 * - Header: video title and language chip
 * - Banner when the requested language isn't available for the video
 * - AI prompt card: switch in its header, prompt selector and edit button while on
 * - Transcript toolbar (word count, Search, Copy), pinned at the top while the text scrolls under it
 * - The transcript text
 * - Share button with multiple options, floating over the list
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isSearchActive by rememberSaveable { mutableStateOf(false) }

    // A newer message replaces the one on screen instead of waiting behind it
    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

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

    // One-time actions from the ViewModel
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SummariserUiEffect.ShowMessage -> showMessage(effect.message)
                is SummariserUiEffect.CopyToClipboard -> {
                    val copied = copyToClipboard(context, effect.label, effect.text)
                    showMessage(if (copied) effect.confirmation else "Couldn't copy to the clipboard")
                }
            }
        }
    }

    // Back closes the transcript search before it leaves the screen
    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
    }

    val subtitle = uiState.subtitleResult

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transcript") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Show Share FAB only when subtitle loaded and not auto-sharing
            if (subtitle != null && !uiState.shouldAutoShare && !uiState.isSwitchingLanguage) {
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
        Column(
            modifier = modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> LoadingState()

                uiState.error != null -> ErrorState(
                    message = uiState.error.orEmpty(),
                    canRetry = autoDownload,
                    onRetry = { viewModel.onEvent(SummariserUiEvent.RetryClicked) },
                    onGoBack = onNavigateBack
                )

                subtitle != null -> TranscriptContent(
                    uiState = uiState,
                    subtitle = subtitle,
                    isSearchActive = isSearchActive,
                    onSearchActiveChange = { isSearchActive = it },
                    onEvent = viewModel::onEvent
                )

                // Before the first download starts there is nothing to show yet
                else -> LoadingState()
            }
        }
    }

    // Share bottom sheet
    if (uiState.showShareSheet) {
        ShareBottomSheet(
            isAiEnabled = uiState.isAiSummariserEnabled,
            onDismiss = { viewModel.onEvent(SummariserUiEvent.DismissShareSheet) },
            onOptionSelected = { option ->
                viewModel.onEvent(SummariserUiEvent.ShareOptionSelected(option))
            }
        )
    }

    // Language bottom sheet
    if (uiState.showLanguageSheet && subtitle != null) {
        LanguageBottomSheet(
            tracks = subtitle.availableTracks,
            current = subtitle,
            onSelect = { track -> viewModel.onEvent(SummariserUiEvent.LanguageSelected(track)) },
            onDismiss = { viewModel.onEvent(SummariserUiEvent.DismissLanguageSheet) }
        )
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Downloading transcript...")
    }
}

/**
 * Error with a retry option; without auto-download there is nothing to retry, so it offers to go back.
 */
@Composable
private fun ErrorState(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onGoBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Error: $message",
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = if (canRetry) onRetry else onGoBack) {
            Text(if (canRetry) "Retry" else "Go Back")
        }
    }
}

/**
 * Put text on the system clipboard.
 *
 * @return Whether the text was copied
 */
private fun copyToClipboard(context: Context, label: String, text: String): Boolean =
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Logger.logI("SummariserScreen: Copied ${text.length} characters to clipboard")
        true
    } catch (e: Exception) {
        Logger.logE("SummariserScreen: Failed to copy to clipboard", e)
        false
    }
