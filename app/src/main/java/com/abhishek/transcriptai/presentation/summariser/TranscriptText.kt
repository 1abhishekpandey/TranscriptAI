package com.abhishek.transcriptai.presentation.summariser

/**
 * Longest stretch of transcript shown as one paragraph.
 * Captions arrive as one unbroken line, so the text is cut into paragraphs that are readable
 * and cheap to lay out, instead of one text block that can hold hundreds of thousands of characters.
 */
private const val MAX_PARAGRAPH_LENGTH = 800

/** A paragraph is not cut shorter than this unless the text itself ends or has a blank line. */
private const val MIN_PARAGRAPH_LENGTH = 400

private const val SENTENCE_ENDINGS = ".!?…"
private const val CJK_SENTENCE_ENDINGS = "。！？"

/**
 * One displayed paragraph of the transcript.
 *
 * @property start Offset of the first character in the full transcript
 * @property end Offset after the last character in the full transcript; trailing whitespace is not shown
 */
internal data class TranscriptParagraph(val start: Int, val end: Int)

/**
 * A search match inside one paragraph.
 *
 * @property index Position of the match among all matches in the transcript
 * @property range Character range inside the paragraph's displayed text
 */
internal data class ParagraphMatch(val index: Int, val range: IntRange)

/**
 * Counts words, treating any run of whitespace as a separator and ignoring blanks.
 */
internal fun countWords(text: String): Int {
    var count = 0
    var inWord = false
    for (char in text) {
        if (char.isWhitespace()) {
            inWord = false
        } else if (!inWord) {
            inWord = true
            count++
        }
    }
    return count
}

/**
 * Text of a paragraph as displayed.
 */
internal fun TranscriptParagraph.textIn(fullText: String): String =
    fullText.substring(start, end).trimEnd()

/**
 * Cuts the transcript into paragraphs without losing or reordering any character.
 *
 * A blank line always ends a paragraph. Otherwise a paragraph ends at the last sentence ending,
 * or failing that the last whitespace, before [MAX_PARAGRAPH_LENGTH] characters. Text with no
 * whitespace at all (e.g., Chinese or Japanese) is cut at the length limit.
 */
internal fun splitIntoParagraphs(text: String): List<TranscriptParagraph> {
    val paragraphs = ArrayList<TranscriptParagraph>()
    var start = 0
    while (start < text.length) {
        while (start < text.length && text[start].isWhitespace()) start++
        if (start >= text.length) break
        val end = findParagraphEnd(text, start)
        paragraphs.add(TranscriptParagraph(start, end))
        start = end
    }
    return paragraphs
}

private fun findParagraphEnd(text: String, start: Int): Int {
    val limit = minOf(start + MAX_PARAGRAPH_LENGTH, text.length)

    for (i in start until limit - 1) {
        if (text[i] == '\n' && text[i + 1] == '\n') return i
    }
    if (limit == text.length) return limit

    val earliest = start + MIN_PARAGRAPH_LENGTH
    for (i in limit downTo earliest) {
        val previous = text[i - 1]
        val endsSentence = previous in CJK_SENTENCE_ENDINGS ||
            (previous in SENTENCE_ENDINGS && text[i].isWhitespace())
        if (endsSentence) return i
    }
    for (i in limit downTo earliest) {
        if (text[i].isWhitespace()) return i
    }
    return if (text[limit - 1].isHighSurrogate()) limit - 1 else limit
}

/**
 * Finds every non-overlapping, case-insensitive occurrence of [query].
 *
 * @return Start offsets in ascending order; empty when the query is blank or not found
 */
internal fun findMatchStarts(text: String, query: String): IntArray {
    if (query.isBlank()) return IntArray(0)
    val starts = ArrayList<Int>()
    var from = 0
    while (true) {
        val found = text.indexOf(query, from, ignoreCase = true)
        if (found < 0) break
        starts.add(found)
        from = found + query.length
    }
    return starts.toIntArray()
}

/**
 * Index of the paragraph that contains [offset], or the nearest one before it.
 */
internal fun paragraphIndexAt(paragraphs: List<TranscriptParagraph>, offset: Int): Int {
    var low = 0
    var high = paragraphs.lastIndex
    var result = 0
    while (low <= high) {
        val middle = (low + high) ushr 1
        if (paragraphs[middle].start <= offset) {
            result = middle
            low = middle + 1
        } else {
            high = middle - 1
        }
    }
    return result
}

/**
 * Matches that start inside a paragraph, with ranges relative to the paragraph's displayed text.
 * A match that runs past the end of the paragraph is cut at the paragraph end.
 *
 * @param paragraph The paragraph to look in
 * @param displayedLength Length of the paragraph's displayed text
 * @param matchStarts Start offsets from [findMatchStarts], ascending
 * @param queryLength Length of the search query
 */
internal fun matchesIn(
    paragraph: TranscriptParagraph,
    displayedLength: Int,
    matchStarts: IntArray,
    queryLength: Int
): List<ParagraphMatch> {
    if (matchStarts.isEmpty()) return emptyList()

    var low = 0
    var high = matchStarts.size
    while (low < high) {
        val middle = (low + high) ushr 1
        if (matchStarts[middle] < paragraph.start) low = middle + 1 else high = middle
    }

    val matches = ArrayList<ParagraphMatch>()
    var index = low
    while (index < matchStarts.size && matchStarts[index] < paragraph.end) {
        val from = matchStarts[index] - paragraph.start
        val to = minOf(from + queryLength, displayedLength)
        if (from < to) matches.add(ParagraphMatch(index, from until to))
        index++
    }
    return matches
}
