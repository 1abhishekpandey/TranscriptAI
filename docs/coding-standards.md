# Coding Standards

## Naming Conventions

- **UseCases**: `VerbNounUseCase` (e.g., `DownloadSubtitlesUseCase`)
- **ViewModels**: `FeatureViewModel` (e.g., `HomeViewModel`)
- **Repositories**: `EntityRepository` (e.g., `SubtitleRepository`)
- **Composables**: PascalCase (e.g., `SubtitleScreen`)

## State Management

- Use `StateFlow`/`State` for UI state in ViewModels
- Single source of truth principle
- Immutable data classes for UI state

## Error Handling

- Domain models should use sealed classes for Result types
- ViewModels handle errors and map to UI-friendly messages
- Show user-friendly error messages in UI

## Logging

- Use appropriate log levels (VERBOSE, DEBUG, INFO, WARN, ERROR)
- Include context in log messages
- Log exceptions with stack traces
- Use centralized logging utilities

## Edge Case Handling

Always handle:
- Empty/null values
- Malformed input
- Network timeouts
- Parsing errors
- API failures

## Visibility Modifiers

- Keep internal classes `internal` unless needed in public API
- Expose minimal surface area to consumers
- Mark all utility functions as `internal` or `private`

## Testing Strategy

### Unit Tests
- Test Use Cases with mock repositories
- Test ViewModels with fake use cases
- Test utility functions with various inputs

### UI Tests
- Composable tests with preview parameters
- User interaction flows

### Integration Tests
- Repository implementations with real/fake data sources
- End-to-end flows for critical features
