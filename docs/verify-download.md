# Verify your download

This page shows how to check that the APK you downloaded is safe to install. There are two checks:

1. **Is it the same file that was virus-scanned?** You check this with the file's SHA-256 fingerprint.
2. **Was it signed by the TranscriptAI developer?** You check this with the signing certificate.

Each check takes a minute. You need a computer for the commands, or a hash checker app on your phone.

## How the virus scan works

- Each time a new version is published on the [Releases page](https://github.com/1abhishekpandey/TranscriptAI/releases/latest), GitHub uploads its APK to [VirusTotal](https://www.virustotal.com).
- VirusTotal checks the file with about 60 to 70 antivirus scanners.
- The badge at the top of the README shows the result for the **latest version only**. For example, `VirusTotal v2.1.0 | clean 0/67` means 67 scanners checked version 2.1.0 and none of them flagged it.
- Click the badge to open the full VirusTotal report.
- Only GitHub and VirusTotal take part. No other service is used.

The badge is green when no scanner flags the file and red when at least one does. Small, lesser-known scanners sometimes flag apps that are not on the Play Store by mistake. Open the report to see which scanner flagged it and why.

## Check 1: the file is the one that was scanned

### What a SHA-256 fingerprint is

- A SHA-256 fingerprint is a 64-character code calculated from every byte of a file.
- Two identical files always give the same code.
- Change a single byte and the code changes completely.
- So if your file has the same fingerprint as the scanned file, it **is** the scanned file.

Example: `14f94b26c2dfcaec80d95947ac30cc4e94fb10e7be4d610fd4c11888187f3389` is the fingerprint of `TranscriptAI-v2.1.0.apk`.

### Step 1: find the fingerprint of the scanned file

- Click the VirusTotal badge in the README.
- The report's web address ends with the fingerprint: `https://www.virustotal.com/gui/file/<fingerprint>`.
- The report page also shows it at the top, and in full on its **Details** tab under **SHA-256**.

### Step 2: find the fingerprint of the published file

- Open the [Releases page](https://github.com/1abhishekpandey/TranscriptAI/releases/latest).
- Under **Assets**, GitHub shows `sha256:` followed by the fingerprint next to the APK.

### Step 3: calculate the fingerprint of your download

Run the command for your system in the folder where the APK is saved. Replace the file name with the version you downloaded.

**macOS**

```bash
shasum -a 256 TranscriptAI-v2.1.0.apk
```

**Linux**

```bash
sha256sum TranscriptAI-v2.1.0.apk
```

**Windows (PowerShell)**

```powershell
Get-FileHash TranscriptAI-v2.1.0.apk -Algorithm SHA256
```

**Windows (Command Prompt)**

```bat
certutil -hashfile TranscriptAI-v2.1.0.apk SHA256
```

**Android**

Install any hash checker app, open the APK with it and choose SHA-256.

Windows prints the fingerprint in capital letters. That is fine: capital and small letters mean the same thing here.

### Step 4: compare

| Fingerprint from | Where |
| --- | --- |
| The scanned file | VirusTotal report |
| The published file | GitHub Releases page |
| Your download | The command in step 3 |

All three are the same: your APK is exactly the file VirusTotal scanned. If any one is different, do not install the file. Download it again from the Releases page.

## Check 2: the file was signed by the developer

Every Android app is signed with its developer's private key. Only the developer has this key. Android uses the signature to make sure an update comes from the same developer as the app already on your phone.

TranscriptAI's signing certificate has this SHA-256 fingerprint:

```
12:0F:1E:E4:BC:85:EB:67:40:DC:82:8E:80:58:5C:03:CE:56:5C:DF:11:6C:BB:3B:0C:34:0E:1E:A5:50:E5:BC
```

It is the same for every release from 2.0.0 onwards. Versions 1.3 and earlier were signed with an older key.

To check it, use either tool:

**With Java installed** (`keytool` comes with Java):

```bash
keytool -printcert -jarfile TranscriptAI-v2.1.0.apk
```

Look for the `SHA256:` line under `Certificate fingerprints`. The owner shows as `CN=Abhishek Pandey, O=TranscriptAI`.

**With the Android SDK installed** (`apksigner` is in the SDK's `build-tools` folder):

```bash
apksigner verify --print-certs TranscriptAI-v2.1.0.apk
```

Look for the `certificate SHA-256 digest` line. It shows the same fingerprint without colons, in small letters.

If the fingerprint is different, the APK was not signed by the TranscriptAI developer. Do not install it.

## What these checks prove

- **They prove** your file is byte-for-byte the file VirusTotal scanned and the file published on GitHub.
- **They prove** the file was signed with the TranscriptAI developer's key.
- **They do not prove** the APK was built from the source code in this repository. To be sure of that, build the app yourself. See [Build Commands](build-commands.md).
- **A clean scan is not a guarantee.** It means no scanner recognised anything harmful in the file when it was checked.
