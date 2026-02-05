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

## See Also

- ADR-001: Package-by-Feature decision
- Decisiones_Tecnicas_y_Alcance_MVP.md §10: Comparativa arquitecturas
