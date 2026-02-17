# ADR-002: Arquitectura de Navegación para MVP

**Status:** Accepted  
**Date:** 2026-02-11  
**Deciders:** Brian O.S. (Lead Developer)

## Context
MiEmpresa requiere soportar flujo Admin (tabs + drawer) y flujo Cliente (lineal por deeplink) en un MVP con alcance y tiempo acotados.  
La implementación actual usa `MiEmpresaScreen` (enum + routes string), `NavHost` plano y navegación manual por tabs, lo cual es funcional pero con deuda técnica conocida (sin type-safe args, sin nested graphs, sin adaptive navigation).

## Decision
Mantener la arquitectura de navegación actual para el MVP y **diferir la migración estructural** (type-safe routes, nested graphs y `NavigationSuiteScaffold`) a una fase post-MVP.

Para completar el MVP:
- Extender el patrón actual agregando rutas nuevas en `MiEmpresaScreen` y `NavHost`.
- Mantener navegación full-screen push para formularios/detalles.
- Resolver flujo cliente (deeplink -> catálogo -> carrito -> WhatsApp) sin refactor global de navegación.

## Rationale
- La navegación actual ya es funcional para alcance phone-only del MVP.
- El refactor completo de navegación compite con features críticas pendientes.
- Diferir reduce riesgo de regresiones en una etapa de cierre y validación de usuario.

## Consequences
**Positivas**
- Menor riesgo técnico en cierre de MVP.
- Más foco en features funcionales y pruebas de usuario.
- Camino de evolución post-MVP ya definido.

**Negativas**
- Se mantiene deuda técnica en rutas string/manuales.
- Sin adaptive layout ni separación fuerte por graphs en esta etapa.

## Mejoras Post-MVP (Roadmap)
1. Migrar a type-safe routes (Kotlin Serialization).
2. Separar `auth/admin/client` con nested NavGraphs.
3. Evaluar `NavigationSuiteScaffold` para layouts adaptativos.
4. Hacer TopBar route-aware para desacoplarla del índice de tab.

## References
- `docs/context/history/ADR-002-navigation-architecture.md` (análisis extendido/histórico).
- `docs/context/ArquitecturaInformacion_MVP.md`.
- `docs/context/Decisiones_Tecnicas_y_Alcance_MVP.md`.
