package com.abhishek.transcriptai.presentation.prompteditor

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abhishek.transcriptai.presentation.prompteditor.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptEditorScreen(
    promptId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PromptEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Set callbacks
    LaunchedEffect(Unit) {
        viewModel.onNavigateBack = onNavigateBack
        viewModel.onShowToast = { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        viewModel.loadPromptForEditIfProvided(promptId)
    }

    // Handle back press with unsaved changes
    BackHandler {
        viewModel.onEvent(PromptEditorUiEvent.OnBackPressed)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prompt Editor") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(PromptEditorUiEvent.OnBackPressed) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Prompt List Section
            Text(
                text = "Saved Prompts",
                style = MaterialTheme.typography.titleMedium
            )

            if (uiState.prompts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No prompts yet. Create your first prompt below!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.prompts) { prompt ->
                        PromptListItem(
                            prompt = prompt,
                            onEdit = { viewModel.onEvent(PromptEditorUiEvent.LoadPromptForEdit(prompt.id)) },
                            onDelete = { viewModel.onEvent(PromptEditorUiEvent.DeletePrompt(prompt.id)) }
                        )
                    }
                }
            }

            HorizontalDivider()

            // Editor Section
            EditorSection(
                editorText = uiState.editorText,
                editorMode = uiState.editorMode,
                hasUnsavedChanges = uiState.hasUnsavedChanges,
                validationError = uiState.validationError,
                onTextChange = { viewModel.onEvent(PromptEditorUiEvent.UpdateEditorText(it)) },
                onSave = { viewModel.onEvent(PromptEditorUiEvent.SavePrompt) },
                onClear = { viewModel.onEvent(PromptEditorUiEvent.ClearEditor) },
                onStartNew = { viewModel.onEvent(PromptEditorUiEvent.StartNewPrompt) }
            )
        }

        // Dialogs
        if (uiState.showDeleteConfirmation) {
            DeleteConfirmationDialog(
                onConfirm = { viewModel.onEvent(PromptEditorUiEvent.ConfirmDelete) },
                onDismiss = { viewModel.onEvent(PromptEditorUiEvent.CancelDelete) }
            )
        }

        if (uiState.showClearConfirmation) {
            ClearConfirmationDialog(
                onConfirm = { viewModel.onEvent(PromptEditorUiEvent.ConfirmClear) },
                onDismiss = { viewModel.onEvent(PromptEditorUiEvent.CancelClear) }
            )
        }

        if (uiState.showUnsavedChangesDialog) {
            UnsavedChangesDialog(
                onSave = { viewModel.onEvent(PromptEditorUiEvent.SaveAndExit) },
                onDiscard = { viewModel.onEvent(PromptEditorUiEvent.ConfirmDiscardChanges) },
                onCancel = { viewModel.onEvent(PromptEditorUiEvent.CancelDiscardChanges) }
            )
        }
    }
}
