# Dependencies Guide

This document lists all dependencies used in the TranscriptAI project.

## App Dependencies

### Core Android

- `androidx.core:core-ktx` - Kotlin extensions for Android SDK
- `androidx.lifecycle:lifecycle-runtime-ktx` - Lifecycle-aware components
- `androidx.lifecycle:lifecycle-viewmodel-ktx` - ViewModel with coroutines support
- `androidx.lifecycle:lifecycle-viewmodel-compose` - ViewModel integration for Compose

### Jetpack Compose

- `androidx.compose.bom` - Compose Bill of Materials (version 2024.09.00)
- `androidx.compose.ui` - Compose UI toolkit
- `androidx.compose.material3` - Material3 design components
- `androidx.activity:activity-compose` - Compose integration for Activities

### Dependency Injection

- `com.google.dagger:hilt-android` (2.52) - Hilt dependency injection
- `androidx.hilt:hilt-navigation-compose` - Hilt navigation for Compose
- `com.google.devtools.ksp` - Kotlin Symbol Processing (for Hilt)

### Networking

- `com.squareup.retrofit2:retrofit` - Type-safe HTTP client
- `com.squareup.retrofit2:converter-gson` - Gson converter for Retrofit
- `com.squareup.okhttp3:okhttp` - HTTP & HTTP/2 client
- `com.squareup.okhttp3:logging-interceptor` - HTTP request/response logging

### Coroutines

- `org.jetbrains.kotlinx:kotlinx-coroutines-core` - Coroutines core library
- `org.jetbrains.kotlinx:kotlinx-coroutines-android` - Android-specific coroutines

### Logging

- `com.jakewharton.timber:timber` - Logging library with tree-based approach

### Other

- `com.google.code.gson:gson` - JSON parsing library
- `com.squareup:javapoet` - Java code generation (Hilt dependency)

## Extension Module Dependencies

### YouTube Subtitle Downloader Extension

**Self-contained** - bundles its own dependencies:

- OkHttp 4.12.0 (HTTP client)
- OkHttp Logging Interceptor (debug logging)
- Kotlinx Coroutines 1.7.3 (async operations)
- AndroidX Core KTX (Android utilities)
- Android XML Pull Parser (built-in)

## Version Compatibility

### Critical Version Constraints

**Android Gradle Plugin (AGP)**: 8.5.2
**Kotlin**: 1.9.24
**KSP**: 1.9.24-1.0.20
**Hilt**: 2.52
**Compose BOM**: 2024.09.00
**Compose Compiler**: 1.5.14

### Important Notes

1. **Kotlin 2.x**: Do not upgrade to Kotlin 2.x without updating Compose plugin configuration
2. **KSP Version**: Must match Kotlin version exactly (format: `<kotlin-version>-<ksp-version>`)
3. **Compose Compiler**: Must be compatible with Kotlin version
   - Kotlin 1.9.24 → Compose Compiler 1.5.14
4. **Hilt**: Should be compatible with Kotlin and AGP versions

## Version Compatibility Matrix

| Component | Version | Compatible With |
|-----------|---------|-----------------|
| AGP | 8.5.2 | Kotlin 1.9.24 |
| Kotlin | 1.9.24 | AGP 8.5.2, Compose 1.5.14 |
| KSP | 1.9.24-1.0.20 | Kotlin 1.9.24 |
| Hilt | 2.52 | Kotlin 1.9.24, AGP 8.5.2 |
| Compose BOM | 2024.09.00 | Compose Compiler 1.5.14 |
| Compose Compiler | 1.5.14 | Kotlin 1.9.24 |

## Adding New Dependencies

### Process

1. **Check compatibility** with existing versions (especially Kotlin/Compose)
2. **Add to `libs.versions.toml`** for version management
3. **Add to module `build.gradle.kts`**
4. **Sync Gradle**
5. **Update this document**

### Example: Adding Room Database

```toml
# In libs.versions.toml
[versions]
room = "2.6.0"

[libraries]
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
```

```kotlin
// In app/build.gradle.kts
dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
```

## Dependency Management Best Practices

### Use Version Catalogs (`libs.versions.toml`)

- Centralized version management
- Type-safe accessors in Gradle files
- Easy to update versions across modules

### Minimize Transitive Dependencies

- Use `implementation` instead of `api` where possible
- Keep dependency tree clean
- Avoid version conflicts

### Regular Updates

- Update dependencies quarterly
- Check for security vulnerabilities
- Test thoroughly after updates

### Extension Module Philosophy

- Extensions should be self-contained
- Bundle all dependencies within the module
- No version conflicts with app dependencies

## Troubleshooting

### Common Dependency Issues

**Problem**: Hilt compilation errors
- **Solution**: Check Hilt version matches Kotlin/AGP versions
- **Current Working**: Hilt 2.52 + Kotlin 1.9.24 + AGP 8.5.2

**Problem**: Compose compiler errors
- **Solution**: Ensure `composeOptions.kotlinCompilerExtensionVersion` matches Kotlin version
- **Current**: 1.5.14 for Kotlin 1.9.24

**Problem**: KSP errors
- **Solution**: KSP version must match Kotlin version exactly
- **Current**: 1.9.24-1.0.20 for Kotlin 1.9.24

**Problem**: Version conflicts
- **Solution**: Use `./gradlew app:dependencies` to inspect dependency tree
- **Resolution**: Force specific version or exclude transitive dependency

### Checking Dependencies

```bash
# View all dependencies
./gradlew app:dependencies

# View dependency tree for specific configuration
./gradlew app:dependencies --configuration debugCompileClasspath

# Check for dependency updates
./gradlew dependencyUpdates
```

## Future Dependencies (Planned)

- [ ] Room Database - Local caching
- [ ] DataStore - Preferences storage
- [ ] WorkManager - Background tasks
- [ ] Coil - Image loading (if needed)
- [ ] Navigation Compose - Multi-screen navigation

## Related Documentation

- [Build Commands](./build-commands.md)
- [Coding Standards](./coding-standards.md)
- [Architecture Guidelines](./architecture.md)
