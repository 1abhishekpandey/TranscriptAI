package com.abhishek.transcriptai.presentation.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abhishek.transcriptai.domain.model.LanguageChoice
import com.abhishek.transcriptai.domain.model.LanguageMode
import com.abhishek.transcriptai.domain.model.SubtitleLanguages
import com.abhishek.transcriptai.util.Logger

/**
 * Sample video offered below the Extract button: a TED talk with captions in 50+ languages,
 * so a language choice can be tried without finding a video first
 */
private const val SAMPLE_VIDEO_URL = "https://www.youtube.com/watch?v=arj7oStGLkU"

/**
 * Home Screen Composable
 * Single screen with URL input, language choice, extract button, a sample video entry
 * and the auto-share toggle
 *
 * @param modifier Modifier for the screen
 * @param initialUrl Initial YouTube URL from deep link (optional)
 * @param onNavigateToSummariser Callback for navigating to Summariser screen
 * @param onNavigateToVersionInput Callback for opening the client version fix screen
 * @param onNavigateToLanguageSettings Callback for opening the language settings page
 * @param onNavigateToSettings Callback for opening the Settings screen from the top bar
 * @param onTrySampleVideo Callback for opening the transcript of the sample video, given its URL
 * @param viewModel HomeViewModel injected via Hilt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    initialUrl: String? = null,
    onNavigateToSummariser: (videoId: String) -> Unit = {},
    onNavigateToVersionInput: () -> Unit = {},
    onNavigateToLanguageSettings: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onTrySampleVideo: (url: String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val videoUrl by viewModel.videoUrl.collectAsState()
    val languageChoice by viewModel.languageChoice.collectAsState()
    val languageExpanded by viewModel.languageExpanded.collectAsState()
    val autoShareEnabled by viewModel.autoShareEnabled.collectAsState()
    val selectedApp by viewModel.selectedApp.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val isLoading = uiState is HomeUiState.Loading
    val canExtract = videoUrl.isNotBlank() && !isLoading

    // Set initial URL from deep link (only once)
    LaunchedEffect(initialUrl) {
        viewModel.setInitialUrl(initialUrl)
    }

    // Set navigation callback
    LaunchedEffect(onNavigateToSummariser) {
        viewModel.setNavigationCallback(onNavigateToSummariser)
    }

    // Set version input navigation callback
    LaunchedEffect(onNavigateToVersionInput) {
        viewModel.setVersionInputNavigationCallback(onNavigateToVersionInput)
    }

    Logger.logV("HomeScreen: Recomposing with state: ${uiState::class.simpleName}")

    val onExtract = {
        Logger.logD("HomeScreen: Extract requested")
        focusManager.clearFocus()
        viewModel.onEvent(HomeUiEvent.DownloadSubtitle(videoUrl.trim()))
    }

    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("TranscriptAI", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            AutoShareToggleBar(
                enabled = autoShareEnabled,
                selectedApp = selectedApp,
                onToggle = { viewModel.onEvent(HomeUiEvent.ToggleAutoShare(it)) },
                onSelectApp = { viewModel.onEvent(HomeUiEvent.SelectAutoShareApp(it)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState is HomeUiState.Idle) {
                IntroHeader()
            }

            UrlInputField(
                value = videoUrl,
                enabled = !isLoading,
                onValueChange = { viewModel.onEvent(HomeUiEvent.UpdateVideoUrl(it)) },
                onPaste = {
                    Logger.logD("HomeScreen: Paste clicked")
                    val pastedUrl = readClipboardUrl(context)
                    if (pastedUrl != null) {
                        viewModel.onEvent(HomeUiEvent.UpdateVideoUrl(pastedUrl))
                    } else {
                        Toast.makeText(context, "Nothing to paste", Toast.LENGTH_SHORT).show()
                    }
                },
                onClear = {
                    Logger.logD("HomeScreen: Clear clicked")
                    viewModel.onEvent(HomeUiEvent.ClearContent)
                },
                onGo = { if (canExtract) onExtract() }
            )

            LanguageCard(
                languageChoice = languageChoice,
                isExpanded = languageExpanded,
                onExpandToggle = { viewModel.onEvent(HomeUiEvent.ToggleLanguageExpansion) },
                onModeSelect = { viewModel.onEvent(HomeUiEvent.SelectLanguageMode(it)) },
                onOpenLanguageSettings = onNavigateToLanguageSettings,
                enabled = !isLoading
            )

            Button(
                onClick = onExtract,
                enabled = canExtract,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Text("Extract Subtitle", style = MaterialTheme.typography.titleMedium)
            }

            // Only offered while the URL field is empty and nothing is downloading
            if (uiState is HomeUiState.Idle && videoUrl.isBlank()) {
                SampleVideoEntry(
                    languageName = languageChoice.selectedName(),
                    onClick = {
                        Logger.logD("HomeScreen: Sample video clicked")
                        focusManager.clearFocus()
                        onTrySampleVideo(SAMPLE_VIDEO_URL)
                    }
                )
            }

            // Status section - based on UI state
            when (val state = uiState) {
                is HomeUiState.Idle -> Unit
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
                is HomeUiState.VersionOutdated -> {
                    VersionOutdatedContent(
                        currentVersion = state.currentVersion,
                        onFixClick = { viewModel.navigateToVersionInput() }
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Get a YouTube transcript",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Paste a video link to read its subtitles, copy them, or send them to ChatGPT or Claude.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Low-emphasis row that opens the sample video's transcript in the selected language,
 * so it does not compete with the Extract button
 */
