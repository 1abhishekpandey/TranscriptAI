package com.abhishek.youtubesubtitledownloader.data.parser

import com.abhishek.youtubesubtitledownloader.data.remote.SubtitleSegmentDto
import com.abhishek.youtubesubtitledownloader.util.SubtitleLogger
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Parser for YouTube transcript XML
 * Converts XML format to plain text without timestamps
 */
internal object XmlSubtitleParser {

    /**
     * Parse transcript XML and extract text segments
     * @param xml The transcript XML string
     * @return List of subtitle segments
     */
    fun parseXml(xml: String): List<SubtitleSegmentDto> {
        SubtitleLogger.logStep("XML Parsing", "Starting to parse transcript XML")

        val segments = mutableListOf<SubtitleSegmentDto>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(xml.reader())

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        // Old format: <text start="0.0" dur="1.5">Hello</text>
                        "text" -> {
                            val segment = parseTextTag(parser)
                            segment?.let { segments.add(it) }
                        }
                        // New SRV3 format: <p t="0" d="1500"><s>Hello</s><s>world</s></p>
                        "p" -> {
                            val paragraphSegments = parseParagraphTag(parser)
                            segments.addAll(paragraphSegments)
                        }
                    }
                }
                eventType = parser.next()
            }

            SubtitleLogger.i("Successfully parsed ${segments.size} subtitle segments")
        } catch (e: Exception) {
            SubtitleLogger.e("Failed to parse XML", e)
            throw e
        }

        return segments
    }

    /**
     * Parse a single <text> tag (old format)
     */
    private fun parseTextTag(parser: XmlPullParser): SubtitleSegmentDto? {
        return try {
            val start = parser.getAttributeValue(null, "start")?.toFloatOrNull() ?: 0f
            val dur = parser.getAttributeValue(null, "dur")?.toFloatOrNull() ?: 0f

            // Get text content
            parser.next()
            val rawText = if (parser.eventType == XmlPullParser.TEXT) {
                parser.text
            } else {
                ""
            }

            val decodedText = decodeHtmlEntities(rawText)

            SubtitleSegmentDto(
                text = decodedText.trim(),
                startTime = start,
                duration = dur
            )
        } catch (e: Exception) {
            SubtitleLogger.w("Failed to parse text tag", e)
            null
        }
    }

    /**
     * Parse a <p> tag (SRV3 format paragraph)
     * Format: <p t="0" d="1500"><s>Hello</s><s>world</s></p>
     * - t: start time in milliseconds
     * - d: duration in milliseconds
     */
    private fun parseParagraphTag(parser: XmlPullParser): List<SubtitleSegmentDto> {
        return try {
            // Get timing attributes (in milliseconds)
            val startMs = parser.getAttributeValue(null, "t")?.toLongOrNull() ?: 0L
            val durationMs = parser.getAttributeValue(null, "d")?.toLongOrNull() ?: 0L

            // Convert to seconds
            val start = startMs / 1000f
            val duration = durationMs / 1000f

            // Collect all <s> segments within this paragraph
            val textSegments = mutableListOf<String>()
            var depth = 1 // Track nested depth

            parser.next()
            while (depth > 0 && parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "s") {
                            // Parse segment text
                            parser.next()
                            if (parser.eventType == XmlPullParser.TEXT) {
                                textSegments.add(parser.text)
                            }
                        }
                        depth++
                    }
                    XmlPullParser.END_TAG -> {
                        depth--
                    }
                }
                if (depth > 0) parser.next()
            }

            // Combine all segments into one subtitle segment
            val combinedText = textSegments.joinToString(" ")
            val decodedText = decodeHtmlEntities(combinedText)

            if (decodedText.isNotBlank()) {
                listOf(
                    SubtitleSegmentDto(
                        text = decodedText.trim(),
                        startTime = start,
                        duration = duration
                    )
                )
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            SubtitleLogger.w("Failed to parse paragraph tag", e)
            emptyList()
        }
    }

    /**
     * Convert list of segments to plain text (no timestamps)
     * @param segments List of subtitle segments
     * @return Plain text with segments joined by space
     */
    fun toPlainText(segments: List<SubtitleSegmentDto>): String {
        SubtitleLogger.logStep("Text Formatting", "Converting ${segments.size} segments to plain text")

        val plainText = segments
            .map { it.text }
            .filter { it.isNotBlank() }
            .joinToString(" ")

        SubtitleLogger.d("Generated plain text: ${plainText.length} characters")
        return plainText
    }

    /**
     * Decode HTML entities in subtitle text
     * YouTube transcripts contain HTML entities that need to be decoded
     */
    private fun decodeHtmlEntities(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            // YouTube specific entities
            .replace("&#160;", " ") // Non-breaking space
            .replace("\n", " ") // Replace newlines with spaces
            .trim()
    }
}
