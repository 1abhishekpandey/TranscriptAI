# YouTube Subtitle Downloader Extension

## Module Overview

**Purpose**: Self-contained Android library for downloading YouTube video subtitles
**Package**: `com.abhishek.youtubesubtitledownloader`
**Architecture**: Clean Architecture + MVVM
**Module Type**: Android Library
**Min SDK**: 25 | **Target SDK**: 36

## Quick Start

### Basic Usage

```kotlin
val downloader = YouTubeSubtitleDownloader.getInstance(context)

val result = downloader.downloadSubtitles("https://www.youtube.com/watch?v=VIDEO_ID")

when (result) {
    is SubtitleResult.Success -> {
        println("Text: ${result.text}")
        println("Language: ${result.language}")
    }
    is SubtitleResult.Error -> {
        println("Error: ${result.type} - ${result.message}")
    }
    is SubtitleResult.Loading -> println("Loading...")
}
```

### Advanced Options

```kotlin
// Custom language preferences
val result = downloader.downloadSubtitles(
    url = "https://youtu.be/VIDEO_ID",
    languagePreferences = listOf("hi", "en", "auto")
)

// Configure logging
YouTubeSubtitleDownloader.setLogLevel(LogLevel.ERROR)

// Clear cached API key
downloader.clearCache()
```

### Supported URL Formats
- `https://www.youtube.com/watch?v=VIDEO_ID`
- `https://youtu.be/VIDEO_ID`
- `https://www.youtube.com/embed/VIDEO_ID`
- `https://m.youtube.com/watch?v=VIDEO_ID`

## Key Features

- Downloads YouTube subtitles using InnerTube API (undocumented)
- Returns raw text without timestamps
- Automatic API key caching with 24-hour TTL
- Language preference support (default: en → hi → auto)
- Comprehensive logging with configurable levels (default: VERBOSE)
- Self-contained (bundles all dependencies)
- No app-side code changes required

## Architecture

```
YouTubeSubtitleDownloader (Public API)
    ↓
Domain Layer (Use Cases + Repository Interface)
    ↓
Data Layer (Repository Impl + Remote Service + Parsers + Cache)
    ↓
InnerTube API (3-step flow)
```

## Project Structure

```
extensions/youtubeSubtitleDownloader/
├── src/main/java/com/abhishek/youtubesubtitledownloader/
│   ├── YouTubeSubtitleDownloader.kt          # Public API entry point
│   ├── domain/                                # Domain Layer
│   │   ├── model/SubtitleResult.kt           # Sealed class (Success/Error/Loading)
│   │   ├── repository/SubtitleRepository.kt  # Repository interface
│   │   └── usecase/DownloadSubtitlesUseCase.kt
│   ├── data/                                  # Data Layer
│   │   ├── remote/
│   │   │   ├── YouTubeApiService.kt          # OkHttp-based API service
│   │   │   └── InnerTubeDto.kt               # API DTOs
│   │   ├── repository/SubtitleRepositoryImpl.kt
│   │   └── parser/XmlSubtitleParser.kt       # Parse transcript XML
│   ├── cache/ApiKeyCache.kt                   # SharedPreferences wrapper
│   └── util/
│       ├── SubtitleLogger.kt                 # Centralized logger
│       ├── LogLevel.kt                       # Log level enum
│       └── UrlValidator.kt                   # URL validation & video ID extraction
├── build.gradle.kts
└── CLAUDE.md                                 # This file
```

## How It Works

See detailed guide: [YouTube InnerTube API](../../docs/youtube-innertube-api.md)

**Summary**:
1. Extract INNERTUBE_API_KEY from YouTube page (cached for 24h)
2. Call InnerTube Player API to get caption track metadata
3. Fetch transcript XML from track URL and parse

## Configuration

### Logging

**Tag**: `transcriptaiYouTubeSubtitleDownloader`
**Default Level**: `VERBOSE`

**Log Levels**: `VERBOSE` | `DEBUG` | `INFO` | `WARN` | `ERROR` | `NONE`

```kotlin
YouTubeSubtitleDownloader.setLogLevel(LogLevel.ERROR)
```

### Language Preferences

**Default Order**: `["en", "hi", "auto"]`

- Matches `languageCode` field (case-insensitive)
- `"auto"` matches auto-generated subtitles (`kind == "asr"`)
- Falls back to first available track

### API Key Caching

- **TTL**: 24 hours (86400000 milliseconds)
- **Storage**: SharedPreferences
- **Retry Logic**: Clear cache and retry once on API failure

