# TranscriptAI App Module

This file provides guidance to Claude Code when working with the TranscriptAI Android application module.

## Module Overview

**Name**: TranscriptAI
**Package**: `com.abhishek.TranscriptAI`
**Purpose**: Android application for downloading YouTube subtitles and displaying them in a scrollable format with export capabilities
**Architecture**: MVVM + Clean Architecture
**UI Framework**: Jetpack Compose with Material3
**Language**: Kotlin
**Min SDK**: 25 | **Target SDK**: 36

## Quick Links

- [Architecture Guidelines](../docs/architecture.md)
- [Coding Standards](../docs/coding-standards.md)
- [Build Commands](../docs/build-commands.md)
- [Dependencies Guide](../docs/dependencies.md)
- [Logging Guide](../docs/logging.md)
- [Troubleshooting](../docs/troubleshooting.md)

## Project Structure

```
app/src/main/java/com/abhishek/TranscriptAI/
├── TranscriptAIApplication.kt          # Application class (@HiltAndroidApp)
├── MainActivity.kt                   # Main entry point (@AndroidEntryPoint)
│
├── util/                             # Utilities
│   └── Logger.kt                     # Centralized logging (Tag: "TranscriptAI", Default: VERBOSE)
│
├── domain/                           # Domain Layer (No Android dependencies)
│   ├── model/                        # Domain entities
│   ├── repository/                   # Repository interfaces
│   └── usecase/                      # Use cases
│
├── data/                             # Data Layer
│   ├── repository/                   # Repository implementations
│   ├── local/                        # Database, SharedPreferences, entities
│   ├── remote/                       # API services, DTOs (if needed)
│   └── mapper/                       # Data ↔ Domain mappers
│
├── presentation/                     # Presentation Layer
│   └── home/                         # Home screen
│       ├── HomeUiState.kt            # UI state
│       ├── HomeViewModel.kt          # ViewModel
│       └── HomeScreen.kt             # Composable UI
│
├── di/                               # Dependency Injection (Hilt)
│   └── AppModule.kt                  # Hilt modules
│
└── ui/theme/                         # Theme & Design
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

## Architecture

See [Architecture Guidelines](../docs/architecture.md) for complete details.

**Quick Summary**:
```
Presentation → Domain ← Data
```

- **Domain Layer**: Pure Kotlin, no Android dependencies
- **Data Layer**: Implements repository interfaces, handles data sources
- **Presentation Layer**: ViewModels + Composables, manages UI state

## Current Implementation

### Home Screen (Single Screen UI)

**Location**: `presentation/home/HomeScreen.kt`

**Features**:
- URL input field for YouTube URL
- "Extract Subtitle" button
- Loading indicator with progress messages
- Scrollable subtitle display (LazyColumn)
- "Copy to Clipboard" button

**UI States** (`HomeUiState.kt`):
- `Idle` - Initial state
- `Loading(message)` - Shows progress ("Fetching video info...", "Parsing subtitles...")
- `Success(subtitle, videoTitle)` - Shows subtitle with copy button
- `Error(message)` - Shows error card

**ViewModel** (`HomeViewModel.kt`):
- Manages `StateFlow<HomeUiState>`
- Handles events via `onEvent(HomeUiEvent)`
- Coordinates with `DownloadSubtitlesUseCase`
- Comprehensive logging at each step

### Logging System

**Location**: `util/Logger.kt`
**Tag**: `"TranscriptAI"`
**Default Level**: `VERBOSE`

See [Logging Guide](../docs/logging.md) for complete documentation.

**Quick Usage**:
```kotlin
Logger.logI("HomeViewModel: Starting subtitle download")
Logger.logE("Download failed", exception)
Logger.logEntry("methodName")
Logger.logExit("methodName")
```

### Dependency Injection

**Framework**: Hilt

**Setup**:
- Application: `@HiltAndroidApp` on `TranscriptAIApplication`
- Activity: `@AndroidEntryPoint` on `MainActivity`
- ViewModel: `@HiltViewModel` on `HomeViewModel`
- Modules: `AppModule.kt` provides dependencies

**Modules** (`di/AppModule.kt`):
- `provideOkHttpClient()` - OkHttp with logging
- `provideRetrofit()` - Retrofit instance
- `provideSubtitleRepository()` - Repository implementation

## Implementation Status

### ✅ Implemented

1. Complete UI (single screen)
2. MVVM Architecture
3. Clean Architecture layers
4. Logging system with TranscriptAI tag
5. Dependency Injection (Hilt)
6. Mock data for UI testing

### 🚧 Pending Integration

**YouTube Subtitle Downloader Extension**:
- **Location**: `extensions/youtubeSubtitleDownloader/`
- **Integration Point**: `data/repository/SubtitleRepositoryImpl.kt:27`
- **TODO**: Replace mock data with actual extension call

**Next Steps**:
1. Add extension dependency to `app/build.gradle.kts`
2. Update `SubtitleRepositoryImpl` to use extension
3. Handle extension's `SubtitleResult` response
4. Remove mock data

See [Extension Documentation](../extensions/youtubeSubtitleDownloader/CLAUDE.md) for usage details.

## Development Workflow

### Build & Run

See [Build Commands](../docs/build-commands.md) for all commands.

```bash
./gradlew clean                  # Clean build
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Install on device
```

**APK Output**: `app/build/outputs/apk/debug/app-debug.apk`

### Coding Standards

See [Coding Standards](../docs/coding-standards.md) for complete guidelines.

**Key Conventions**:
- Use Cases: `VerbNounUseCase` (e.g., `DownloadSubtitlesUseCase`)
- ViewModels: `FeatureViewModel` (e.g., `HomeViewModel`)
- Repositories: `EntityRepository` (e.g., `SubtitleRepository`)
- Composables: PascalCase (e.g., `HomeScreen`)
- UI States: `FeatureUiState` (e.g., `HomeUiState`)
- UI Events: `FeatureUiEvent` (e.g., `HomeUiEvent`)

### State Management

- Use `StateFlow` for UI state in ViewModels
- Immutable data classes for UI state
- Single source of truth
- Collect in Composables: `collectAsState()`

### Error Handling

Domain layer uses `Result` sealed class:
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

## Dependencies

See [Dependencies Guide](../docs/dependencies.md) for complete details.

### Core Libraries

- **Compose**: UI framework (BOM 2024.09.00)
- **Hilt**: Dependency injection (2.52)
- **Coroutines**: Async operations
- **Retrofit/OkHttp**: Networking
- **Timber**: Logging

### Version Compatibility

**Critical**: Do not upgrade Kotlin to 2.x without updating Compose configuration

- AGP: 8.5.2
- Kotlin: 1.9.24
- KSP: 1.9.24-1.0.20
- Hilt: 2.52
- Compose Compiler: 1.5.14

## Common Tasks

### Adding a New Feature

**Domain Layer**:
1. Create model in `domain/model/`
2. Define repository interface in `domain/repository/`
3. Create use case in `domain/usecase/`

**Data Layer**:
1. Implement repository in `data/repository/`
2. Add DTOs if needed in `data/remote/dto/`
3. Create mappers in `data/mapper/`

**Presentation Layer**:
1. Create UI state in `presentation/feature/FeatureUiState.kt`
2. Create ViewModel in `presentation/feature/FeatureViewModel.kt`
3. Create Composable in `presentation/feature/FeatureScreen.kt`

**DI**:
1. Add providers to `di/AppModule.kt`
2. Inject in ViewModel constructor

See [Architecture Guidelines](../docs/architecture.md) for detailed patterns.

### Integrating YouTube Subtitle Downloader Extension

1. **Add dependency** (`app/build.gradle.kts`):
   ```kotlin
   dependencies {
       implementation(project(":extensions:youtubeSubtitleDownloader"))
   }
   ```

2. **Update repository** (`SubtitleRepositoryImpl.kt`):
   ```kotlin
   override suspend fun downloadSubtitles(url: String): Result<String> {
       val downloader = YouTubeSubtitleDownloader.getInstance(context)

       return when (val result = downloader.downloadSubtitles(url)) {
           is SubtitleResult.Success -> Result.Success(result.text)
           is SubtitleResult.Error -> Result.Error(result.message)
           is SubtitleResult.Loading -> Result.Loading
       }
   }
   ```

3. **Remove mock data** and test with real YouTube URLs

### Updating Theme

1. Colors: `ui/theme/Color.kt`
2. Theme: `ui/theme/Theme.kt`
3. Typography: `ui/theme/Type.kt`

## Troubleshooting

See [Troubleshooting Guide](../docs/troubleshooting.md) for complete solutions.

### Quick Fixes

**Build errors**:
```bash
./gradlew clean
./gradlew build
```

**Hilt errors**:
- Check Hilt version matches Kotlin/AGP (current: 2.52 + 1.9.24 + 8.5.2)

**Logger not working**:
- Ensure `Logger.init()` in `TranscriptAIApplication.onCreate()`

**UI not updating**:
- Verify StateFlow collection: `collectAsState()`

**View logs**:
```bash
adb logcat -s TranscriptAI
```

## Permissions

**Declared** (`AndroidManifest.xml`):
- `android.permission.INTERNET` - Required for YouTube API calls

## Known Limitations

1. **Mock Data**: Currently returns placeholder subtitle text
2. **No Persistence**: Subtitles not cached (in-memory only)
3. **Single Language**: No language selection (extension handles)
4. **No Retry**: User must manually retry failed downloads
5. **No Cancellation**: Can't cancel ongoing download

## Future Enhancements

- [ ] Language selection dropdown
- [ ] Subtitle caching (Room DB)
- [ ] Export to file functionality
- [ ] Share to ChatGPT/Claude integration
- [ ] Dark mode toggle
- [ ] Subtitle search/filter
- [ ] Download history
- [ ] Batch download for playlists
- [ ] Settings screen
- [ ] Error retry mechanism

## Testing Strategy

See [Coding Standards](../docs/coding-standards.md) for testing guidelines.

### Unit Tests (to be implemented)

- Use Cases: Mock repository, test business logic
- ViewModels: Mock use cases, verify state changes
- Repository: Mock data sources, test mapping

### UI Tests (to be implemented)

- Home Screen: Test URL input, button clicks, state rendering
- Integration: Test end-to-end flow with mock extension

## Documentation Index

**Shared Documentation** (`docs/`):
- [Architecture Guidelines](../docs/architecture.md) - Clean Architecture, MVVM, dependency flow
- [Coding Standards](../docs/coding-standards.md) - Naming, state management, error handling
- [Build Commands](../docs/build-commands.md) - Build, test, lint commands
- [Dependencies Guide](../docs/dependencies.md) - All dependencies, version compatibility
- [Logging Guide](../docs/logging.md) - Logging patterns, best practices
- [Troubleshooting](../docs/troubleshooting.md) - Common issues and solutions
- [YouTube InnerTube API Guide](../docs/youtube-innertube-api.md) - How extension works

**Related Documentation**:
- [Root CLAUDE.md](../CLAUDE.md) - Project-level guidelines
- [Extension CLAUDE.md](../extensions/youtubeSubtitleDownloader/CLAUDE.md) - Extension module

---

**Last Updated**: 2025-11-09
**App Version**: 1.0.0 (versionCode 1)

For complete details on any topic, refer to the linked documentation files in the `docs/` directory.
