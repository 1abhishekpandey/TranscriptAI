# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## High-Level Overview

**TranscriptAI** is a modern Android application that enables users to download YouTube video subtitles/transcripts, view them in an intuitive scrollable interface, and seamlessly export them to AI platforms (ChatGPT, Claude) or other destinations.

### What Makes This Project Unique

- **Self-Contained Extension Architecture**: The YouTube subtitle downloader is built as an independent, reusable module at `extensions/youtubeSubtitleDownloader/` that can be integrated into other projects
- **Direct YouTube API Integration**: Uses YouTube's InnerTube API (the same API the YouTube website uses) for reliable subtitle extraction without third-party dependencies
- **Clean Architecture Pattern**: Strictly layered architecture (Presentation → Domain ← Data) ensuring maintainability, testability, and scalability
- **Modern Android Stack**: Built entirely with Jetpack Compose, Kotlin Coroutines, and Material3 following current Android best practices

### Core User Journey

1. User pastes/enters a YouTube video URL
2. App downloads subtitles using the extension module
3. Subtitles are displayed in a clean, scrollable text viewer
4. User can export to:
   - Clipboard (for quick sharing)
   - ChatGPT/Claude apps (direct intent)
   - Text file (local storage)

### Technical Foundation

**Architecture**: Clean Architecture + MVVM pattern
**UI Framework**: Jetpack Compose (100% declarative UI)
**Language**: Kotlin (100%)
**Min SDK**: 25 (Android 7.1) | **Target SDK**: 36 (Android 15)
**Build System**: Gradle with Kotlin DSL
**Dependency Injection**: Hilt (planned) / Manual DI (current)
**Async Operations**: Kotlin Coroutines + Flow

## Quick Links

- [Architecture Guidelines](docs/architecture.md) - Clean Architecture layers, MVVM implementation, dependency flow
- [Coding Standards](docs/coding-standards.md) - Naming conventions, state management, error handling, testing
- [Build Commands](docs/build-commands.md) - All build, test, and deployment commands
- [YouTube InnerTube API Guide](docs/youtube-innertube-api.md) - How the subtitle downloader extension works

## Project Structure

```
TranscriptAI/
├── app/src/main/java/com/abhishek/transcriptai/
│   ├── data/
│   │   ├── repository/          # Repository implementations
│   │   ├── remote/              # API services, DTOs
│   │   ├── local/               # Database, SharedPreferences, entities
│   │   └── mapper/              # Data ↔ Domain mappers
│   ├── domain/
│   │   ├── model/               # Domain entities
│   │   ├── repository/          # Repository interfaces
│   │   └── usecase/             # Use cases
│   ├── presentation/            # UI layer (to be organized)
│   │   ├── home/                # Home screen (subtitle display)
│   │   ├── download/            # Download functionality
│   │   └── export/              # Export functionality
│   ├── di/                      # Dependency injection
│   └── ui/theme/                # Theme, colors, typography
├── extensions/
│   └── youtubeSubtitleDownloader/  # Self-contained subtitle downloader module
├── docs/                        # Shared documentation
└── CLAUDE.md                    # This file
```

## Architecture Overview

See [Architecture Guidelines](docs/architecture.md) for detailed information.

**Quick Summary**:
- **3 Layers**: Presentation → Domain ← Data
- **MVVM Pattern**: View (Composable) → ViewModel → Use Cases → Repository
- **Domain Layer**: Pure Kotlin, no Android dependencies
- **Clean Architecture**: Strict separation of concerns

## Development Workflow

### Build Commands

See [Build Commands](docs/build-commands.md) for all commands.

```bash
./gradlew assembleDebug              # Debug build
./gradlew test                       # Unit tests
./gradlew lint                       # Run lint checks
```

### Coding Standards

See [Coding Standards](docs/coding-standards.md) for detailed guidelines.

