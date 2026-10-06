# TranscriptAI

An Android app that gets the transcript of any YouTube video and sends it to ChatGPT or Claude with a prompt, in a few taps.

Share a video from the YouTube app, and the app can forward its transcript straight to your AI app. No copy-pasting.

## Features

### Get a transcript

- Paste a YouTube link, or use **Share → Transcript AI** in the YouTube app.
- Works with `youtube.com`, `youtu.be`, mobile and live links.

### Pick the language

- **Auto-generated** (default): the captions YouTube makes from the video's speech.
- **Your last pick**: the language you chose last, shown in its own script (for example हिन्दी).
- **Other languages**: opens a settings page with 40+ languages, each written in its own script, with search.
- Your choice is remembered.
- Home has a "Try a sample video" link (a TED talk with 50+ caption languages) so you can try it out.

### Read the transcript

- Video title, and a button to open the video in YouTube.
- Switch to any caption language the video has.
- A note when your language is not available, telling you which one is shown instead.
- Search inside the transcript.
- Copy it, with a word count.

### Add an AI prompt

- Turn on the AI summariser to put a prompt in front of the transcript, for example "Summarise this".
- Pick from saved prompts, or add and edit your own.

### Share

- Send to **ChatGPT** or **Claude**, or copy to the clipboard.
- If the app does not accept shared text directly, the transcript is copied and the app opens, so you can paste it.
- **Auto-forward**: when on, sharing a video from YouTube goes straight to ChatGPT or Claude with your prompt. You choose which app.

### Keeps working

- YouTube sometimes rejects the client version the app uses. The app tries newer versions by itself.
- If that fails, a **Fix version** screen lets you enter a version by hand.

## How to use

1. Open a video in the YouTube app and tap **Share → Transcript AI**. Or paste the link in the app.
2. Pick a language if you do not want auto-generated captions.
3. Read the transcript, or add a prompt.
4. Tap the share button and choose ChatGPT, Claude or Copy.

## Install

1. Download the latest APK from the [Releases page](https://github.com/1abhishekpandey/TranscriptAI/releases/latest).
2. Open it on your phone and allow installs from your browser or file manager if Android asks.

**Requires Android 7.1 or newer.**

### Updating from version 1.3 or earlier

- Versions 1.3 and earlier were signed with a key that is no longer available.
- To install a newer version, uninstall the old app first.
- Your saved prompts and settings will be lost.

## Building from source

- Android Studio and JDK 21 or newer.
- `./gradlew assembleDebug`

## Also see

[FastScribe](https://github.com/1abhishekpandey/FastScribe/) turns video files into text on a PC, using OpenAI's Whisper, with fast parallel transcription.

## License

MIT License.

---

**Made with ❤️ by Vibe Coding!**
