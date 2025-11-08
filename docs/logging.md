# Logging Guide

This document describes the logging system used across the SummaryAI project.

## Overview

SummaryAI uses a centralized logging system with consistent patterns across all modules.

## App Logging

### Logger Utility

**Location**: `app/src/main/java/com/abhishek/summaryai/util/Logger.kt`
**Tag**: `"SummaryAI"`
**Default Level**: `VERBOSE`
**Built on**: Timber library

### Available Methods

```kotlin
// Standard log levels
Logger.logV(message: String, throwable: Throwable? = null)  // VERBOSE
Logger.logD(message: String, throwable: Throwable? = null)  // DEBUG
Logger.logI(message: String, throwable: Throwable? = null)  // INFO
Logger.logW(message: String, throwable: Throwable? = null)  // WARN
Logger.logE(message: String, throwable: Throwable? = null)  // ERROR

// Method tracing
Logger.logEntry(methodName: String)  // Log method entry
Logger.logExit(methodName: String)   // Log method exit
```

### Initialization

The logger is initialized in the Application class:

```kotlin
class SummaryAiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.init()  // Initialize Timber
    }
}
```

## Extension Logging

### YouTube Subtitle Downloader

**Tag**: `"summaryaiYouTubeSubtitleDownloader"`
**Default Level**: `VERBOSE`
**Location**: `extensions/youtubeSubtitleDownloader/src/main/java/com/abhishek/youtubesubtitledownloader/util/SubtitleLogger.kt`

**Log Levels**: `VERBOSE` | `DEBUG` | `INFO` | `WARN` | `ERROR` | `NONE`

**Configuration**:
```kotlin
YouTubeSubtitleDownloader.setLogLevel(LogLevel.ERROR)
```

## When to Log

### INFO Level
- Important milestones
- Application/feature lifecycle events
- Successful operations
- User actions

**Examples**:
```kotlin
Logger.logI("Application started")
Logger.logI("HomeViewModel: Subtitle download completed successfully")
Logger.logI("User clicked Extract Subtitle button")
```

### DEBUG Level
- Development information
- State changes
- API responses
- Configuration values

**Examples**:
```kotlin
Logger.logD("HomeViewModel: State changed to Loading")
Logger.logD("API response received: ${response.size} bytes")
Logger.logD("Cache hit for API key")
```

### VERBOSE Level
- Detailed flow information
- Method entry/exit
- Parameter values
- Detailed execution steps

**Examples**:
```kotlin
Logger.logV("HomeViewModel.onEvent called with: $event")
Logger.logEntry("downloadSubtitles")
Logger.logV("Processing subtitle track: languageCode=$code")
```

### WARN Level
- Non-fatal issues
- Retry attempts
- Fallback scenarios
- Deprecated usage

**Examples**:
```kotlin
Logger.logW("API key extraction failed, retrying...")
Logger.logW("Preferred language not found, using fallback")
Logger.logW("Cache expired, fetching fresh data")
```

### ERROR Level
- Failures and exceptions
- Network errors
- Parse errors
- Validation failures

**Examples**:
```kotlin
Logger.logE("Failed to download subtitle", exception)
Logger.logE("Network error: ${error.message}")
Logger.logE("Invalid YouTube URL: $url")
```

## Log Message Format

### Recommended Pattern

```kotlin
"ClassName: Action - details"
```

### Examples

```kotlin
// Good
Logger.logI("HomeViewModel: Starting subtitle download for: $url")
Logger.logD("SubtitleRepositoryImpl: Fetching from API")
Logger.logE("DownloadSubtitlesUseCase: Validation failed - invalid URL")

// Avoid
Logger.logI("Starting download")  // Too vague
Logger.logD(url)  // No context
Logger.logE("Error")  // Not helpful
```

### With Exceptions

```kotlin
Logger.logE("HomeViewModel: Download failed", exception)
Logger.logW("ApiKeyCache: Cache read error", ioException)
```

## Logging Across Layers

### Application Layer

```kotlin
class SummaryAiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.init()
        Logger.logI("SummaryAiApplication: Application created")
    }
}
```

### Presentation Layer (ViewModels)

```kotlin
class HomeViewModel @Inject constructor(
    private val downloadSubtitlesUseCase: DownloadSubtitlesUseCase
) : ViewModel() {

    init {
        Logger.logI("HomeViewModel: ViewModel initialized")
    }

    fun onEvent(event: HomeUiEvent) {
        Logger.logV("HomeViewModel.onEvent: $event")
        when (event) {
            is HomeUiEvent.DownloadSubtitle -> {
                Logger.logI("HomeViewModel: Starting subtitle download for: ${event.url}")
                downloadSubtitle(event.url)
            }
        }
    }

    private fun downloadSubtitle(url: String) {
        Logger.logEntry("downloadSubtitle")
        // ... implementation
        Logger.logExit("downloadSubtitle")
    }
}
```

