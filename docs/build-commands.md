# Build Commands

## Environment

- **JDK**: Use a current JDK (21 or newer). Android Studio's bundled JBR works:
  ```bash
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  ```
  The path differs by OS and install location.
- **Android SDK**: `ANDROID_HOME` must be set, or `local.properties` must contain `sdk.dir=/path/to/Android/sdk`. `local.properties` is git-ignored.
- **Gradle**: Use the wrapper (`./gradlew`). It downloads Gradle 9.8.

## Build

```bash
./gradlew assembleDebug              # Debug build
./gradlew assembleRelease            # Release build (unsigned: the repo has no signing config)
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
