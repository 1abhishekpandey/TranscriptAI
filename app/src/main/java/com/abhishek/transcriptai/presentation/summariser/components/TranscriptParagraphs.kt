package com.abhishek.transcriptai.presentation.summariser.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.abhishek.transcriptai.presentation.summariser.TranscriptParagraph
import com.abhishek.transcriptai.presentation.summariser.matchesIn

/**
 * How search matches are drawn.
 *
 * @property match Style of every match
 * @property selected Style of the match in focus, stronger than [match]
 */
@Immutable
class MatchHighlight(val match: SpanStyle, val selected: SpanStyle)

/**
 * One paragraph of the transcript, with search matches highlighted.
 *
 * Selection is per paragraph: one selection container around the whole list makes the list stutter
 * and fight with scrolling. The styled text is only built for a paragraph that has matches, and only
 * rebuilt when its own matches or the match in focus change.
 *
 * @param text Displayed text of the paragraph
 * @param paragraph Where the paragraph sits in the full transcript
 * @param matchStarts Start offsets of every match in the full transcript, ascending
 * @param queryLength Length of the search query
 * @param selectedMatch Index of the match in focus among all matches, or -1 when there is none
 * @param style Text style of the paragraph
 * @param highlight How matches are drawn
 * @param modifier Optional modifier for customization
 */
@Composable
internal fun ParagraphText(
    text: String,
    paragraph: TranscriptParagraph,
    matchStarts: IntArray,
    queryLength: Int,
    selectedMatch: Int,
    style: TextStyle,
    highlight: MatchHighlight,
    modifier: Modifier = Modifier
) {
    val matches = remember(text, paragraph, matchStarts, queryLength) {
        matchesIn(paragraph, text.length, matchStarts, queryLength)
    }
    // Only the paragraph holding the focused match needs restyling when focus moves
    val focusedMatch = if (matches.any { it.index == selectedMatch }) selectedMatch else -1

    val highlighted = remember(text, matches, focusedMatch, highlight) {
        if (matches.isEmpty()) {
            null
        } else {
            buildAnnotatedString {
                append(text)
                matches.forEach { match ->
                    addStyle(
                        style = if (match.index == focusedMatch) highlight.selected else highlight.match,
                        start = match.range.first,
                        end = match.range.last + 1
                    )
                }
            }
        }
    }

    SelectionContainer(modifier = modifier) {
        if (highlighted == null) {
            Text(text = text, style = style)
        } else {
            Text(text = highlighted, style = style)
        }
    }
}

/**
 * Card shown in place of the transcript text while another language is being downloaded.
 *
 * @param languageLabel Name of the language being loaded
 * @param modifier Optional modifier for customization
 */
@Composable
fun TranscriptLoadingCard(
    languageLabel: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (languageLabel != null) "Loading $languageLabel…" else "Loading transcript…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
