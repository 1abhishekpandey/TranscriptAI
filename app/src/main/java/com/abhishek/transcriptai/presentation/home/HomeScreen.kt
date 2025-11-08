package com.abhishek.transcriptai.presentation.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abhishek.transcriptai.util.Logger

/**
 * Home Screen Composable
 * Single screen with URL input, subtitle display, and copy functionality
 *
 * @param modifier Modifier for the screen
 * @param initialUrl Initial YouTube URL from deep link (optional)
 * @param onNavigateToSummariser Callback for navigating to Summariser screen
 * @param viewModel HomeViewModel injected via Hilt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    initialUrl: String? = null,
    onNavigateToSummariser: (videoId: String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val videoUrl by viewModel.videoUrl.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val languageExpanded by viewModel.languageExpanded.collectAsState()
    val context = LocalContext.current

    // Set initial URL from deep link (only once)
    LaunchedEffect(initialUrl) {
        viewModel.setInitialUrl(initialUrl)
    }

    // Set navigation callback
    LaunchedEffect(onNavigateToSummariser) {
        viewModel.setNavigationCallback(onNavigateToSummariser)
    }

    Logger.logV("HomeScreen: Recomposing with state: ${uiState::class.simpleName}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TranscriptAI", fontWeight = FontWeight.Bold) },
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
            // Idle prompt message - shown at top when in Idle state
            if (uiState is HomeUiState.Idle) {
                IdleContent()
            }

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

            // Language Selection Section
            CollapsibleLanguageSelection(
                selectedLanguage = selectedLanguage,
                isExpanded = languageExpanded,
                onExpandToggle = { viewModel.onEvent(HomeUiEvent.ToggleLanguageExpansion) },
                onLanguageSelect = { viewModel.onEvent(HomeUiEvent.SelectLanguage(it)) },
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

            // Clear Button - shown when text is entered or error occurred
            if (videoUrl.isNotBlank() || uiState is HomeUiState.Error) {
                OutlinedButton(
                    onClick = {
                        Logger.logD("HomeScreen: Clear button clicked")
                        viewModel.onEvent(HomeUiEvent.ClearContent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear")
                }
            }

            // Content Section - based on UI state
            when (val state = uiState) {
                is HomeUiState.Idle -> {
                    // Idle content is now shown at the top
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

@Composable
private fun CollapsibleLanguageSelection(
    selectedLanguage: SubtitleLanguage,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onLanguageSelect: (SubtitleLanguage) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header - Always visible (clickable to expand/collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { onExpandToggle() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Language",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    // Compact view - show selected language with badge style
                    Text(
                        text = selectedLanguage.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Expand/Collapse icon
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select preferred language",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Language options with radio buttons
                    SubtitleLanguage.values().forEach { language ->
                        LanguageOption(
                            language = language,
                            isSelected = selectedLanguage == language,
                            onSelect = { onLanguageSelect(language) },
                            enabled = enabled
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageOption(
    language: SubtitleLanguage,
    isSelected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onSelect() }
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelect() },
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = language.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
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