### Domain Layer (Use Cases)

```kotlin
class DownloadSubtitlesUseCase @Inject constructor(
    private val repository: SubtitleRepository
) {
    suspend operator fun invoke(url: String): Result<String> {
        Logger.logEntry("DownloadSubtitlesUseCase.invoke")
        Logger.logI("DownloadSubtitlesUseCase: Downloading subtitles for: $url")

        val result = repository.downloadSubtitles(url)

        when (result) {
            is Result.Success -> Logger.logI("DownloadSubtitlesUseCase: Success")
            is Result.Error -> Logger.logE("DownloadSubtitlesUseCase: Error - ${result.message}")
        }

        Logger.logExit("DownloadSubtitlesUseCase.invoke")
        return result
    }
}
```

### Data Layer (Repositories)

```kotlin
class SubtitleRepositoryImpl @Inject constructor() : SubtitleRepository {
    override suspend fun downloadSubtitles(url: String): Result<String> {
        Logger.logEntry("SubtitleRepositoryImpl.downloadSubtitles")
        Logger.logD("SubtitleRepositoryImpl: Processing URL: $url")

        try {
            // ... implementation
            Logger.logI("SubtitleRepositoryImpl: Download successful")
            return Result.Success(subtitle)
        } catch (e: Exception) {
            Logger.logE("SubtitleRepositoryImpl: Download failed", e)
            return Result.Error(e.message ?: "Unknown error", e)
        } finally {
            Logger.logExit("SubtitleRepositoryImpl.downloadSubtitles")
        }
    }
}
```

## Production vs Debug Logging

### Debug Builds

All log levels enabled by default.

### Release Builds (Future)

Recommended configuration:
- Disable VERBOSE and DEBUG logs
- Keep INFO, WARN, ERROR logs
- Consider using ProGuard to remove log statements

```kotlin
// In Logger.kt
object Logger {
    private val isDebug = BuildConfig.DEBUG

    fun logV(message: String, throwable: Throwable? = null) {
        if (isDebug) {
            Timber.v(throwable, message)
        }
    }

    fun logD(message: String, throwable: Throwable? = null) {
        if (isDebug) {
            Timber.d(throwable, message)
        }
    }

    // INFO, WARN, ERROR always logged
    fun logI(message: String, throwable: Throwable? = null) {
        Timber.i(throwable, message)
    }
}
```

## Viewing Logs

### Logcat (Android Studio)

1. Open Logcat tab
2. Filter by tag: `SummaryAI` or `summaryaiYouTubeSubtitleDownloader`
3. Select log level

### ADB Command Line

```bash
# View app logs only
adb logcat -s SummaryAI

# View extension logs only
adb logcat -s summaryaiYouTubeSubtitleDownloader

# View both
adb logcat -s SummaryAI:V summaryaiYouTubeSubtitleDownloader:V

# Set log level
adb shell setprop log.tag.SummaryAI VERBOSE

# Clear logs
adb logcat -c
```

## Best Practices

### Do's

✅ Log important milestones and state changes
✅ Include context (class name, action, details)
✅ Log exceptions with stack traces
✅ Use appropriate log levels
✅ Log method entry/exit for complex flows
✅ Include relevant parameter values (sanitized)

### Don'ts

❌ Log sensitive information (passwords, tokens, PII)
❌ Log in tight loops (use sparingly)
❌ Log entire large objects (log summary instead)
❌ Use println() or System.out instead of Logger
❌ Log without context ("Error" vs "HomeViewModel: Download failed")
❌ Over-log trivial operations

### Sensitive Data

Never log:
- User credentials (passwords, API keys)
- Personal information (email, phone, address)
- Financial data (credit cards, transactions)
- Session tokens or auth tokens

If you must log URLs or data, sanitize them:

```kotlin
// Bad
Logger.logD("API request: $fullUrl")  // May contain tokens

// Good
val sanitizedUrl = url.substringBefore("?")  // Remove query params
Logger.logD("API request: $sanitizedUrl")
```

## Troubleshooting

### Logs Not Appearing

**Problem**: Timber not initialized
- **Solution**: Ensure `Logger.init()` is called in `Application.onCreate()`

**Problem**: Log level too high
- **Solution**: Check device log level settings with `adb shell getprop log.tag.SummaryAI`

**Problem**: Tag filter not working
- **Solution**: Verify exact tag name (case-sensitive)

### Too Many Logs

**Problem**: Verbose logs cluttering output
- **Solution**: Use log level filters in Logcat or adb
- **Solution**: Consider setting default level to INFO in production

## Related Documentation

- [Coding Standards](./coding-standards.md)
- [Architecture Guidelines](./architecture.md)