@Composable
private fun SampleVideoEntry(
    languageName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.PlayCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Try a sample video",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "TED talk with captions in 50+ languages",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Opens in: $languageName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * URL field with a Paste action while empty and a clear (x) action once text is entered
 */
@Composable
private fun UrlInputField(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onPaste: () -> Unit,
    onClear: () -> Unit,
    onGo: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("YouTube URL") },
        placeholder = { Text("Link or video ID") },
        trailingIcon = {
            if (value.isEmpty()) {
                TextButton(onClick = onPaste, enabled = enabled) {
                    Text("Paste")
                }
            } else {
                IconButton(onClick = onClear, enabled = enabled) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear URL"
                    )
                }
            }
        },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Go
        ),
        keyboardActions = KeyboardActions(onGo = { onGo() }),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LoadingContent(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
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

/**
 * Fallback result view, shown only when no navigation callback is set
 */
@Composable
private fun SuccessContent(
    subtitle: String,
    videoTitle: String?,
    onCopyClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        videoTitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            )
        }

        Button(
            onClick = onCopyClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) {
            Text("Copy to Clipboard")
        }
    }
}

@Composable
private fun ErrorContent(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Couldn't get subtitles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun VersionOutdatedContent(
    currentVersion: String,
    onFixClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Version outdated",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "YouTube has rejected the current client version ($currentVersion). You can provide a newer version to fix this.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Button(
                onClick = onFixClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Fix Version")
            }
        }
    }
}

/**
 * Display name of the selected language option, e.g. "Auto-generated" or "Bahasa Indonesia"
 */
private fun LanguageChoice.selectedName(): String = when (mode) {
    LanguageMode.AUTO -> "Auto-generated"
    LanguageMode.OTHER -> otherLanguage?.nativeName ?: "Auto-generated"
}

/**
 * Collapsible card showing the selected subtitle language.
 * Expands to the auto-generated option, the language picked on the settings page,
 * and a row that opens the settings page.
 */
@Composable
private fun LanguageCard(
    languageChoice: LanguageChoice,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onModeSelect: (LanguageMode) -> Unit,
    onOpenLanguageSettings: () -> Unit,
    enabled: Boolean
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "languageArrowRotation"
    )

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            // Header - always visible, tap to expand or collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = enabled,
                        onClickLabel = if (isExpanded) "Hide language options" else "Show language options",
                        onClick = onExpandToggle
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Subtitle language",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = languageChoice.selectedName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(arrowRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider()
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        LanguageOption(
                            title = "Auto-generated",
                            subtitle = "Captions YouTube makes from the video's speech",
                            isSelected = languageChoice.mode == LanguageMode.AUTO,
                            onSelect = { onModeSelect(LanguageMode.AUTO) },
                            enabled = enabled
                        )

                        // Language picked on the settings page, shown in its own script
                        languageChoice.otherLanguage?.let { otherLanguage ->
                            LanguageOption(
                                title = otherLanguage.nativeName,
                                subtitle = otherLanguage.englishName,
                                isSelected = languageChoice.mode == LanguageMode.OTHER,
                                onSelect = { onModeSelect(LanguageMode.OTHER) },
                                enabled = enabled
                            )
                        }

                        OtherLanguagesRow(
                            onClick = onOpenLanguageSettings,
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
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onSelect
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Opens the language settings page to pick a subtitle language
 */
@Composable
private fun OtherLanguagesRow(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Other languages",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Choose from ${SubtitleLanguages.all.size} languages",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Apps the auto-share option can send to, as saved id and display name
 */
private val autoShareApps = listOf(
    "chatgpt" to "ChatGPT",
    "claude" to "Claude"
)

/**
 * Auto-Share Toggle Bar
 * Displays the toggle for the auto-share feature and,
 * when it is on, the app the transcript is sent to
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoShareToggleBar(
    enabled: Boolean,
    selectedApp: String,
    onToggle: (Boolean) -> Unit,
    onSelectApp: (String) -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = enabled,
                        role = Role.Switch,
                        onValueChange = onToggle
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-forward from YouTube",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Send a shared video's transcript straight to ChatGPT or Claude",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = enabled,
                    onCheckedChange = null
                )
            }

            if (enabled) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    autoShareApps.forEachIndexed { index, (appId, appName) ->
                        SegmentedButton(
                            selected = selectedApp == appId,
                            onClick = { onSelectApp(appId) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = autoShareApps.size
                            )
                        ) {
                            Text(appName)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Read the clipboard text for the URL field.
 * Returns the first YouTube link in the text, or the trimmed text when it has none,
 * or null when the clipboard holds no text.
 */
private fun readClipboardUrl(context: Context): String? {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = clipboardManager.primaryClip?.takeIf { it.itemCount > 0 } ?: return null
    val text = clip.getItemAt(0).coerceToText(context)?.toString()?.trim()
    if (text.isNullOrEmpty()) return null

    return text.split(Regex("\\s+"))
        .firstOrNull { it.contains("youtube.com") || it.contains("youtu.be") }
        ?: text
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
