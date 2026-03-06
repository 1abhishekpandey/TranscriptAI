package com.abhishek.youtubesubtitledownloader.data.remote

/**
 * Caption track from InnerTube Player API response
 */
internal data class CaptionTrackDto(
    val baseUrl: String,
    val name: NameDto,
    val languageCode: String,
    val kind: String?, // "asr" = auto-generated
    val isTranslatable: Boolean?
)

internal data class NameDto(
    val simpleText: String
)

/**
 * Internal model for subtitle segment during XML parsing
 */
internal data class SubtitleSegmentDto(
    val text: String,
    val startTime: Float,
    val duration: Float
)
