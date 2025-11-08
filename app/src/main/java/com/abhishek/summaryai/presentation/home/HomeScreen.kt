package com.abhishek.summaryai.presentation.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abhishek.summaryai.util.Logger

/**
 * Home Screen Composable
 * Single screen with URL input, subtitle display, and copy functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val videoUrl by viewModel.videoUrl.collectAsState()
    val context = LocalContext.current

    Logger.logV("HomeScreen: Recomposing with state: ${uiState::class.simpleName}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SummaryAI", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // URL Input Section
            OutlinedTextField(
                value = videoUrl,
                onValueChange = { viewModel.onEvent(HomeUiEvent.UpdateVideoUrl(it)) },
                label = { Text("YouTube URL") },
                placeholder = { Text("Enter YouTube video URL or ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is HomeUiState.Loading
            )

            // Extract Button
            Button(
                onClick = {
                    Logger.logD("HomeScreen: Extract button clicked")
                    viewModel.onEvent(HomeUiEvent.DownloadSubtitle(videoUrl))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = videoUrl.isNotBlank() && uiState !is HomeUiState.Loading
            ) {
                Text("Extract Subtitle")
            }

            // Content Section - based on UI state
            when (val state = uiState) {
                is HomeUiState.Idle -> {
                    IdleContent()
                }
                is HomeUiState.Loading -> {
                    LoadingContent(message = state.message)
                }
                is HomeUiState.Success -> {
                    SuccessContent(
                        subtitle = state.subtitle,
                        videoTitle = state.videoTitle,
                        onCopyClick = {
                            Logger.logD("HomeScreen: Copy button clicked")
                            copyToClipboard(
                                context = context,
                                text = viewModel.getCurrentSubtitle()
                            )
                            viewModel.onEvent(HomeUiEvent.CopyToClipboard)
                        }
                    )
                }
                is HomeUiState.Error -> {
                    ErrorContent(message = state.message)
                }
            }
        }
    }
}

@Composable
private fun IdleContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Enter a YouTube URL to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingContent(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SuccessContent(
    subtitle: String,
    videoTitle: String?,
    onCopyClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Video title if available
        videoTitle?.let {
            Text(
                text = "Video: $it",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Subtitle display card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Copy to Clipboard Button
        Button(
            onClick = onCopyClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Copy to Clipboard")
        }
    }
}

@Composable
private fun ErrorContent(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * Copy text to clipboard and show a toast
 */
private fun copyToClipboard(context: Context, text: String) {
    Logger.logI("HomeScreen: Copying text to clipboard (${text.length} chars)")

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Subtitle", text)
    clipboardManager.setPrimaryClip(clip)

    Toast.makeText(context, "Subtitle copied to clipboard!", Toast.LENGTH_SHORT).show()
    Logger.logI("HomeScreen: Text copied successfully")
}
