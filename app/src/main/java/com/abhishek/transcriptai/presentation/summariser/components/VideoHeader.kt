package com.abhishek.transcriptai.presentation.summariser.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Header above the transcript: the video title and the language chip.
 *
 * @param title Video title, or the video ID when YouTube gave none
 * @param languageLabel Name of the language being shown, or null to hide the chip
 * @param canChangeLanguage Whether the video offers another language to switch to
 * @param isLanguageBusy Whether a language switch is already running
 * @param onLanguageClick Callback when the language chip is tapped
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoHeader(
    title: String,
    languageLabel: String?,
    canChangeLanguage: Boolean,
    isLanguageBusy: Boolean,
    onLanguageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (languageLabel != null) {
            AssistChip(
                onClick = onLanguageClick,
                enabled = canChangeLanguage && !isLanguageBusy,
                label = {
                    Text(
                        text = languageLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                    )
                },
                trailingIcon = if (canChangeLanguage) {
                    {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Change language",
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                },
                colors = AssistChipDefaults.assistChipColors(
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                    disabledLeadingIconContentColor = MaterialTheme.colorScheme.primary
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = canChangeLanguage && !isLanguageBusy,
                    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}
