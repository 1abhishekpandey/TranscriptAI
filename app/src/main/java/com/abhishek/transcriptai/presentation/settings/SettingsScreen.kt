package com.abhishek.transcriptai.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abhishek.transcriptai.BuildConfig
import com.abhishek.transcriptai.util.Logger
import kotlinx.coroutines.launch

/** Project page that is opened from the About section */
private const val SOURCE_CODE_URL = "https://github.com/1abhishekpandey/TranscriptAI"

/**
 * Settings Screen
 * Gathers the AI prompt settings, the YouTube client version and the app info in one place.
 *
 * Top to bottom:
 * - AI prompts: one row with the on/off state and the selected prompt, opening the AI prompts screen
 * - YouTube connection: the client version in use and a row to fix it
 * - About: app version, link to the source code, and a note that the app is not affiliated with YouTube
 *
 * @param onNavigateBack Callback for navigating back to the previous screen
 * @param onNavigateToAiPrompts Callback for opening the AI prompts screen
 * @param onNavigateToVersionInput Callback for opening the client version fix screen
 * @param viewModel SettingsViewModel injected via Hilt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAiPrompts: () -> Unit,
    onNavigateToVersionInput: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // A newer message replaces the one on screen instead of waiting behind it
    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    // The version can change on the Version Input screen. Coming back from it puts this screen
    // into the composition again, so the version is read again each time.
    LaunchedEffect(Unit) {
        viewModel.refreshClientVersion()
    }

    Logger.logV("SettingsScreen: Recomposing")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // AI prompts
            item {
                SettingsRow(
                    title = "AI prompts",
                    supporting = uiState.aiPromptsSummary.ifEmpty { null },
                    supportingMaxLines = 1,
                    leadingIcon = Icons.Default.AutoAwesome,
                    trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    onClick = {
                        Logger.logI("SettingsScreen: Opening the AI prompts screen")
                        onNavigateToAiPrompts()
                    }
                )
            }

            // YouTube connection
            item { SectionDivider() }
            item { SectionHeader("YouTube connection") }
            item {
                ListItem(
                    headlineContent = { Text("Client version") },
                    supportingContent = {
                        Text(
                            "The app pretends to be this version of the YouTube app. " +
                                "If transcripts stop working, update it."
                        )
                    },
                    leadingContent = { Icon(Icons.Default.PlayCircle, contentDescription = null) },
                    trailingContent = {
                        Text(
                            text = uiState.clientVersion,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = settingsRowColors()
                )
            }
            item {
                SettingsRow(
                    title = "Fix client version",
                    supporting = "Test and save a newer version by hand",
                    leadingIcon = Icons.Default.Build,
                    trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    onClick = {
                        Logger.logI("SettingsScreen: Opening the client version screen")
                        onNavigateToVersionInput()
                    }
                )
            }

            // About
            item { SectionDivider() }
            item { SectionHeader("About") }
            item {
                SettingsRow(
                    title = "TranscriptAI",
                    supporting = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    leadingIcon = Icons.Default.Info
                )
            }
            item {
                SettingsRow(
                    title = "Source code & updates",
                    supporting = "github.com/1abhishekpandey/TranscriptAI",
                    leadingIcon = Icons.Default.Code,
                    trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                    onClick = {
                        if (!openUrl(context, SOURCE_CODE_URL)) {
                            showMessage("No app can open this link")
                        }
                    }
                )
            }
            item {
                Text(
                    text = "TranscriptAI is not affiliated with YouTube.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
}

/**
 * Rows sit directly on the screen background instead of the surface colour of a bare list item
 */
@Composable
private fun settingsRowColors() = ListItemDefaults.colors(containerColor = Color.Transparent)

/**
 * A plain settings row: icon, title, optional second line (cut off after [supportingMaxLines] lines),
 * and an optional icon at the end.
 * It is tappable only when [onClick] is given.
 */
@Composable
private fun SettingsRow(
    title: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    supportingMaxLines: Int = Int.MAX_VALUE,
    trailingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = supporting?.let {
            { Text(it, maxLines = supportingMaxLines, overflow = TextOverflow.Ellipsis) }
        },
        leadingContent = { Icon(leadingIcon, contentDescription = null) },
        trailingContent = trailingIcon?.let { { Icon(it, contentDescription = null) } },
        colors = settingsRowColors(),
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    )
}

/**
 * Open a link in the browser.
 *
 * @return Whether an app was found to open it
 */
private fun openUrl(context: Context, url: String): Boolean =
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        Logger.logI("SettingsScreen: Opened $url")
        true
    } catch (e: ActivityNotFoundException) {
        Logger.logW("SettingsScreen: No app can open $url")
        false
    }
