YouTube InnerTube API Guide

## Overview

The InnerTube API is YouTube's undocumented internal API used for fetching video metadata, including subtitle/caption tracks.

**Important Notes:**
- This API is not officially documented
- YouTube can change it without notice
- May violate YouTube Terms of Service
- Use responsibly and ethically

## The 3-Step Flow

### Step 1: Extract INNERTUBE_API_KEY

**Purpose**: Get the API key needed to call InnerTube endpoints

**Process**:
1. Fetch YouTube video page HTML with a browser User-Agent
2. Use regex to extract `"INNERTUBE_API_KEY":"[key]"` from embedded JavaScript
3. Cache key with timestamp (recommended: 24h TTL)

**Example Regex**:
```kotlin
"INNERTUBE_API_KEY"\\s*:\\s*"([^"]+)"
```

**User-Agent requirement**: The page HTML fetch is the only request that needs a browser User-Agent to get the full HTML response:
```
Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36
```

### Step 2: Call InnerTube Player API

**Purpose**: Get caption track metadata

**Endpoint**:
```
POST https://www.youtube.com/youtubei/v1/player?key={API_KEY}
```

**Request Body**:
```json
{
  "context": {
    "client": {
      "clientName": "ANDROID",
      "clientVersion": "20.10.38"
    }
  },
  "videoId": "VIDEO_ID"
}
```

**Critical: Client selection**:
- MUST use `clientName: "ANDROID"` with a recent version (e.g., `"20.10.38"`)
- Using `clientName: "WEB"` returns `playabilityStatus: UNPLAYABLE`
- Other clients (`TVHTML5`, `IOS`, `WEB_CREATOR`, `MWEB`, `WEB_EMBEDDED_PLAYER`) are all blocked with `LOGIN_REQUIRED`, `FAILED_PRECONDITION`, or `ERROR`

**Critical: Do NOT send a custom User-Agent**:
- The default OkHttp User-Agent works fine
- Sending a browser User-Agent (e.g., `Mozilla/5.0...`) with ANDROID client causes HTTP 400
- Sending an Android YouTube app User-Agent (e.g., `com.google.android.youtube/...`) also causes HTTP 400

**Response Path**:
```
captions.playerCaptionsTracklistRenderer.captionTracks[]
```

**Track Object**:
```json
{
  "baseUrl": "https://www.youtube.com/api/timedtext?v=...",
  "languageCode": "en",
  "kind": "asr",
  "name": { "simpleText": "English" }
}
```

### Step 3: Fetch Transcript XML

**Purpose**: Download actual subtitle content

**Endpoint**: Use `baseUrl` from caption track, with `&fmt=srv3` stripped (see below)

**Do NOT send a custom User-Agent** for this request either - the default OkHttp User-Agent works.

**Response Format** (default XML, no `fmt` param):
```xml
<transcript>
  <text start="0" dur="2.5">Hello</text>
  <text start="2.5" dur="3.1">Welcome to the video</text>
  ...
</transcript>
```

**Parsing**:
- Extract text from `<text>` elements
- Decode HTML entities (`&amp;`, `&lt;`, etc.)
- Join text with spaces or newlines

## PO Token and timedtext URL Differences

YouTube now requires PO (Proof of Origin) tokens for many InnerTube API calls. This directly affects which timedtext URLs work.

**URLs from page HTML** (`ytInitialPlayerResponse`):
- Contain protection params: `xoaf=5`, `xowf=1`, `xospf=1`, `exp=xpe`
- The `exp=xpe` parameter requires a PO token to return content
- Without a PO token, these URLs fail or return empty responses

**URLs from ANDROID player API**:
- Lack the `exp=xpe` and related protection params
- Work without PO tokens and return actual subtitle content
- This is the primary reason the ANDROID client approach works

## timedtext URL Format

**Strip `&fmt=srv3`** from the `baseUrl` returned by the API. The SRV3 format uses a different XML structure with `<p>` and `<s>` tags. Removing the `fmt` parameter returns the simpler XML format with `<text start="..." dur="...">` tags, which is easier to parse.

**Default format** (no `fmt` param): XML with `<text start="0" dur="1.5">` tags
**SRV3 format** (`fmt=srv3`): XML with `<p>` paragraph and `<s>` segment tags

## Client Version Rotation Strategy

ANDROID client versions get outdated periodically — YouTube rejects old versions with HTTP 400, and unrecognised future versions with HTTP 404.

**Automated probing (self-healing):**
1. On HTTP 400 from player API (version too old), probe upward by incrementing the major version
2. Try major+1, major+2, ... up to major+5 (keeping the same minor.patch)
3. On HTTP 200 with caption tracks: cache the working version, done
4. On HTTP 404 (too new): stop probing — overshot the valid range
5. On HTTP 400 (still too old): continue probing upward

