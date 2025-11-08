# Build Commands

## Build

```bash
./gradlew assembleDebug              # Debug build
./gradlew assembleRelease            # Release build
./gradlew build                      # Build all variants
```

## Run Tests

```bash
./gradlew test                       # Unit tests
./gradlew connectedAndroidTest       # Instrumented tests
./gradlew testDebugUnitTest          # Single variant unit tests
```

## Code Quality

```bash
./gradlew lint                       # Run lint checks
./gradlew lintDebug                  # Lint for debug variant
```

## Clean

```bash
./gradlew clean                      # Clean build artifacts
```

## Install

```bash
./gradlew installDebug               # Install debug APK
```

## Module-Specific Commands

### YouTube Subtitle Downloader Extension

```bash
# Build module
./gradlew :extensions:youtubeSubtitleDownloader:build

# Run tests
./gradlew :extensions:youtubeSubtitleDownloader:test

# Generate AAR
./gradlew :extensions:youtubeSubtitleDownloader:assembleRelease
# Output: extensions/youtubeSubtitleDownloader/build/outputs/aar/
```
