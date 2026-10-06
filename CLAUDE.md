# CLAUDE.md

Guidance for Claude Code when working in this repository.

## What this is

**TranscriptAI** (app label "Transcript AI") is an Android app. It downloads a YouTube video's transcript, shows it, and sends it to ChatGPT or Claude, with an optional prompt in front.

- Package: `com.abhishek.transcriptai`
- Min SDK 25 | Target SDK 36
- Kotlin, Jetpack Compose, Material3, Hilt, Room, Coroutines/Flow
- Clean Architecture + MVVM: Presentation → Domain ← Data. Domain is pure Kotlin.
- The subtitle downloader is a separate module: `extensions/youtubeSubtitleDownloader/`.

## Toolchain

Versions live in `gradle/libs.versions.toml`. The non-obvious parts:

- Gradle 9.8, Android Gradle Plugin 9.4.
- AGP 9 has **built-in Kotlin**. Do not add the `kotlin-android` plugin to a module.
- Kotlin 2.4 with the Compose compiler Gradle plugin (`kotlin-compose`). No `composeOptions`, no `kotlinOptions`.
- KSP 2.3, Hilt 2.60, Room 2.8.
- Java 21 target. Builds on current JDKs. Android Studio's bundled JBR works.
- The Android SDK location must be set: `ANDROID_HOME`, or `sdk.dir` in `local.properties` (git-ignored).

See [Build Commands](docs/build-commands.md).

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

## Release signing

- There is no signing config in the repo.
- The release key and passwords live outside the repo.
- Never commit `*.jks`, keystores or passwords.

## Screens

Navigation is in `presentation/navigation/`.

- **Home**: URL input, language choice, auto-forward bar ("Auto-forward from YouTube"; called auto-share in code).
- **Summariser** (the transcript page): title, language switcher, search, prompt selector, share sheet.
- **Prompt editor**: add and edit prompts.
- **Language settings**: 40+ languages with search, opened from Home ("Other languages").
- **Version input**: manual YouTube client version, shown when auto-probing fails.

## Key facts and gotchas

### Language choice

- Saved in SharedPreferences (`LanguagePreferences`), exposed through `LanguagePreferenceRepository`.
- Two modes: auto-generated, or the language last picked on the settings page.
- The language list is `domain/model/SubtitleLanguage.kt`. A language can have several YouTube codes, tried in order (for example Hebrew is `iw` on older videos, `he` on newer ones).
- `LanguageChoice.toLanguagePreferences()` turns the choice into the code list passed to the downloader. `"auto"` means YouTube's speech-generated captions.
- The Home screen and the auto-share flow both read the saved choice.

### Sharing

- Share to ChatGPT or Claude: `ACTION_SEND` first, then clipboard + launch the app as a fallback.
- Android 11+ needs the `<queries>` block in `AndroidManifest.xml` to detect the ChatGPT and Claude apps. Keep it.
- `MainActivity` handles `ACTION_VIEW` (YouTube links) and `ACTION_SEND` (YouTube share sheet).
- Auto-share: a share from YouTube with the toggle on stores the URL in `SubtitleCacheRepository`, opens the Summariser in auto-download mode, shares to the chosen app, then closes the app.
- `SubtitleCacheRepository` is an in-memory hand-off between screens. Nothing is persisted.

### Storage

- Prompts: Room (`AppDatabase`). Default prompts are seeded.
- App settings (AI toggle, selected prompt, auto-share, language): SharedPreferences.

### YouTube client version

- The extension uses YouTube's InnerTube API with the ANDROID client. YouTube rejects old client versions.
- The extension probes newer versions on its own. If that fails, the app shows the Version input screen.
- Details and the do-not-send-a-User-Agent rule: [YouTube InnerTube API Guide](docs/youtube-innertube-api.md).

### Logging

- Use `Logger` (`util/Logger.kt`, tag `TranscriptAI-Log`), not `Log` or Timber directly.
- The extension has its own logger. See [Logging Guide](docs/logging.md).

## Extension: YouTube Subtitle Downloader

- Location: `extensions/youtubeSubtitleDownloader/`. Self-contained library module, no dependency on app code.
- The app uses it through `data/repository/SubtitleRepositoryImpl.kt`, injected by Hilt (`di/AppModule.kt`).
- Module docs: [extensions/youtubeSubtitleDownloader/CLAUDE.md](extensions/youtubeSubtitleDownloader/CLAUDE.md).

```kotlin
val downloader = YouTubeSubtitleDownloader.getInstance(context)
val result = downloader.downloadSubtitles(
    url = "https://www.youtube.com/watch?v=VIDEO_ID",
    languagePreferences = listOf("hi", "en", "auto")
)
```

## Conventions

- Use cases: `VerbNounUseCase`. ViewModels: `FeatureViewModel`. Repositories: `EntityRepository`.
- UI state and events: `FeatureUiState`, `FeatureUiEvent`.
- UI state is an immutable data class in a `StateFlow`, collected with `collectAsState()`.
- Domain results use the `Result` sealed class.

More: [Coding Standards](docs/coding-standards.md), [Architecture](docs/architecture.md).

## Documentation

- [docs/architecture.md](docs/architecture.md): layers, MVVM, dependency flow
- [docs/coding-standards.md](docs/coding-standards.md): naming, state, errors, tests
- [docs/build-commands.md](docs/build-commands.md): build, test, lint, install
- [docs/dependencies.md](docs/dependencies.md): what each dependency is for, compatibility notes
- [docs/logging.md](docs/logging.md): logging patterns
- [docs/troubleshooting.md](docs/troubleshooting.md): common problems
- [docs/youtube-innertube-api.md](docs/youtube-innertube-api.md): how the extension works
- [app/CLAUDE.md](app/CLAUDE.md): app module notes
- [extensions/youtubeSubtitleDownloader/CLAUDE.md](extensions/youtubeSubtitleDownloader/CLAUDE.md): extension notes

---

**Last Updated**: 2026-10-07