**Manual override:**
If probing fails (all 5 candidates rejected), the app shows a Version Input screen where the user can:
1. Enter a version number manually (format: `major.minor.patch`, e.g., `21.10.38`)
2. Test it against YouTube's API
3. Save it for future use

**How to find the current YouTube Android app version:**
1. Open Google Play Store
2. Search for "YouTube"
3. Tap on the YouTube app → scroll to "About this app"
4. Note the version number (e.g., `20.10.38`)

**Cache keys** (SharedPreferences):
- `android_client_version`: Last working ANDROID client version string
- `android_client_version_timestamp`: Long (milliseconds since epoch)

## API Key Caching Strategy

**Why Cache**: Fetching the key on every request is slow and unnecessary

**Implementation**:
- **First request**: Fetch key from YouTube page, cache with timestamp
- **Subsequent requests**: Use cached key if age < TTL
- **On API failure**: Clear cache, fetch fresh key, retry once
- **Recommended TTL**: 24 hours (86400000 milliseconds)

## Language Preference Handling

**Selection Logic**:
1. Try each preference in order (e.g., `["en", "hi", "auto"]`)
2. Match `languageCode` field (case-insensitive)
3. Special case: `"auto"` matches tracks where `kind == "asr"`
4. Fallback: First available track if no preference matches

## Error Handling

### Common Issues

1. **API Key Extraction Fails**
   - YouTube page structure changed
   - Regex pattern needs updating
   - Missing browser User-Agent on page fetch

2. **InnerTube API Returns Error**
   - Invalid/expired API key - retry with fresh key
   - Outdated client version - auto-probed or manual override (see strategy above)
   - Invalid video ID
   - Video is private/restricted

3. **HTTP 400 from Player API**
   - Client version outdated - auto-probe upward or use manual override
   - Custom User-Agent sent with ANDROID client - use default User-Agent
   - Browser User-Agent sent - remove it

4. **No Captions Found**
   - Video has no subtitles
   - Captions are disabled by uploader
   - Age-restricted content

5. **Transcript Download Fails**
   - `baseUrl` expired (includes timestamp)
   - PO token required (using page HTML URLs instead of ANDROID API URLs)
   - Network timeout
   - Rate limiting

### Retry Logic

- **API key fetch failure**: Retry once with fresh key
- **Client version rejection (HTTP 400)**: Auto-probe upward by major version (up to +5), or manual override
- **InnerTube API failure**: Clear cache, fetch fresh key, retry once
- **Network errors**: Implement exponential backoff (caller responsibility)

## Rate Limiting

**Risks**:
- Too many requests may trigger IP blocking
- YouTube may throttle or ban aggressive clients

**Best Practices**:
- Implement rate limiting on client side
- Cache subtitles to avoid re-downloading
- Add delays between bulk requests
- Respect HTTP 429 (Too Many Requests) responses

## Troubleshooting

### Debugging Steps

1. **Enable verbose logging** to see all API calls
2. **Test with known working URL** (public video with subtitles)
3. **Clear cache and retry** with fresh API key and client version
4. **Check client version**: Outdated versions cause HTTP 400
5. **Inspect raw API responses** for structure changes
6. **Verify no custom User-Agent** is sent on InnerTube/timedtext calls

### Testing Endpoints Manually

**Extract API Key**:
```bash
curl "https://www.youtube.com/watch?v=VIDEO_ID" \
  -H "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" | \
  grep -o '"INNERTUBE_API_KEY":"[^"]*"'
```

**Call InnerTube Player API** (ANDROID client, no custom User-Agent):
```bash
curl -X POST "https://www.youtube.com/youtubei/v1/player?key=API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"context":{"client":{"clientName":"ANDROID","clientVersion":"20.10.38"}},"videoId":"VIDEO_ID"}'
```

## Reference

The `youtube-transcript-api` Python library (v1.x) uses this same approach: `ANDROID` client, version `20.10.38`, no custom User-Agent.

## Known Limitations

1. **Undocumented API** - Can break at any time; client version is auto-probed when rejected, with manual override fallback
2. **Rate Limiting** - Aggressive use may trigger blocks
3. **Terms of Service** - May violate YouTube ToS
4. **Client Version Decay** - Versions get outdated; handled by auto-probing with manual fallback
5. **Restricted Content** - Private/age-restricted videos don't work
6. **Availability** - Not all videos have subtitles

## Related Documentation

- [Architecture Guidelines](./architecture.md)
- [Coding Standards](./coding-standards.md)
- Extension-specific implementation: `extensions/youtubeSubtitleDownloader/CLAUDE.md`
