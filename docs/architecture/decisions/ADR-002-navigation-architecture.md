# ADR-002: Arquitectura de Navegación

**Status:** Accepted (Reopened 2026-02-18 for phased refactor)  
**Date:** 2026-02-11  
**Updated:** 2026-02-18

## Document Scope
- Este archivo es la **fuente canónica** de ADR-002 para el repositorio público.
- Cualquier investigación extensa/histórica debe considerarse anexo interno, no la decisión oficial.

## Context
La arquitectura legacy (rutas string dispersas + `NavHost` plano + tabs manuales) permitió cerrar el MVP, pero en pruebas reales aparecieron problemas de interacción durante transiciones push/pop (ghost taps).

Los hotfix de lifecycle resolvieron el riesgo inmediato, pero no eliminan la deuda estructural base:
- rutas string/manuales (sin type safety),
- ausencia de separación fuerte por grafos (auth/admin/client),
- acoplamiento de UI de navegación a índices/listas manuales.

## Decision Drivers
- Reducir riesgo de bugs de navegación en evolución de features.
- Mantener continuidad de entrega (sin big-bang refactor).
- Alinear con prácticas oficiales de Navigation Compose.
- Evitar regresiones UX durante transición.

## Considered Options
### Opción A — Solo hotfixes incrementales
- **Pros:** bajo riesgo inmediato.
- **Contras:** deuda técnica se acumula; cada nueva pantalla requiere hardening manual.

### Opción B — Refactor total inmediato (type-safe + nested + adaptive)
- **Pros:** arquitectura objetivo de una sola vez.
- **Contras:** alto riesgo operativo y de regresión en una etapa de estabilización.

### Opción C — Refactor por fases (seleccionada)
- **Pros:** reduce deuda estructural sin congelar producto.
- **Contras:** convivencia temporal de patrones durante la migración.

## Decision
Adoptar **Opción C (phased refactor)**:

1. **Fase 0 (completada):** hardening anti ghost-tap por lifecycle/transición.
2. **Fase 1 (siguiente):**
   - definir contrato de rutas centralizado (patterns/builders/args),
   - centralizar navegación en helpers/navigator de dominio UI,
   - volver TopBar/acciones route-aware (sin listas manuales de excepciones).
3. **Fase 2 (siguiente):**
   - separar `authGraph`, `adminGraph`, `clientGraph` con nested graphs,
   - limpiar dependencias cruzadas de backstack.
4. **Fase 3 (post-MVP / expansión):**
   - evaluar `NavigationSuiteScaffold` y layouts adaptativos (tablet/foldable).

## Implementation Progress (2026-02-18)
- **Fase 0:** completada.
- **Fase 1:** parcialmente implementada.
  - ✅ Contrato central de rutas (`MiEmpresaRoutes`).
  - ✅ Helper central de clear-backstack (`navigateClearingBackStack`).
  - ✅ Migración de `NavHost` y `Drawer` al contrato central.
  - ✅ Tabs admin migradas de índices manuales a `AdminTopLevelTab`.
  - ✅ Eliminado `MiEmpresaScreen` para dejar una sola fuente de rutas.
  - 🔄 Pendiente: separación por nested graphs (Fase 2) y evaluación de type-safe routes con Kotlin Serialization.

## Consequences
### Positivas
- Camino explícito y escalable para abandonar rutas string manuales.
- Menor probabilidad de regresiones por eventos duplicados/transiciones.
- Base más testeable para flujos admin/client híbridos.

### Negativas
- Habrá una etapa temporal de migración con complejidad mixta.
- Requiere disciplina para que nuevas pantallas entren al patrón nuevo.

## Evidence & References
- AndroidX Navigation release notes (`NavBackStackEntry`/`RESUMED` timing fixes y regresiones relacionadas):  
  https://developer.android.com/jetpack/androidx/releases/navigation
- Navigation Compose official docs (nested graphs, testing, event callbacks):  
  https://developer.android.com/develop/ui/compose/navigation
- Jetsnack sample (`lifecycleIsResumed()` para deduplicar navegación):  
  https://github.com/android/compose-samples/blob/main/Jetsnack/app/src/main/java/com/example/jetsnack/ui/navigation/JetsnackNavController.kt
- Now in Android (estado de navegación centralizado `NavigationState` + `Navigator` + top-level stack):  
  https://github.com/android/nowinandroid/blob/main/core/navigation/src/main/kotlin/com/google/samples/apps/nowinandroid/core/navigation/NavigationState.kt  
  https://github.com/android/nowinandroid/blob/main/core/navigation/src/main/kotlin/com/google/samples/apps/nowinandroid/core/navigation/Navigator.kt
