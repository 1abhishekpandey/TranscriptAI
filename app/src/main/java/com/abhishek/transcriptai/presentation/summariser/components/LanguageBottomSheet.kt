package com.abhishek.transcriptai.presentation.summariser.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abhishek.transcriptai.domain.model.CaptionTrack
import com.abhishek.transcriptai.domain.model.SubtitleResult
import com.abhishek.transcriptai.presentation.summariser.languageRows

/**
 * Bottom sheet listing the languages this video really has, with the one on screen marked.
 *
 * The sheet opens fully expanded so a drag on the list scrolls the list; it is closed by tapping
 * outside, pressing back, or dragging down while the list is at its top. The list starts at the language
 * on screen so it is in view even when the video has dozens of tracks.
 *
 * @param tracks Caption tracks the video offers
 * @param current The transcript on screen, used to mark its track
 * @param onSelect Callback when a language is picked
 * @param onDismiss Callback when the sheet is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    tracks: List<CaptionTrack>,
    current: SubtitleResult,
    onSelect: (CaptionTrack) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rows = remember(tracks, current) { languageRows(tracks, current) }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (rows.indexOfFirst { it.isCurrent } - ROWS_SHOWN_ABOVE_CURRENT).coerceAtLeast(0)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Transcript language",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                text = "Languages this video has. Switching only affects this video.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(rows, key = { it.key }, contentType = { "language" }) { row ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = row.label,
                                fontWeight = if (row.isCurrent) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        supportingContent = row.secondary?.let { { Text(it) } },
                        trailingContent = if (row.isCurrent) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Showing now",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            null
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.selectable(
                            selected = row.isCurrent,
                            role = Role.RadioButton,
                            onClick = { onSelect(row.track) }
                        )
                    )
                }
            }
        }
    }
}

/** Rows kept in view above the language on screen when the list opens, for context. */
private const val ROWS_SHOWN_ABOVE_CURRENT = 2