## Error Handling

**ErrorType Enum**:
- `INVALID_URL` - Not a YouTube URL
- `INVALID_VIDEO_ID` - Failed to extract video ID
- `NETWORK_ERROR` - Connection/timeout issues
- `API_KEY_EXTRACTION_FAILED` - Can't get INNERTUBE_API_KEY
- `CAPTION_TRACKS_NOT_FOUND` - InnerTube API returned no tracks
- `NO_SUBTITLES_AVAILABLE` - Video has no subtitles
- `LANGUAGE_NOT_AVAILABLE` - Requested language not found
- `TRANSCRIPT_DOWNLOAD_FAILED` - Failed to fetch XML
- `TRANSCRIPT_PARSE_FAILED` - Failed to parse XML
- `UNKNOWN_ERROR` - Unexpected error

## Build & Integration

### Build Module

See [Build Commands](../../docs/build-commands.md) for all commands.

```bash
./gradlew :extensions:youtubeSubtitleDownloader:build
./gradlew :extensions:youtubeSubtitleDownloader:test
```

### Integrate with App

Add to `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(project(":extensions:youtubeSubtitleDownloader"))
}
```

No other changes needed - module is self-contained!

## Dependencies (Bundled)

- OkHttp 4.12.0 (HTTP client)
- OkHttp Logging Interceptor (debug logging)
- Kotlinx Coroutines 1.7.3 (async operations)
- AndroidX Core KTX (Android utilities)
- Android XML Pull Parser (built-in)

## Development Guidelines

### When Modifying This Module

1. **Preserve Clean Architecture** - See [Architecture Guidelines](../../docs/architecture.md)
2. **Keep Internal Classes Internal** - Only expose public API classes
3. **Maintain Backward Compatibility** - Don't break existing API
4. **Log All Critical Steps** - Use appropriate log levels
5. **Handle Edge Cases** - Empty URLs, network errors, parse errors

See [Coding Standards](../../docs/coding-standards.md) for detailed guidelines.

### Testing Strategy

**Unit Tests** (to be implemented):
- `UrlValidator.extractVideoId()` - various URL formats
- `XmlSubtitleParser.parseXml()` - XML parsing
- `ApiKeyCache` - cache hit/miss/expiry
- `DownloadSubtitlesUseCase` - input validation

**Integration Tests** (to be implemented):
- End-to-end download with real YouTube URL
- API key caching across multiple requests
- Language preference selection
- Retry logic on API key failure

## Known Limitations

1. **Undocumented API** - YouTube can change it without notice
2. **Rate Limiting** - Too many requests may trigger IP blocking
3. **Terms of Service** - Using this API may violate YouTube ToS
4. **API Key Rotation** - 24h cache TTL may be too long if key rotates frequently
5. **Video Availability** - Private/restricted videos won't work, not all videos have subtitles

## Troubleshooting

### Common Issues

- **API key extraction fails**: YouTube page structure changed
- **No subtitles found**: Video has no captions or they're disabled
- **Network errors**: Check internet permission in AndroidManifest.xml
- **Parse errors**: YouTube changed transcript XML format

### Debugging

1. Set log level to `VERBOSE`
2. Check logcat for tag `transcriptaiYouTubeSubtitleDownloader`
3. Test with known working YouTube URL (with subtitles)
4. Clear cache: `downloader.clearCache()`

See [YouTube InnerTube API Guide](../../docs/youtube-innertube-api.md) for detailed troubleshooting.

## Version History

**v1.0.0** (Current)
- Initial implementation
- InnerTube API integration
- API key caching with 24h TTL
- Language preference support (en/hi/auto)
- Comprehensive logging
- Clean Architecture + MVVM pattern

## Future Enhancements (Potential)

- [ ] Coroutine Flow for progress updates
- [ ] Retry policy with exponential backoff
- [ ] Subtitle translation support
- [ ] Cache downloaded subtitles (Room DB)
- [ ] Rate limiting to prevent IP blocking
- [ ] Timestamp preservation (optional parameter)
- [ ] Batch download for multiple videos
- [ ] WebVTT/SRT export formats

---

**Last Updated**: 2025-11-09
**Module Version**: 1.0.0

## Related Documentation

- [Architecture Guidelines](../../docs/architecture.md)
- [Coding Standards](../../docs/coding-standards.md)
- [Build Commands](../../docs/build-commands.md)
- [YouTube InnerTube API Guide](../../docs/youtube-innertube-api.md)
