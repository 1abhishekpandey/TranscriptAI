package com.abhishek.transcriptai.presentation.summariser.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abhishek.transcriptai.domain.model.ShareTarget
import com.abhishek.transcriptai.util.share.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    isAiEnabled: Boolean,
    onDismiss: () -> Unit,
    onOptionSelected: (ShareTarget) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = "Share Options",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Copy to Clipboard - Always available
            ShareOptionItem(
                icon = Icons.Default.ContentCopy,
                title = "Copy to Clipboard",
                subtitle = "Copy text to clipboard",
                enabled = true,
                onClick = {
                    onOptionSelected(ShareTarget.CLIPBOARD)
                    onDismiss()
                }
            )

            // One row per supported AI app
            ShareTarget.aiApps.forEach { app ->
                val isInstalled = ShareHelper.isAppInstalled(context, app.packageName)
                val enabled = isAiEnabled && isInstalled
                ShareOptionItem(
                    icon = Icons.Default.Share,
                    title = "Share to ${app.displayName}",
                    subtitle = when {
                        !isAiEnabled -> "Enable AI summariser to share"
                        !isInstalled -> "${app.displayName} app is not installed"
                        else -> "Share text to ${app.displayName} app"
                    },
                    enabled = enabled,
                    onClick = {
                        if (enabled) {
                            onOptionSelected(app)
                            onDismiss()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ShareOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }
    }
}