**Key Conventions**:
- Use Cases: `VerbNounUseCase` (e.g., `DownloadSubtitlesUseCase`)
- ViewModels: `FeatureViewModel` (e.g., `HomeViewModel`)
- Repositories: `EntityRepository` (e.g., `SubtitleRepository`)
- Composables: PascalCase (e.g., `SubtitleScreen`)

## Feature: YouTube Subtitle Downloader

### Implementation Status

The YouTube subtitle downloader is implemented as a **self-contained extension module** at `extensions/youtubeSubtitleDownloader/`.

See [YouTube Subtitle Downloader Extension CLAUDE.md](extensions/youtubeSubtitleDownloader/CLAUDE.md) for module-specific documentation.

### Core Functionality

1. Download subtitles from YouTube videos (via extension module)
2. Display subtitles in scrollable text area on home screen
3. Export options:
   - Copy to clipboard
   - Share to ChatGPT/Claude apps
   - Export as text file

### App-Level Implementation Approach

**Use Cases** (domain layer):
- `DownloadSubtitlesUseCase`: Fetch subtitles from YouTube (uses extension)
- `GetSubtitlesUseCase`: Retrieve stored subtitles
- `ExportSubtitlesUseCase`: Export to various formats
- `CopyToClipboardUseCase`: Copy text to clipboard

**Repository**:
- `SubtitleRepository`: Interface in domain layer
- `SubtitleRepositoryImpl`: Implementation in data layer

**Data Sources**:
- `YoutubeSubtitleService`: Wrapper around extension module
- `SubtitleLocalDataSource`: Cache subtitles locally (Room)

**ViewModels**:
- `HomeViewModel`: Display and scroll subtitles
- `DownloadViewModel`: Handle download process
- `ExportViewModel`: Manage export options

## Key Dependencies

### Current Dependencies (app/build.gradle.kts)
- Compose BOM (UI framework)
- androidx.lifecycle (ViewModel, LiveData)
- androidx.activity.compose
- Material3

### Extension Module
- YouTube Subtitle Downloader: `implementation(project(":extensions:youtubeSubtitleDownloader"))`

### Dependencies Needed for Features
- Room (local caching)
- Hilt/Koin (Dependency injection)
- Coroutines (async operations)
- ViewModel Compose extensions

## Project-Specific Notes

- Application package: `com.abhishek.transcriptai`
- Java version: 11
- Compose is enabled (buildFeatures.compose = true)
- Edge-to-edge display enabled in MainActivity
- No ProGuard rules for debug builds

## Extensions & Modules

### YouTube Subtitle Downloader Extension

**Location**: `extensions/youtubeSubtitleDownloader/`
**Documentation**: [Extension CLAUDE.md](extensions/youtubeSubtitleDownloader/CLAUDE.md)

**Integration**:
```kotlin
// Add to app/build.gradle.kts
dependencies {
    implementation(project(":extensions:youtubeSubtitleDownloader"))
}
```

**Usage**:
```kotlin
val downloader = YouTubeSubtitleDownloader.getInstance(context)
val result = downloader.downloadSubtitles("https://www.youtube.com/watch?v=VIDEO_ID")
```

See [YouTube InnerTube API Guide](docs/youtube-innertube-api.md) for how the extension works under the hood.

## Documentation Index

All shared documentation is in the `docs/` directory:

- **[architecture.md](docs/architecture.md)** - Clean Architecture layers, MVVM, dependency flow
- **[coding-standards.md](docs/coding-standards.md)** - Naming, state management, error handling, testing
- **[build-commands.md](docs/build-commands.md)** - Build, test, lint, install commands
- **[youtube-innertube-api.md](docs/youtube-innertube-api.md)** - InnerTube API implementation details

Module-specific documentation:
- **[extensions/youtubeSubtitleDownloader/CLAUDE.md](extensions/youtubeSubtitleDownloader/CLAUDE.md)** - Subtitle downloader extension

---

**Last Updated**: 2025-11-09
