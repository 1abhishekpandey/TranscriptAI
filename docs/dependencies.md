# Dependencies Guide

This document lists the dependencies used in the TranscriptAI project and what they are for.

Exact versions live in `gradle/libs.versions.toml`. They are not repeated here, except the critical ones below.

## App Dependencies

### Core Android

- `androidx.core:core-ktx` - Kotlin extensions for Android SDK
- `androidx.lifecycle:lifecycle-runtime-ktx` - Lifecycle-aware components
- `androidx.lifecycle:lifecycle-viewmodel-ktx` - ViewModel with coroutines support
- `androidx.lifecycle:lifecycle-viewmodel-compose` - ViewModel integration for Compose

### Jetpack Compose

- `androidx.compose.bom` - Compose Bill of Materials
- `androidx.compose.ui` - Compose UI toolkit
- `androidx.compose.material3` - Material3 design components
- `androidx.compose.material:material-icons-extended` - Extended Material icons
- `androidx.activity:activity-compose` - Compose integration for Activities
- `androidx.navigation:navigation-compose` - Screen navigation

### Dependency Injection

- `com.google.dagger:hilt-android` - Hilt dependency injection
- `androidx.hilt:hilt-navigation-compose` - Hilt navigation for Compose
- `com.google.devtools.ksp` - Kotlin Symbol Processing (for Hilt and Room)

### Database

- `androidx.room:room-runtime`, `room-ktx`, `room-compiler` (KSP) - Stores AI prompts

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

- OkHttp (HTTP client)
- OkHttp Logging Interceptor (debug logging)
- Kotlinx Coroutines (async operations)
- AndroidX Core KTX (Android utilities)
- Android XML Pull Parser (built-in)

## Version Compatibility

### Critical Version Constraints

| Component | Notes |
|-----------|-------|
| Gradle 9.8 | Wrapper in `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin 9.4 | Built-in Kotlin. No `kotlin-android` plugin |
| Kotlin 2.4 | Compose via the `kotlin-compose` Gradle plugin, same version as Kotlin |
| KSP 2.3 | Must support the Kotlin version in use |
| Hilt 2.60 | Processed with KSP |
| Room 2.8 | Processed with KSP |
| Java 21 | `compileOptions` source and target in `app` and the extension |
| Compose BOM | Pinned in `libs.versions.toml` |

### Important Notes

1. **Built-in Kotlin**: AGP 9 compiles Kotlin itself. Do not apply `org.jetbrains.kotlin.android`. There is no `kotlinOptions` block.
2. **Compose compiler**: No `composeOptions`. The `kotlin-compose` plugin version follows Kotlin.
3. **Upgrade together**: Kotlin, KSP, Hilt, Room and AGP depend on each other. Upgrade them in one change and run a full build.
4. **JDK**: Builds run on current JDKs. Android Studio's bundled JBR works.

## Adding New Dependencies

### Process

1. **Check compatibility** with existing versions (especially Kotlin/Compose)
2. **Add to `libs.versions.toml`** for version management
3. **Add to module `build.gradle.kts`**
4. **Sync Gradle**
5. **Update this document**

### Example: Adding a Library

```toml
# In libs.versions.toml
[versions]
example = "1.0.0"

[libraries]
example-lib = { group = "com.example", name = "example-lib", version.ref = "example" }
```

```kotlin
// In app/build.gradle.kts
dependencies {
    implementation(libs.example.lib)
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
- **Solution**: Check Hilt version supports the Kotlin and AGP versions in use
- **Current Working**: Hilt 2.60 + Kotlin 2.4 + AGP 9.4 + KSP 2.3

**Problem**: Compose compiler errors
- **Solution**: Ensure the `kotlin-compose` plugin version equals the Kotlin version
- Do not use `composeOptions.kotlinCompilerExtensionVersion` with Kotlin 2.x

**Problem**: KSP errors
- **Solution**: Check the KSP version supports the Kotlin version in use

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

- [ ] DataStore - Preferences storage (settings use SharedPreferences today)
- [ ] WorkManager - Background tasks
- [ ] Coil - Image loading (if needed)

## Related Documentation

- [Build Commands](./build-commands.md)
- [Coding Standards](./coding-standards.md)
- [Architecture Guidelines](./architecture.md)
