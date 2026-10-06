package com.abhishek.transcriptai.presentation.aiprompts.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Confirmation before every prompt is replaced by the built-in defaults
 *
 * @param defaultPromptCount How many default prompts will be restored
 * @param onConfirm Callback when the user confirms the reset
 * @param onDismiss Callback when the dialog is cancelled
 */
@Composable
fun ResetPromptsDialog(
    defaultPromptCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset AI prompts?") },
        text = {
            Text("Your own prompts will be deleted and the $defaultPromptCount default prompts restored.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
