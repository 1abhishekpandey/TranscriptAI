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

1. User pastes/enters a YouTube video URL (or shares from YouTube app via deep link)
2. App downloads subtitles using the extension module
3. Subtitles are displayed in AI Summariser screen with scrollable text viewer
4. User can optionally:
   - Enable AI Summariser to prepend custom prompts
   - Select from predefined AI prompts or create custom ones
5. User shares via Share FAB:
   - **Copy to Clipboard** - Always available
   - **Share to ChatGPT** - Direct ACTION_SEND intent or clipboard + launch fallback
   - **Share to Claude** - Direct ACTION_SEND intent or clipboard + launch fallback

### Technical Foundation

**Architecture**: Clean Architecture + MVVM pattern
**UI Framework**: Jetpack Compose (100% declarative UI)
**Language**: Kotlin (100%)
**Min SDK**: 25 (Android 7.1) | **Target SDK**: 36 (Android 15)
**Build System**: Gradle with Kotlin DSL
**Dependency Injection**: Hilt
**Async Operations**: Kotlin Coroutines + Flow
**Database**: Room (for AI prompts)
**Preferences**: SharedPreferences (for app config)

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
│   │   ├── repository/          # Repository implementations (incl. AIShareRepositoryImpl)
│   │   ├── local/               # Room database, SharedPreferences, entities
│   │   └── mapper/              # Data ↔ Domain mappers
│   ├── domain/
│   │   ├── model/               # Domain entities (incl. ShareTarget)
│   │   ├── repository/          # Repository interfaces (incl. AIShareRepository)
│   │   └── usecase/             # Use cases (download, format, config, prompts)
│   ├── presentation/            # UI layer
│   │   ├── home/                # Home screen (URL input, download trigger)
│   │   ├── summariser/          # AI Summariser screen (subtitle display + sharing)
│   │   ├── prompteditor/        # Prompt management screen
│   │   └── navigation/          # Navigation setup
│   ├── di/                      # Dependency injection (Hilt modules)
│   ├── util/                    # Utilities (Logger, ClipboardHelper, ShareHelper)
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

1. **Subtitle Downloading**: Download subtitles from YouTube videos (via extension module)
2. **AI Prompt Management**: Create, edit, delete, and select AI prompts (stored in Room DB)
3. **Subtitle Display**: Scrollable text area with word count in AI Summariser screen
4. **Smart Sharing**: Share to ChatGPT/Claude with multiple fallback strategies
5. **AI Integration**: Optionally prepend AI prompts to subtitles before sharing

### App-Level Implementation (Current Status: ✅ Fully Implemented)

**Domain Layer** (Pure Kotlin, no Android dependencies):
- **Models**: `ShareTarget`, `Prompt`, `SubtitleResult`, `SummariserConfig`
- **Repository Interfaces**: `SubtitleRepository`, `AIShareRepository`, `PromptRepository`, `SummariserConfigRepository`
- **Use Cases**:
  - `DownloadSubtitlesUseCase`: Fetch subtitles from YouTube (uses extension)
  - `FormatSubtitleForCopyUseCase`: Format subtitle with AI prompt prepending
  - `GetPromptsUseCase`, `AddPromptUseCase`, `DeletePromptUseCase`: Prompt CRUD
  - `GetSummariserConfigUseCase`, `ToggleAiSummariserUseCase`, `UpdateSummariserConfigUseCase`: Config management

**Data Layer** (Android implementations):
- **Repositories**:
  - `SubtitleRepositoryImpl`: Uses YouTubeSubtitleDownloader extension
  - `AIShareRepositoryImpl`: ACTION_SEND intent + clipboard fallback for ChatGPT/Claude
  - `PromptRepositoryImpl`: Room database operations
  - `SummariserConfigRepositoryImpl`: SharedPreferences operations
  - `SubtitleCacheRepository`: In-memory cache for subtitle sharing between screens
- **Data Sources**:
  - `YouTubeSubtitleDownloader`: Extension module for subtitle fetching
  - `AppDatabase` (Room): Stores AI prompts
  - `SummariserPreferences`: Stores app configuration

**Presentation Layer** (MVVM):
- **ViewModels**:
  - `HomeViewModel`: Handle URL input, trigger subtitle download
  - `SummariserViewModel`: Display subtitles, manage AI prompts, handle sharing
  - `PromptEditorViewModel`: Manage prompt CRUD operations
- **Screens (Jetpack Compose)**:
  - `HomeScreen`: URL input with download button
  - `SummariserScreen`: Subtitle display with AI toggle, prompt selector, Share FAB
  - `PromptEditorScreen`: Prompt management UI
- **Components**:
  - `ShareBottomSheet`: Modal sheet with Copy/ChatGPT/Claude options

## Key Dependencies

### Current Dependencies (app/build.gradle.kts)
- **Compose BOM** (2024.09.00): UI framework with Material3
- **Hilt** (2.52): Dependency injection with KSP
- **Room** (2.6.1): Local database for AI prompts
- **Coroutines**: Async operations with Flow
- **Navigation Compose**: Multi-screen navigation
- **Retrofit + OkHttp**: HTTP client (for future features)
- **androidx.lifecycle**: ViewModel and lifecycle management

### Extension Module
- **YouTube Subtitle Downloader**: `implementation(project(":extensions:youtubeSubtitleDownloader"))`
  - Self-contained module using YouTube InnerTube API
  - See [Extension CLAUDE.md](extensions/youtubeSubtitleDownloader/CLAUDE.md)

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
- **[app/CLAUDE.md](app/CLAUDE.md)** - App module documentation (detailed implementation guide)
- **[extensions/youtubeSubtitleDownloader/CLAUDE.md](extensions/youtubeSubtitleDownloader/CLAUDE.md)** - Subtitle downloader extension

---

**Last Updated**: 2025-11-12
