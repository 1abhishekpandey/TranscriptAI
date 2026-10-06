package com.abhishek.transcriptai.presentation.aiprompts

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abhishek.transcriptai.domain.model.Prompt
import com.abhishek.transcriptai.presentation.aiprompts.components.ResetPromptsDialog
import com.abhishek.transcriptai.presentation.prompteditor.components.DeleteConfirmationDialog
import com.abhishek.transcriptai.util.Logger
import kotlinx.coroutines.launch

/**
 * AI Prompts Screen
 * Holds everything about the prompt that goes in front of the transcript.
 *
 * Top to bottom:
 * - The switch that turns the AI prompt on or off
 * - The saved prompts: tap one to select it, with icons to edit or delete it
 * - An "Add prompt" button floating over the list
 * - "Reset to defaults" in the top bar menu, which replaces all prompts with the built-in ones after a confirmation
 *
 * @param onNavigateBack Callback for navigating back to the previous screen
 * @param onNavigateToPromptEditor Callback for opening the prompt editor; the ID is null to add a new prompt
 * @param viewModel AiPromptsViewModel injected via Hilt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPromptsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPromptEditor: (promptId: String?) -> Unit,
    viewModel: AiPromptsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isMenuOpen by remember { mutableStateOf(false) }

    // A newer message replaces the one on screen instead of waiting behind it
    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    // One-time actions from the ViewModel
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AiPromptsUiEffect.ShowMessage -> showMessage(effect.message)
            }
        }
    }

    Logger.logV("AiPromptsScreen: Recomposing with ${uiState.prompts.size} prompts")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI prompts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isMenuOpen = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = { isMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reset to defaults") },
                            onClick = {
                                isMenuOpen = false
                                viewModel.onEvent(AiPromptsUiEvent.ResetClicked)
                            }
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    Logger.logI("AiPromptsScreen: Add prompt")
                    onNavigateToPromptEditor(null)
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add prompt") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            // Room under the last prompt so the floating button never covers it
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item {
                AiPromptSwitchRow(
                    isEnabled = uiState.isAiPromptEnabled,
                    onToggle = { viewModel.onEvent(AiPromptsUiEvent.ToggleAiPrompt(it)) }
                )
                HorizontalDivider()
            }
            item {
                Text(
                    text = "Saved prompts",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
                )
            }
            items(uiState.prompts, key = { it.id }) { prompt ->
                PromptRow(
                    prompt = prompt,
                    isSelected = prompt.id == uiState.selectedPromptId,
                    canDelete = uiState.canDeletePrompts,
                    onSelect = { viewModel.onEvent(AiPromptsUiEvent.SelectPrompt(prompt.id)) },
                    onEdit = {
                        Logger.logI("AiPromptsScreen: Edit prompt ${prompt.id}")
                        onNavigateToPromptEditor(prompt.id)
                    },
                    onDelete = { viewModel.onEvent(AiPromptsUiEvent.DeletePromptClicked(prompt.id)) }
                )
            }
        }
    }

    if (uiState.promptIdToDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = { viewModel.onEvent(AiPromptsUiEvent.ConfirmDelete) },
            onDismiss = { viewModel.onEvent(AiPromptsUiEvent.CancelDelete) }
        )
    }

    if (uiState.showResetConfirmation) {
        ResetPromptsDialog(
            defaultPromptCount = uiState.defaultPromptCount,
            onConfirm = { viewModel.onEvent(AiPromptsUiEvent.ConfirmReset) },
            onDismiss = { viewModel.onEvent(AiPromptsUiEvent.CancelReset) }
        )
    }
}

/**
 * Rows sit directly on the screen background instead of the surface colour of a bare list item
 */
@Composable
private fun rowColors() = ListItemDefaults.colors(containerColor = Color.Transparent)

/**
 * The AI prompt switch. The whole row toggles it.
 */
@Composable
private fun AiPromptSwitchRow(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text("Add AI prompt before the transcript") },
        supportingContent = {
            Text(if (isEnabled) "The selected prompt goes in front of the transcript" else "Only the transcript is shared")
        },
        leadingContent = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
        trailingContent = { Switch(checked = isEnabled, onCheckedChange = null) },
        colors = rowColors(),
        modifier = Modifier.toggleable(
            value = isEnabled,
            role = Role.Switch,
            onValueChange = onToggle
        )
    )
}

/**
 * One saved prompt. Tapping the row selects it; the icons at the end edit or delete it.
 * Delete is disabled for the last prompt.
 */
@Composable
private fun PromptRow(
    prompt: Prompt,
    isSelected: Boolean,
    canDelete: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = prompt.text,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = if (isSelected) {
            { Text("Selected", color = MaterialTheme.colorScheme.primary) }
        } else {
            null
        },
        leadingContent = { RadioButton(selected = isSelected, onClick = null) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit prompt",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete, enabled = canDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete prompt",
                        tint = if (canDelete) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
        },
        colors = rowColors(),
        modifier = Modifier.selectable(
            selected = isSelected,
            role = Role.RadioButton,
            onClick = onSelect
        )
    )
}
