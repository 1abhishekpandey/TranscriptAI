# TranscriptAI

A modern Android application that enables seamless YouTube video transcript extraction and AI-powered analysis. Download subtitles, view them instantly, and export to your favorite AI platforms.

## Features

- **YouTube Transcript Download**: Extract subtitles from any YouTube video using direct InnerTube API integration
- **Clean Subtitle Viewer**: Intuitive scrollable interface for reading transcripts
- **Smart Export Options**:
  - Copy to clipboard
  - Direct share to ChatGPT/Claude apps
  - Export as text file
- **Self-Contained Extension**: Reusable YouTube subtitle downloader module

## Tech Stack

- **100% Kotlin** with Jetpack Compose
- **Clean Architecture** + MVVM pattern
- **Kotlin Coroutines** for async operations
- **Material3** design system
- **Min SDK**: 25 (Android 7.1) | **Target SDK**: 36 (Android 15)

## Quick Start

```bash
# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Install on device
./gradlew installDebug
```

## Architecture

```
Presentation Layer (Compose UI)
        ↓
   ViewModels
        ↓
    Use Cases
        ↓
  Repositories
        ↓
Data Sources (API/Local)
```

See [docs/architecture.md](docs/architecture.md) for detailed architecture guidelines.

## Future Scope (Based on needs)

### Multi-Platform Support
- **Video Platforms**: Extend support to other video platforms
- **Social Media**: Support for Instagram, Twitter/X video transcripts
- **File Import**: Load existing text/transcript files from device storage

### Built-in LLM Integration
- **On-Device AI**: Integrate lightweight LLMs for offline transcript analysis
- **Smart Summarization**: Generate concise summaries directly within the app
- **Key Points Extraction**: Automatically highlight important moments
- **Multi-Language Translation**: Translate transcripts on-the-fly
- **Custom Prompts**: User-defined AI analysis templates
- **Hybrid Approach**: Option to use cloud LLMs (GPT/Claude API) or on-device models based on user preference

## Documentation

- [Architecture Guidelines](docs/architecture.md)
- [Coding Standards](docs/coding-standards.md)
- [Build Commands](docs/build-commands.md)
- [YouTube InnerTube API Guide](docs/youtube-innertube-api.md)

## License

This project is licensed under the MIT License.

---

**Made with ❤️ by Vibe Coding!**
