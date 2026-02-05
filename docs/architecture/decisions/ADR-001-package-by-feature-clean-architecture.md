# ADR-001: Package-by-Feature + Clean Architecture

**Status:** Accepted  
**Date:** 2026-02-05  
**Context:** Refactoring MiEmpresa MVP from flat structure to scalable architecture

## Decision
Adopt Package-by-Feature with Clean Architecture (domain/data/ui layers within features).

## Rationale
- **Cohesion:** All code for a feature (products/) lives together
- **Scalability:** Adding features = duplicate structure
- **Clean Architecture:** Separation domain (interfaces) / data (impl) / ui (ViewModels)
- **Offline-first support:** Repository pattern enables Room write → WorkManager sync
- **Core transversal:** Shared code (APIs, auth, sync, database) in core/

## Consequences
- **Positive:** Clear structure, testable, modular
- **Negative:** Slight duplication of domain/data/ui folders per feature
- **Mitigated:** Template-able structure, auto code generation possible

## Alternatives Rejected
- Package-by-layer (Google official): Poor cohesion, merge conflicts
- Flat structure (current): No separation of concerns, hard to test

## References
- Now in Android (Google sample): github.com/android/nowinandroid
- Decisiones_Tecnicas_y_Alcance_MVP.md §10
