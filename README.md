# MiEmpresaApp - Versión Materia Desarrollo Mobile

**⚠️ SNAPSHOT HISTÓRICO - NO MODIFICAR**

## Contexto Académico

- **Materia:** Desarrollo Mobile
- **Universidad:** Universidad Austral - Ingeniería Informática
- **Período:** Agosto - Noviembre 2024
- **Tag:** `v1.0.0-materia-mobile`

## Funcionalidades Originales

### Implementadas en esta versión:

✅ **Autenticación y Usuarios**
- Autenticación con Google (OAuth 2.0)
- Firebase Auth para gestión de sesiones
- Perfil de usuario básico

✅ **Gestión de Empresas**
- Crear empresas con información básica
- Lista de empresas del usuario
- Selección de empresa activa
- Cambio entre empresas desde drawer

✅ **Gestión de Productos**
- CRUD completo de productos
- Categorías de productos
- Imágenes de productos (carga desde dispositivo)
- Vista de detalle de producto (fullscreen en lugar de dialog)
- Búsqueda y filtrado

✅ **Integración Google Drive**
- Creación de carpeta por empresa en Drive
- Google Sheets como base de datos remota
- Sincronización básica (no offline-first)
- Carpeta de imágenes en Drive

✅ **Persistencia Local**
- Room Database configurado
- DAO para Company
- Caché local de datos

✅ **UI/UX**
- Jetpack Compose
- Material Design 3
- Dark mode implementado
- Navigation Compose
- Animaciones básicas

## Stack Tecnológico

### Core
- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + Material3
- **Arquitectura:** MVVM básico
- **DI:** Hilt (Dagger)
- **SDK:** compileSdk 35, minSdk 24, targetSdk 35

### Persistencia
- **Local:** Room Database
- **Remoto:** Google Sheets API + Drive API
- **Auth:** Firebase Auth + Google Sign-In

### Dependencias Principales (Versiones Exactas)

**Build & Plugins:**
```gradle
// Desde gradle/libs.versions.toml
kotlin = "2.0.20"
agp = "8.13.2"
hilt = "2.52"
ksp = "2.0.20-1.0.25"
```

**Compose:**
```gradle
compose-bom = "2024.09.00"
androidx.compose.ui
androidx.compose.ui.graphics
androidx.compose.ui.tooling.preview
androidx.compose.material3
androidx.navigation.navigation-compose
androidx.hilt.hilt-navigation-compose
```

**Persistencia:**
```gradle
room = "2.6.1"
androidx.room:room-runtime
androidx.room:room-ktx
datastore-preferences = "1.1.1"
```

**Firebase & Google:**
```gradle
firebase-bom = "33.12.0"
com.google.firebase:firebase-auth-ktx
gms-play-services-auth = "21.3.0"
com.google.apis:google-api-services-drive:v3-rev20240903-2.0.0
com.google.apis:google-api-services-sheets:v4-rev20240730-2.0.0
```

**Otros:**
```gradle
androidx.core:core-ktx
androidx.lifecycle:lifecycle-runtime-ktx
androidx.activity:activity-compose
coil-compose (para carga de imágenes)
```

## Arquitectura Original

### Patrón MVVM Básico

```
┌─────────────────────────────────────────────┐
│              UI Layer (Compose)              │
│  ┌─────────────┐  ┌──────────────┐         │
│  │ SignInScreen│  │ ProductScreen│  ...    │
│  └──────┬──────┘  └──────┬───────┘         │
└─────────┼─────────────────┼─────────────────┘
          │                 │
          ▼                 ▼
┌─────────────────────────────────────────────┐
│           ViewModel Layer                    │
│  ┌──────────────┐  ┌───────────────┐       │
│  │SignInViewModel│  │ProductViewModel│ ...  │
│  └──────┬───────┘  └───────┬───────┘       │
└─────────┼──────────────────┼─────────────────┘
          │                  │
          ▼                  ▼
┌─────────────────────────────────────────────┐
│         Domain/Data Layer                    │
│  ┌─────────────┐  ┌────────┐  ┌──────────┐ │
│  │GoogleAuthAPI│  │ DriveApi│  │ Room DAO │ │
│  └─────────────┘  └────────┘  └──────────┘ │
└─────────────────────────────────────────────┘
```

### Flujo de Datos (Simplificado)
1. Usuario autentica con Google OAuth
2. App crea carpeta en Drive del usuario
3. Crea Google Sheet como "base de datos"
4. CRUD de productos se escribe en Sheet
5. Room Database cachea datos localmente
6. Sincronización manual (no automática en background)

## Limitaciones Conocidas

### No Implementado en esta versión:

🚫 **Multitenancy / Roles**
- No hay distinción entre administrador y cliente
- Solo el dueño puede acceder a sus empresas
- No hay vista pública de catálogo

🚫 **Sistema de Pedidos**
- No existe funcionalidad de pedidos
- No hay carrito de compras
- No hay gestión de órdenes

🚫 **Sincronización Offline-First**
- Sincronización básica, no robusta
- Sin WorkManager para sync en background
- Sin manejo de conflictos
- Sin cola de operaciones pendientes

🚫 **Compartir Catálogo**
- No se puede compartir catálogo con clientes
- No hay deeplinks
- No hay integración WhatsApp

🚫 **Testing Automatizado**
- Sin tests unitarios
- Sin tests de integración
- Sin tests de UI

🚫 **CI/CD**
- Sin pipeline de deployment
- Sin GitHub Actions configurado
- Sin distribución automática

🚫 **Privacidad "Zero Knowledge"**
- Permisos OAuth amplios (DRIVE completo)
- No hay énfasis en privacidad por diseño
- Arquitectura no está optimizada para zero-knowledge

## Estructura del Código Fuente

### Organización de Paquetes

```
app/src/main/java/com/brios/miempresa/
├── signin/                    ← Autenticación Google/Firebase
│   ├── SignInScreen.kt
│   ├── SignInViewModel.kt
│   └── AuthState.kt
├── navigation/                ← Navegación y routing
│   ├── NavHost.kt
│   ├── TopBar.kt
│   ├── Drawer.kt
│   └── MiEmpresaScreen.kt
├── data/                      ← Capa de datos
│   ├── Entities.kt           (Company entity)
│   ├── Daos.kt               (CompanyDao)
│   ├── RemoteDataStorage.kt  (Room Database config)
│   └── LocalDataStorage.kt   (DataStore preferences)
├── domain/                    ← Lógica de negocio e integraciones
│   ├── GoogleAuthClient.kt   (OAuth + servicios Google)
│   ├── DriveApi.kt           (Operaciones Drive)
│   ├── SpreadsheetsApi.kt    (Operaciones Sheets)
│   ├── BiometricAuthManager.kt
│   └── SignInResult.kt
├── product/                   ← Gestión de productos
│   ├── ProductsComposable.kt
│   ├── ProductsViewModel.kt
│   ├── ProductViewModel.kt
│   └── Product.kt            (DTO/modelo UI)
├── categories/                ← Gestión de categorías
│   ├── CategoriesComposable.kt
│   └── CategoriesViewModel.kt
├── initializer/              ← Onboarding/configuración inicial
│   ├── InitializerScreen.kt
│   ├── InitializerViewModel.kt
│   ├── WelcomeView.kt
│   └── CompanyListView.kt
├── components/                ← Componentes UI reutilizables
├── ui/                        ← Temas y diseño
├── MainActivity.kt            ← Punto de entrada
└── MiEmpresa.kt              ← Application class con Hilt
```

### Modelos de Datos

**Company** (Entity Room):
```kotlin
@Entity(tableName = "companies")
data class Company(
    @PrimaryKey val id: String,
    val name: String,
    val selected: Boolean
)
```

**Product** (DTO/Modelo UI):
```kotlin
data class Product(
    val rowIndex: Int,
    val name: String,
    val description: String,
    val price: String,
    val categories: List<String>,
    val imageUrl: String
)
```
*Nota: Product no persiste en Room en esta versión, solo Company.*

## Evolución a Trabajo de Tesis

### Principales Cambios Arquitectónicos Planificados

1. **Zero Knowledge Architecture**
   - Scope OAuth reducido a `DRIVE_FILE`
   - Sin acceso del desarrollador a datos del usuario
   - Toda la data en Google Drive del usuario

2. **Offline-First Sync**
   - WorkManager para sincronización en background
   - Cola de operaciones pendientes
   - Manejo de conflictos
   - Room como source of truth

3. **Multitenancy**
   - Roles: Administrador vs Cliente
   - Vista pública de catálogo
   - Compartir catálogo vía deeplink
   - Gestión de múltiples tiendas

4. **Sistema de Pedidos**
   - Creación de pedidos (manual + desde catálogo)
   - Carrito de compras
   - Integración WhatsApp para envío
   - Estados de pedido

5. **Mejoras UX**
   - Onboarding completo
   - Diseño refinado (Stitch)
   - Animaciones mejoradas
   - Componentes reutilizables

---

> 💡 **Para revisores externos:** Esta branch documenta la versión original entregada en la materia Desarrollo Mobile (Nov 2024). El proyecto continúa evolucionando en el trabajo de tesis con arquitectura Zero-Knowledge y funcionalidades extendidas. Ver branch `mvp-febrero` o `main` para desarrollo actual.
