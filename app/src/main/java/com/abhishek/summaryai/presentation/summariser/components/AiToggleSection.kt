package com.abhishek.summaryai.presentation.summariser.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Section displaying the AI Summarizer toggle switch
 *
 * @param isEnabled Whether the AI summarizer is currently enabled
 * @param onToggle Callback when the toggle is switched
 * @param modifier Optional modifier for customization
 */
@Composable
fun AiToggleSection(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Summarizer",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (isEnabled) "Prompt will be added before subtitle"
                    else "Only subtitle text will be copied",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}
