package com.abhishek.transcriptai.presentation.summariser.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Small dismissible banner saying the requested language isn't available for this video.
 *
 * @param message Text to show
 * @param onDismiss Callback when the close button is tapped
 * @param modifier Optional modifier for customization
 */
@Composable
fun LanguageNoticeBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 2.dp, end = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
