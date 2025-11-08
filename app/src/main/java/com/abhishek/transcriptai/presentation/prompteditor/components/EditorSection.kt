package com.abhishek.transcriptai.presentation.prompteditor.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhishek.transcriptai.domain.model.EditorMode
import com.abhishek.transcriptai.util.constants.SummariserConstants

@Composable
fun EditorSection(
    editorText: String,
    editorMode: EditorMode,
    hasUnsavedChanges: Boolean,
    validationError: String?,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onStartNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (editorMode) {
                        is EditorMode.New -> "Create New Prompt"
                        is EditorMode.Edit -> "Edit Prompt"
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                if (editorMode is EditorMode.Edit) {
                    TextButton(onClick = onStartNew) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editorText,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("Enter your prompt here...") },
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = validationError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "${editorText.length} / ${SummariserConstants.MAX_PROMPT_LENGTH}",
                            color = if (editorText.length > SummariserConstants.MAX_PROMPT_LENGTH)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                isError = validationError != null || editorText.length > SummariserConstants.MAX_PROMPT_LENGTH,
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Clear Button
                OutlinedButton(
                    onClick = onClear,
                    enabled = editorText.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }

                // Save Button
                Button(
                    onClick = onSave,
                    enabled = hasUnsavedChanges &&
                              editorText.isNotEmpty() &&
                              editorText.length <= SummariserConstants.MAX_PROMPT_LENGTH,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save")
                }
            }
        }
    }
}
