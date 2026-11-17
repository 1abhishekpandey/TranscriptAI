package com.abhishek.transcriptai.domain.model

/**
 * Builds the transcript the way YouTube's own transcript panel shows it: one caption line per row,
 * its start time in front.
 *
 * ```
 * 0:00 so today we're going to talk
 * 0:03 about memory and how it works
 * ```
 *
 * @param lines Each caption line's start time in milliseconds and its text, in the order YouTube sent them
 * @return The rows joined by line breaks; blank lines are skipped
 */
fun buildTimestampedTranscript(lines: List<Pair<Long, String>>): String =
    lines
        .filter { (_, text) -> text.isNotBlank() }
        .joinToString("\n") { (startMillis, text) -> "${formatCaptionTime(startMillis)} ${text.trim()}" }

/**
 * Formats a start time the way YouTube does: minutes and seconds ("0:03", "12:45"), with hours in front
 * once the video passes an hour ("1:02:15"). Part seconds are dropped, never rounded up.
 */
fun formatCaptionTime(startMillis: Long): String {
    val totalSeconds = startMillis.coerceAtLeast(0) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    // Built by hand rather than with String.format, which would use the phone's digits (e.g., Arabic)
    val paddedSeconds = seconds.toString().padStart(2, '0')
    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:$paddedSeconds"
    } else {
        "$minutes:$paddedSeconds"
    }
}
