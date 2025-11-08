package com.abhishek.youtubesubtitledownloader.data.remote

/**
 * Request DTO for InnerTube Player API
 */
internal data class InnerTubeRequestDto(
    val context: ContextDto,
    val videoId: String
)

internal data class ContextDto(
    val client: ClientDto
)

internal data class ClientDto(
    val clientName: String = "WEB",
    val clientVersion: String = "2.20241108.01.00"
)

/**
 * Response DTO for InnerTube Player API
 */
internal data class InnerTubeResponseDto(
    val captions: CaptionsDto?
)

internal data class CaptionsDto(
    val playerCaptionsTracklistRenderer: PlayerCaptionsTracklistRendererDto?
)

internal data class PlayerCaptionsTracklistRendererDto(
    val captionTracks: List<CaptionTrackDto>?
)

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
