package com.abhishek.transcriptai.presentation.summariser.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abhishek.transcriptai.domain.model.Prompt

/**
 * One compact card for the AI prompt: the on/off switch sits in the header and, while it is on,
 * the prompt selector and the edit button appear below it.
 *
 * @param isEnabled Whether the AI prompt is added in front of the transcript
 * @param prompts List of available prompts
 * @param selectedPrompt Currently selected prompt
 * @param onToggle Callback when the switch is flipped
 * @param onSelectPrompt Callback when a prompt is picked from the dropdown
 * @param onEditClick Callback when the edit/add prompts button is tapped
 * @param modifier Optional modifier for customization
 */
@Composable
fun AiPromptCard(
    isEnabled: Boolean,
    prompts: List<Prompt>,
    selectedPrompt: Prompt?,
    onToggle: (Boolean) -> Unit,
    onSelectPrompt: (String) -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI prompt",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isEnabled) "Added before the transcript" else "Only the transcript is copied",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle
                )
            }

            AnimatedVisibility(
                visible = isEnabled,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PromptDropdown(
                        prompts = prompts,
                        selectedPrompt = selectedPrompt,
                        onSelectPrompt = onSelectPrompt,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalIconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit or add prompts"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single-line prompt picker that opens a menu as wide as the picker itself.
 */
@Composable
private fun PromptDropdown(
    prompts: List<Prompt>,
    selectedPrompt: Prompt?,
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidthPx by remember { mutableIntStateOf(0) }
    val anchorWidth = with(LocalDensity.current) { anchorWidthPx.toDp() }

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorWidthPx = it.width }
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedPrompt?.text?.replace('\n', ' ') ?: "Select a prompt",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Choose a prompt"
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(anchorWidth)
        ) {
            prompts.forEach { prompt ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = prompt.text,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = if (prompt.id == selectedPrompt?.id) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        onSelectPrompt(prompt.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
