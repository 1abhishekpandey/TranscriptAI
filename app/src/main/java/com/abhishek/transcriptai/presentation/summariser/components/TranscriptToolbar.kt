package com.abhishek.transcriptai.presentation.summariser.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * The bar above the transcript text. It stays pinned while the transcript scrolls, so it has an opaque background.
 *
 * Normally it shows the word count with labelled Search and Copy buttons. While searching it shows
 * the match count with previous, next and close buttons, and the search field underneath.
 *
 * @param statusText Word count, or the position among the search matches
 * @param isSearchActive Whether the search field is open
 * @param query Text being searched for
 * @param canStepMatches Whether there is more than one match to step through
 * @param onQueryChange Callback when the search text changes
 * @param onSearchSubmit Callback when the keyboard's search key is pressed
 * @param onSearchClick Callback when the Search button is tapped
 * @param onCopyClick Callback when the Copy button is tapped
 * @param onPreviousMatch Callback to go to the previous match
 * @param onNextMatch Callback to go to the next match
 * @param onCloseSearch Callback to close the search
 * @param modifier Optional modifier for customization
 */
@Composable
fun TranscriptToolbar(
    statusText: String,
    isSearchActive: Boolean,
    query: String,
    canStepMatches: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onSearchClick: () -> Unit,
    onCopyClick: () -> Unit,
    onPreviousMatch: () -> Unit,
    onNextMatch: () -> Unit,
    onCloseSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column {
            if (isSearchActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onPreviousMatch, enabled = canStepMatches, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous match")
                    }
                    IconButton(onClick = onNextMatch, enabled = canStepMatches, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next match")
                    }
                    IconButton(onClick = onCloseSearch, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close search")
                    }
                }
                TranscriptSearchField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = onSearchSubmit
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    ToolbarButton(
                        icon = Icons.Default.Search,
                        label = "Search",
                        onClick = onSearchClick
                    )
                    ToolbarButton(
                        icon = Icons.Default.ContentCopy,
                        label = "Copy",
                        onClick = onCopyClick
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/**
 * Small button with an icon and a word, so what it does is clear without guessing at the icon.
 */
@Composable
private fun ToolbarButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        contentPadding = PaddingValues(start = 12.dp, end = 14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun TranscriptSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            .height(44.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                if (query.isEmpty()) {
                    Text(
                        text = "Search in transcript",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
