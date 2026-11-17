package com.abhishek.transcriptai.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TimestampedTranscriptTest {

    @Test
    fun formatCaptionTime_showsMinutesAndSecondsUnderAnHour() {
        assertEquals("0:00", formatCaptionTime(0))
        assertEquals("0:03", formatCaptionTime(3_000))
        assertEquals("0:03", formatCaptionTime(3_999))
        assertEquals("12:45", formatCaptionTime(765_000))
        assertEquals("59:59", formatCaptionTime(3_599_999))
    }

    @Test
    fun formatCaptionTime_addsHoursFromTheFirstHour() {
        assertEquals("1:00:00", formatCaptionTime(3_600_000))
        assertEquals("1:02:15", formatCaptionTime(3_735_000))
        assertEquals("10:00:05", formatCaptionTime(36_005_000))
    }

    @Test
    fun formatCaptionTime_treatsANegativeTimeAsTheStart() {
        assertEquals("0:00", formatCaptionTime(-500))
    }

    @Test
    fun buildTimestampedTranscript_putsEachLineOnItsOwnRowWithItsTime() {
        val text = buildTimestampedTranscript(
            listOf(
                0L to "so today we're going to talk",
                3_200L to "about memory and how it works",
                6_000L to "   ",
                6_500L to " in the brain "
            )
        )

        assertEquals(
            "0:00 so today we're going to talk\n0:03 about memory and how it works\n0:06 in the brain",
            text
        )
    }

    @Test
    fun textFor_fallsBackToPlainTextWhenThereAreNoTimes() {
        val withTimes = SubtitleResult(text = "plain", videoId = "v", timestampedText = "0:00 plain")
        val withoutTimes = SubtitleResult(text = "plain", videoId = "v")

        assertEquals("0:00 plain", withTimes.textFor(showTimestamps = true))
        assertEquals("plain", withTimes.textFor(showTimestamps = false))
        assertEquals("plain", withoutTimes.textFor(showTimestamps = true))
    }
}
