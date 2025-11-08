# Troubleshooting Guide

This document provides solutions to common issues encountered when developing SummaryAI.

## Build Issues

### Hilt Compilation Errors

**Problem**: `@HiltAndroidApp` or `@AndroidEntryPoint` not recognized

**Symptoms**:
- `Unresolved reference: HiltAndroidApp`
- Build fails with Hilt annotation errors
- Dagger components not generated

**Solution**:
1. Check Hilt version matches Kotlin/AGP versions
2. Current working combination:
   - Hilt 2.52
   - Kotlin 1.9.24
   - AGP 8.5.2
3. Ensure KSP is configured correctly
4. Clean and rebuild:
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

### Compose Compiler Errors

**Problem**: Compose compiler version mismatch

**Symptoms**:
- `Compose compiler version is incompatible`
- Compilation fails with Compose-related errors
- Runtime crashes with Compose

**Solution**:
1. Check `composeOptions.kotlinCompilerExtensionVersion` in `app/build.gradle.kts`
2. Current working: 1.5.14 for Kotlin 1.9.24
3. Use [Compose-Kotlin compatibility matrix](https://developer.android.com/jetpack/androidx/releases/compose-kotlin)
4. Update both versions together:
   ```kotlin
   composeOptions {
       kotlinCompilerExtensionVersion = "1.5.14"
   }
   ```

### KSP Errors

**Problem**: KSP version mismatch

**Symptoms**:
- `KSP version does not match Kotlin version`
- Annotation processing fails
- Hilt/Room code not generated

**Solution**:
1. KSP version must match Kotlin version exactly
2. Format: `<kotlin-version>-<ksp-version>`
3. Current working: `1.9.24-1.0.20` for Kotlin 1.9.24
4. Update in `libs.versions.toml`:
   ```toml
   [versions]
   kotlin = "1.9.24"
   ksp = "1.9.24-1.0.20"
   ```

### Gradle Sync Fails

**Problem**: Gradle sync fails with version catalog errors

**Symptoms**:
- `Could not resolve all dependencies`
- `Version catalog not found`
- Dependencies not resolving

**Solution**:
1. Check `libs.versions.toml` syntax
2. Ensure all referenced versions exist
3. Invalidate caches and restart:
   - Android Studio → File → Invalidate Caches / Restart
4. Sync Gradle again:
   ```bash
   ./gradlew --refresh-dependencies
   ```

### Module Not Found

**Problem**: Extension module not found

**Symptoms**:
- `Project ':extensions:youtubeSubtitleDownloader' not found`
- Import errors for extension classes

**Solution**:
1. Check `settings.gradle.kts` includes module:
   ```kotlin
   include(":extensions:youtubeSubtitleDownloader")
   ```
2. Verify module path exists
3. Sync Gradle
4. Check module dependency in `app/build.gradle.kts`:
   ```kotlin
   implementation(project(":extensions:youtubeSubtitleDownloader"))
   ```

## Runtime Issues

### Logger Not Working

**Problem**: No logs appearing in Logcat

**Symptoms**:
- `Logger.logI()` calls produce no output
- Logcat is empty
- App seems to run but no logs

**Solution**:
1. Ensure `Logger.init()` is called in `Application.onCreate()`:
   ```kotlin
   class SummaryAiApplication : Application() {
       override fun onCreate() {
           super.onCreate()
           Logger.init()  // Must be called!
       }
   }
   ```
2. Check Logcat filter is set to `SummaryAI`
3. Verify log level:
   ```bash
   adb shell setprop log.tag.SummaryAI VERBOSE
   ```
4. Check device logs are enabled

### Hilt Injection Fails

**Problem**: Hilt dependency injection fails at runtime

**Symptoms**:
- `java.lang.RuntimeException: Cannot create an instance of class`
- `Dagger component not found`
- NullPointerException on injected fields

**Solution**:
1. Verify annotations are correct:
   - `@HiltAndroidApp` on Application class
   - `@AndroidEntryPoint` on Activities/Fragments
   - `@HiltViewModel` on ViewModels
   - `@Inject` on constructor
2. Check Application class is registered in `AndroidManifest.xml`:
   ```xml
   <application
       android:name=".SummaryAiApplication"
       ...
   ```
3. Ensure all required modules are provided in `AppModule.kt`
4. Clean and rebuild project

### UI Not Updating

**Problem**: UI doesn't reflect ViewModel state changes

**Symptoms**:
- StateFlow changes but UI remains static
- Button clicks don't trigger updates
- Loading state never shows

**Solution**:
1. Verify StateFlow is being collected correctly:
   ```kotlin
   val uiState by viewModel.uiState.collectAsState()
   ```
2. Check StateFlow is being updated:
   ```kotlin
   _uiState.value = HomeUiState.Loading("...")
   ```
3. Ensure ViewModel is properly scoped:
   ```kotlin
   val viewModel: HomeViewModel = hiltViewModel()
   ```
4. Check for coroutine scope issues:
   ```kotlin
   viewModelScope.launch {
       // Update state here
   }
   ```

### Extension API Fails

**Problem**: YouTube subtitle downloader returns errors

**Symptoms**:
- `ErrorType.API_KEY_EXTRACTION_FAILED`
- `ErrorType.NO_SUBTITLES_AVAILABLE`
- Network errors

**Solution**:
1. Check internet permission in `AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   ```
2. Enable verbose logging:
   ```kotlin
   YouTubeSubtitleDownloader.setLogLevel(LogLevel.VERBOSE)
   ```
3. Check logcat for extension logs:
   ```bash
   adb logcat -s summaryaiYouTubeSubtitleDownloader
   ```
4. Test with known working YouTube URL (public video with subtitles)
5. Clear extension cache:
   ```kotlin
   downloader.clearCache()
   ```
6. See [YouTube InnerTube API Guide](./youtube-innertube-api.md) for API-specific troubleshooting

### App Crashes on Launch

**Problem**: App crashes immediately after launch

**Symptoms**:
- `ClassNotFoundException`
- `NoSuchMethodError`
- `AbstractMethodError`

**Solution**:
1. Check ProGuard rules (if using release build)
2. Verify all dependencies are compatible
3. Check for MultiDex issues (if using many dependencies)
4. View crash logs:
   ```bash
   adb logcat -s AndroidRuntime
   ```
5. Clean build directory:
   ```bash
   ./gradlew clean
   rm -rf .gradle app/build
   ./gradlew build
   ```

## Development Environment Issues

### Android Studio Issues

**Problem**: Android Studio performance issues or errors

**Solution**:
1. Increase memory allocation:
   - Android Studio → Preferences → Appearance & Behavior → System Settings → Memory Settings
   - Increase Heap Size to 4096 MB or higher
2. Invalidate caches:
   - File → Invalidate Caches / Restart
3. Update Android Studio to latest stable version
4. Check for plugin conflicts

### Emulator Issues

**Problem**: Emulator won't start or is very slow

**Solution**:
1. Enable hardware acceleration (HAXM/Intel VT-x or AMD-V)
2. Allocate more RAM to emulator (2048 MB minimum)
3. Use Google Play system image for better performance
4. Consider using physical device instead

### ADB Issues

**Problem**: ADB device not found

**Symptoms**:
- `adb devices` shows no devices
- Unable to install app on device
- Logcat shows no output

**Solution**:
1. Restart ADB server:
   ```bash
   adb kill-server
   adb start-server
   ```
2. Check USB debugging is enabled on device
3. Verify USB connection mode is not "Charge only"
4. Try different USB cable or port
5. Reinstall USB drivers (Windows)

## Testing Issues

### Tests Not Running

**Problem**: Unit tests don't execute

**Solution**:
1. Check test dependencies are added
2. Verify test classes are in correct source set (`src/test/java`)
3. Run with Gradle command:
   ```bash
   ./gradlew test --stacktrace
   ```
4. Check for JUnit version conflicts

### Instrumented Tests Fail

**Problem**: Android instrumented tests fail

**Solution**:
1. Ensure test device/emulator is running
2. Check test dependencies include `androidTestImplementation`
3. Verify test classes are in `src/androidTest/java`
4. Run with:
   ```bash
   ./gradlew connectedAndroidTest --stacktrace
   ```

## Debugging Tips

### Enable Verbose Logging

For app:
```bash
adb shell setprop log.tag.SummaryAI VERBOSE
adb logcat -s SummaryAI:V
```

For extension:
```kotlin
YouTubeSubtitleDownloader.setLogLevel(LogLevel.VERBOSE)
```
```bash
adb logcat -s summaryaiYouTubeSubtitleDownloader:V
```

### View All Dependencies

Check for version conflicts:
```bash
./gradlew app:dependencies
./gradlew app:dependencies --configuration debugCompileClasspath
```

### Clear App Data

Reset app state completely:
```bash
adb shell pm clear com.abhishek.summaryai
```

### View Build Configuration

See effective build configuration:
```bash
./gradlew app:dependencies --configuration debugRuntimeClasspath
```

### Debug ProGuard Issues

View ProGuard mappings:
```bash
./gradlew app:assembleRelease
# Mappings at: app/build/outputs/mapping/release/mapping.txt
```

## Getting Help

### Before Asking for Help

1. Check this troubleshooting guide
2. Search project documentation:
   - [Architecture Guidelines](./architecture.md)
   - [Coding Standards](./coding-standards.md)
   - [Build Commands](./build-commands.md)
   - [Dependencies Guide](./dependencies.md)
   - [Logging Guide](./logging.md)
3. Check logs with verbose level enabled
4. Try clean build
5. Search GitHub issues (if project is on GitHub)

### Information to Provide

When reporting issues, include:
- Exact error message and stack trace
- Steps to reproduce
- Environment details (OS, Android Studio version, Kotlin version)
- Relevant code snippets
- Logs (with verbose level enabled)
- What you've already tried

## Common Error Messages

### "Cannot inline bytecode built with JVM target 1.8"

**Solution**: Ensure consistent JVM target across all modules
```kotlin
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlinOptions {
    jvmTarget = "11"
}
```

### "Duplicate class found in modules"

**Solution**: Exclude transitive dependency
```kotlin
implementation("com.example:library:1.0") {
    exclude(group = "com.duplicate", module = "module-name")
}
```

### "Manifest merger failed"

**Solution**: Check for conflicts in AndroidManifest.xml files
- View merged manifest in Android Studio: Build → Analyze → Manifest Merger
- Add `tools:replace` or `tools:merge` attributes to resolve conflicts

## Related Documentation

- [Build Commands](./build-commands.md)
- [Dependencies Guide](./dependencies.md)
- [Logging Guide](./logging.md)
- [YouTube InnerTube API Guide](./youtube-innertube-api.md)
