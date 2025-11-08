# Architecture Guidelines

## Clean Architecture Layers

TranscriptAI follows strict separation of concerns across three layers:

### 1. Presentation Layer (`ui/`)
- Views (Composables)
- ViewModels
- UI Models/State
- Navigation

### 2. Domain Layer (`domain/`)
- Use Cases (business logic)
- Domain Models (entities)
- Repository Interfaces
- **No Android dependencies** (pure Kotlin)

### 3. Data Layer (`data/`)
- Repository Implementations
- Data Sources (Remote/Local)
- DTOs (Data Transfer Objects)
- Mappers (DTO ↔ Domain Model)

## MVVM Implementation

- **View (Composable)**: Observes ViewModel state, handles user interactions
- **ViewModel**: Manages UI state, coordinates use cases, transforms domain data to UI models
- **Model**: Domain entities and business logic (in domain layer)

## Dependency Flow

```
Presentation → Domain ← Data
```

- Presentation depends on Domain
- Data depends on Domain
- Domain has no dependencies (pure Kotlin)

## Key Principles

1. **Preserve Clean Architecture**
   - Domain layer has no Android/framework dependencies
   - Data layer implements domain interfaces
   - Public APIs delegate to use cases

2. **Single Source of Truth**
   - Use `StateFlow`/`State` for UI state in ViewModels
   - Immutable data classes for UI state

3. **Separation of Concerns**
   - Each layer has clear responsibilities
   - No business logic in ViewModels or UI
   - Use cases encapsulate all business logic
