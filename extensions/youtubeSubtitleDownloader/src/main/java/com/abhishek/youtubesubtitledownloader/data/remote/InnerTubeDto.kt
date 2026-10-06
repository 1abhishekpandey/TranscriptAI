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

/**
 * The parts of an InnerTube Player API response the downloader uses
 *
 * @param videoTitle Title from `videoDetails`, null when the response has none
 * @param captionTracks Every caption track the video offers, empty when it has none
 */
internal data class PlayerInfoDto(
    val videoTitle: String?,
    val captionTracks: List<CaptionTrackDto>
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
