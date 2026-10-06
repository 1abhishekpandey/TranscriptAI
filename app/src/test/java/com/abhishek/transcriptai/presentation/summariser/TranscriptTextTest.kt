package com.abhishek.transcriptai.presentation.summariser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranscriptTextTest {

    @Test
    fun countWords_splitsOnAnyWhitespaceAndIgnoresBlanks() {
        assertEquals(3, countWords("  hello\n\tworld   again  "))
        assertEquals(2, countWords("a b"))
        assertEquals(0, countWords(""))
        assertEquals(0, countWords(" \n\t "))
        assertEquals(1, countWords("single"))
    }

    @Test
    fun splitIntoParagraphs_keepsEveryCharacterInOrderAndNeverCutsAWord() {
        val text = (1..1500).joinToString(" ") { "word$it" }

        val paragraphs = splitIntoParagraphs(text)

        assertTrue(paragraphs.size > 1)
        val rebuilt = paragraphs.joinToString(" ") { it.textIn(text) }
        assertEquals(text, rebuilt)
        paragraphs.forEach { paragraph ->
            assertTrue(paragraph.textIn(text).length <= 800)
            assertTrue(paragraph.end == text.length || text[paragraph.end].isWhitespace())
        }
    }

    @Test
    fun splitIntoParagraphs_endsAParagraphAtABlankLine() {
        val text = "first part\n\nsecond part"

        val paragraphs = splitIntoParagraphs(text).map { it.textIn(text) }

        assertEquals(listOf("first part", "second part"), paragraphs)
    }

    @Test
    fun splitIntoParagraphs_prefersTheLastSentenceEndingBeforeTheLimit() {
        val sentence = "This is one sentence. "
        val text = sentence.repeat(60)

        val paragraphs = splitIntoParagraphs(text)

        assertTrue(paragraphs.size > 1)
        paragraphs.forEach { assertTrue(it.textIn(text).endsWith(".")) }
    }

    @Test
    fun splitIntoParagraphs_cutsTextWithoutWhitespaceAtTheLengthLimit() {
        val text = "字".repeat(2000)

        val paragraphs = splitIntoParagraphs(text)

        assertEquals(text, paragraphs.joinToString("") { it.textIn(text) })
        assertTrue(paragraphs.all { it.textIn(text).length <= 800 })
    }

    @Test
    fun splitIntoParagraphs_returnsNothingForBlankText() {
        assertTrue(splitIntoParagraphs("").isEmpty())
        assertTrue(splitIntoParagraphs("  \n ").isEmpty())
    }

    @Test
    fun findMatchStarts_isCaseInsensitiveAndDoesNotOverlap() {
        assertEquals(listOf(0, 6), findMatchStarts("Hello hello", "HELLO").toList())
        assertEquals(listOf(0, 2), findMatchStarts("aaaa", "aa").toList())
        assertTrue(findMatchStarts("anything", "   ").isEmpty())
        assertTrue(findMatchStarts("anything", "").isEmpty())
        assertTrue(findMatchStarts("anything", "missing").isEmpty())
    }

    @Test
    fun paragraphIndexAt_returnsTheParagraphContainingTheOffset() {
        val paragraphs = listOf(
            TranscriptParagraph(0, 10),
            TranscriptParagraph(12, 30),
            TranscriptParagraph(31, 50)
        )

        assertEquals(0, paragraphIndexAt(paragraphs, 0))
        assertEquals(0, paragraphIndexAt(paragraphs, 11))
        assertEquals(1, paragraphIndexAt(paragraphs, 12))
        assertEquals(2, paragraphIndexAt(paragraphs, 49))
    }

    @Test
    fun matchesIn_mapsMatchesToParagraphRangesAndCutsThemAtTheParagraphEnd() {
        val paragraph = TranscriptParagraph(start = 20, end = 30)
        val matchStarts = intArrayOf(5, 22, 28, 40)

        val matches = matchesIn(paragraph, displayedLength = 10, matchStarts = matchStarts, queryLength = 4)

        assertEquals(listOf(1, 2), matches.map { it.index })
        assertEquals(2 until 6, matches[0].range)
        assertEquals(8 until 10, matches[1].range)
    }
}
