# MiEmpresa Architecture

Package-by-Feature + Clean Architecture for offline-first Android MVP

## Structure

```
com.brios.miempresa/
├── core/              # Transversal code
│   ├── api/           # DriveApi, SpreadsheetsApi
│   ├── auth/          # GoogleAuthClient
│   ├── sync/          # WorkManager, SyncManager
│   ├── data/          # Room DB, DataStore
│   ├── ui/            # Components, Theme
│   ├── util/          # QR, extensions
│   └── di/            # Hilt modules
├── auth/              # Feature: Authentication
│   ├── domain/        # AuthRepository interface
│   ├── data/          # AuthRepositoryImpl
│   └── ui/            # SignInScreen, SignInViewModel
├── cart/              # Feature: Shopping cart
├── onboarding/        # Feature: Workspace setup (pending US-001)
├── products/          # Feature: CRUD productos (pending US-003)
├── categories/        # Feature: CRUD categorías
├── pedidos/           # Feature: Orders
└── config/            # Feature: Company settings
```

## Architecture Principles

1. **Package-by-Feature:** Each feature = 1 folder
2. **Clean Architecture:** domain/ (interfaces) → data/ (impl) → ui/ (ViewModels)
3. **Offline-first:** Write Room (dirty=true) → WorkManager sync
4. **Repository pattern:** ViewModels inject Repository interfaces (never DAOs/APIs directly)
5. **Multitenancy:** All Room queries filter by companyId

## Dependency Rules

✅ Features can depend on core/  
✅ core/sync can depend on feature/domain (Repository interfaces)  
❌ Features CANNOT depend on other features  
❌ core/ CANNOT depend on feature/data or feature/ui

## C4 Architecture Documentation

Full C4 model documentation (4 levels, bottom-up):

| Level | Document | Description |
|-------|----------|-------------|
| **Context** | [c4/c4-context.md](c4/c4-context.md) | System context: personas, external systems, user journeys |
| **Container** | [c4/c4-container.md](c4/c4-container.md) | Deployment containers: Android App, Room DB, Google APIs, Firebase, WorkManager |
| **Component** | [c4/c4-component.md](c4/c4-component.md) | 11 logical components across 3 architectural layers |
| **Code** | [c4/c4-code-core.md](c4/c4-code-core.md) | Core module: API, auth, sync, data, DI, UI, util |
| **Code** | [c4/c4-code-cart.md](c4/c4-code-cart.md) | Cart feature: domain use cases, price validation, WhatsApp checkout |
| **Code** | [c4/c4-code-features.md](c4/c4-code-features.md) | All other features: auth, onboarding, products, categories, catalog, orders, config, navigation |

## See Also

- ADR-001: Package-by-Feature decision
- ADR-002: Navigation architecture
- Decisiones_Tecnicas_y_Alcance_MVP.md §10: Comparativa arquitecturas
