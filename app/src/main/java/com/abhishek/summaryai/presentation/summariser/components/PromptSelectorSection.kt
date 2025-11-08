package com.abhishek.summaryai.presentation.summariser.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhishek.summaryai.domain.model.Prompt

/**
 * Section displaying the prompt selector dropdown and selected prompt text
 *
 * @param prompts List of available prompts
 * @param selectedPrompt Currently selected prompt
 * @param isExpanded Whether the prompt text is expanded or collapsed
 * @param onSelectPrompt Callback when a prompt is selected from dropdown
 * @param onEditClick Callback when edit button is clicked
 * @param onToggleExpansion Callback when prompt text is clicked to expand/collapse
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptSelectorSection(
    prompts: List<Prompt>,
    selectedPrompt: Prompt?,
    isExpanded: Boolean,
    onSelectPrompt: (String) -> Unit,
    onEditClick: () -> Unit,
    onToggleExpansion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with dropdown and edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Prompt",
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit prompts"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown for prompt selection
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedPrompt?.text?.take(50) ?: "Select a prompt",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    prompts.forEach { prompt ->
                        DropdownMenuItem(
                            text = { Text(prompt.text) },
                            onClick = {
                                onSelectPrompt(prompt.id)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Selected prompt display
            if (selectedPrompt != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleExpansion() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current Prompt:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand"
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isExpanded) selectedPrompt.text
                            else selectedPrompt.text.take(100) + if (selectedPrompt.text.length > 100) "..." else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
