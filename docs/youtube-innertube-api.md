# YouTube InnerTube API Guide

## Overview

The InnerTube API is YouTube's undocumented internal API used for fetching video metadata, including subtitle/caption tracks.

**⚠️ Important Notes:**
- This API is not officially documented
- YouTube can change it without notice
- May violate YouTube Terms of Service
- Use responsibly and ethically

## The 3-Step Flow

### Step 1: Extract INNERTUBE_API_KEY

**Purpose**: Get the API key needed to call InnerTube endpoints

**Process**:
1. Fetch YouTube video page HTML
2. Use regex to extract `"INNERTUBE_API_KEY":"[key]"` from embedded JavaScript
3. Cache key with timestamp (recommended: 24h TTL)

**Example Regex**:
```kotlin
"INNERTUBE_API_KEY"\\s*:\\s*"([^"]+)"
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
      "clientName": "WEB",
      "clientVersion": "2.0"
    }
  },
  "videoId": "VIDEO_ID"
}
```

**Response Path**:
```
captions.playerCaptionsTracklistRenderer.captionTracks[]
```

**Track Object**:
```json
{
  "baseUrl": "https://www.youtube.com/api/timedtext?v=...",
  "languageCode": "en",
  "kind": "asr",  // "asr" = auto-generated
  "name": { "simpleText": "English" }
}
```

### Step 3: Fetch Transcript XML

**Purpose**: Download actual subtitle content

**Endpoint**: Use `baseUrl` from caption track

**Response Format**:
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

## API Key Caching Strategy

**Why Cache**: Fetching the key on every request is slow and unnecessary

**Implementation**:
- **First request**: Fetch key from YouTube page → Cache with timestamp
- **Subsequent requests**: Use cached key if age < TTL
- **On API failure**: Clear cache → Fetch fresh key → Retry once
- **Recommended TTL**: 24 hours (86400000 milliseconds)

**Cache Keys** (SharedPreferences):
- `innertube_api_key`: The API key string
- `api_key_timestamp`: Long (milliseconds since epoch)

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
   - Network issue fetching page

2. **InnerTube API Returns Error**
   - Invalid/expired API key → Retry with fresh key
   - Invalid video ID
   - Video is private/restricted

3. **No Captions Found**
   - Video has no subtitles
   - Captions are disabled by uploader
   - Age-restricted content

4. **Transcript Download Fails**
   - `baseUrl` expired (includes timestamp)
   - Network timeout
   - Rate limiting

### Retry Logic

- **API key fetch failure**: Retry once with fresh key
- **InnerTube API failure**: Clear cache → Fetch fresh key → Retry once
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
3. **Clear cache and retry** with fresh API key
4. **Check API key validity**: Manually test with curl
5. **Inspect raw API responses** for structure changes

### Testing Endpoints Manually

**Extract API Key**:
```bash
curl "https://www.youtube.com/watch?v=VIDEO_ID" | grep -o '"INNERTUBE_API_KEY":"[^"]*"'
```

**Call InnerTube API**:
```bash
curl -X POST "https://www.youtube.com/youtubei/v1/player?key=YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"context":{"client":{"clientName":"WEB"}},"videoId":"VIDEO_ID"}'
```

## Known Limitations

1. **Undocumented API** - Can break at any time
2. **Rate Limiting** - Aggressive use may trigger blocks
3. **Terms of Service** - May violate YouTube ToS
4. **API Key Rotation** - Keys can change unpredictably
5. **Restricted Content** - Private/age-restricted videos don't work
6. **Availability** - Not all videos have subtitles

## Related Documentation

- [Architecture Guidelines](./architecture.md)
- [Coding Standards](./coding-standards.md)
- Extension-specific implementation: `extensions/youtubeSubtitleDownloader/CLAUDE.md`
