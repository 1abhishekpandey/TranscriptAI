# TranscriptAI App Module

Guidance for Claude Code when working in the `app` module. Project-wide facts are in the [root CLAUDE.md](../CLAUDE.md). This file only adds what is specific to the app module.

## Module facts

- Package: `com.abhishek.transcriptai`. The application class is `TranscriptAiApplication`.
- Architecture: Presentation → Domain ← Data. Domain is pure Kotlin. See [Architecture](../docs/architecture.md).
- The module applies `kotlin-compose`, `ksp` and `hilt` plugins. There is no `kotlin-android` plugin (AGP 9 has built-in Kotlin).
- Java 21 target. Release builds are unsigned. The repo has no signing config, and the release key and passwords stay outside the repo.
- Tests: JUnit unit tests for pure logic under `src/test`. No ViewModel or UI tests yet.

## Where things are

Paths are under `app/src/main/java/com/abhishek/transcriptai/`.

- `presentation/<screen>/`: one package per screen (`home`, `summariser`, `prompteditor`, `languagesettings`, `versioninput`, `settings`, `aiprompts`), each with its `Screen`, `ViewModel`, `UiState` and sometimes `UiEvent`.
- `presentation/navigation/`: destinations and the nav graph. Home, Summariser, Prompt editor, Version input, Language settings, Settings and AI prompts are routes here.
- `domain/`: models, repository interfaces and use cases.
- `data/`: repository implementations, Room (`local/database`), SharedPreferences wrappers (`local/`), mappers, and default prompts (`seed/`).
- `di/`: `AppModule` (singleton providers) and `UseCaseModule` (ViewModel-scoped use cases).
- `util/`: `Logger`, clipboard and share helpers.
- `ui/theme/`: theme, colours, typography.

## Screens

- **Home**: URL input, language choice, and the auto-forward bar (auto-share in code). Opens Language settings, Version input and Settings (gear icon in the top bar).
- **Summariser** (transcript page): shows the transcript, the AI toggle, the prompt selector and the share sheet. It also has the language switcher for the video's caption tracks.
- **Prompt editor**: create and edit prompts.
- **Language settings**: 40+ languages, searchable, each in its own script. Home has a "Try a sample video" link that opens a TED talk with 50+ caption languages, without auto-forwarding.
- **Version input**: enter a YouTube client version by hand when auto-probing fails. Also reachable from Settings.
- **Settings**: an "AI prompts" row (on/off and the selected prompt), the YouTube client version with a link to Version input, and About (app version, source code link).
- **AI prompts**: the AI prompt switch and the saved prompts (tap to select, edit, delete; the last prompt cannot be deleted), Add prompt, and Reset to defaults (replaces every prompt with the built-in ones in one transaction and selects the first).

## Language choice

- `LanguagePreferences` (SharedPreferences) stores the mode and the language picked on the settings page.
- `LanguagePreferenceRepository` exposes it as a `Flow` and as one-shot reads.
- The language list is `domain/model/SubtitleLanguage.kt` (`SubtitleLanguages.all`, sorted by English name).
  - Native name is in the language's own script. English name is the search hint.
  - `codes` are YouTube caption codes tried in order. Add old and new codes when YouTube uses both.
- `LanguageChoice.toLanguagePreferences()` gives the list passed to `DownloadSubtitlesUseCase`. `"auto"` means speech-generated captions.
- The downloader result (`SubtitleResult` in `domain/model/Subtitle.kt`) carries the track that was used, whether the requested language matched, and every caption track the video has. The Summariser uses this for the not-available note and the switcher.

## Sharing and auto-share

- `AIShareRepositoryImpl`: `ACTION_SEND` to the app, then clipboard + launch as a fallback. App-specific intent flags matter for ChatGPT and Claude.
- `FormatSubtitleForCopyUseCase` puts the selected prompt in front of the transcript when the AI summariser is on.
- Auto-share flow: `MainActivity` extracts the YouTube URL from `ACTION_SEND`. `SummariserNavGraph` stores it in `SubtitleCacheRepository` and opens the Summariser in auto-download mode. `SummariserViewModel` downloads with the saved language, shares to the chosen app and finishes the activity. If the app is not installed it opens the share sheet.
- `SubtitleCacheRepository` is in-memory only. Transcripts are lost on process death.

## Adding things

- **A feature**: model → repository interface → use case in `domain/`. Implementation in `data/`. State, ViewModel and screen in `presentation/<feature>/`. Provide in `di/`.
- **A language**: add one `SubtitleLanguage` entry in English-name order. Check the YouTube code, including legacy codes.
- **A screen**: add a destination in `SummariserDestination` and a `composable` in `SummariserNavGraph`.
- **A dependency**: add it to `gradle/libs.versions.toml`, then the module. See [Dependencies](../docs/dependencies.md).
- **The extension**: see [extensions/youtubeSubtitleDownloader/CLAUDE.md](../extensions/youtubeSubtitleDownloader/CLAUDE.md). The app calls it through `SubtitleRepositoryImpl`.

## Gotchas

- Android 11+ package visibility: the `<queries>` block in `AndroidManifest.xml` is what lets the app see ChatGPT and Claude. Keep it when editing the manifest.
- Deep links: `youtube.com/watch`, `m.youtube.com/watch` and `youtu.be` are handled by `MainActivity` (`singleTop`), so new links arrive in `onNewIntent`.
- Logs use `Logger` (tag `TranscriptAI-Log`). View them with `adb logcat -s TranscriptAI-Log`.
- ACTION_SEND support in ChatGPT and Claude is not officially documented. The clipboard fallback makes sure sharing always works.
- Hilt or KSP errors after a toolchain change: upgrade Kotlin, KSP, Hilt, Room and AGP together. See [Troubleshooting](../docs/troubleshooting.md).

## Documentation

- [Architecture](../docs/architecture.md)
- [Coding Standards](../docs/coding-standards.md)
- [Build Commands](../docs/build-commands.md)
- [Dependencies](../docs/dependencies.md)
- [Logging](../docs/logging.md)
- [Troubleshooting](../docs/troubleshooting.md)
- [YouTube InnerTube API](../docs/youtube-innertube-api.md)

---

**Last Updated**: 2026-10-07
