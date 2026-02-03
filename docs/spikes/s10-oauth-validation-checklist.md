# Spike S10: OAuth Scope Validation Checklist

**Fecha:** 03/02/2026  
**Spike:** S10 - OAuth scope reducido test  
**Objetivo:** Validar `DriveScopes.DRIVE_FILE` mantiene funcionalidad

---

## Pre-requisitos

- [ ] Revocar permisos OAuth existentes:
  - Android Settings → Google → Manage apps → MiEmpresa → Remove Access
  - O usar: https://myaccount.google.com/permissions
- [ ] App instalada en device/emulator
- [ ] Internet connection activa

---

## 1. Autenticación Básica

### 1.1 Sign In Flow

- [ ] Abrir app MiEmpresa
- [ ] Tap "Continuar con Google"
- [ ] **VERIFICAR:** Consent screen muestra permisos específicos (no "See, edit, create, and delete all your Google Drive files")
- [ ] **ESPERADO:** Mensaje más específico: "View and manage Google Drive files and folders created with this app"
- [ ] Aceptar permisos
- [ ] **RESULTADO:** Sign in exitoso (ver pantalla principal)

**Timestamp inicio:** _________  
**Timestamp fin:** _________  
**Status:** ⬜ PASS / ⬜ FAIL  
**Notas:**

---

## 2. Operaciones Google Drive

### 2.1 Crear carpeta empresa

- [ ] Navegar a "Onboarding" o flujo crear workspace
- [ ] Ingresar nombre empresa (ej: "TestSpike10")
- [ ] Ejecutar creación workspace
- [ ] **VERIFICAR:** Carpeta creada en Google Drive
- [ ] **MÉTODO:** Abrir drive.google.com → buscar carpeta "TestSpike10"

**Timestamp:** _________  
**Status:** ⬜ PASS / ⬜ FAIL  
**Drive Folder ID:** _________  
**Notas:**

### 2.2 Crear 2 Google Sheets

- [ ] Continuar flujo onboarding
- [ ] **VERIFICAR:** Sheet "TestSpike10 - Privado" creado
- [ ] **VERIFICAR:** Sheet "TestSpike10 - Publico" creado
- [ ] **MÉTODO:** Abrir sheets en drive.google.com

**Timestamp:** _________  
**Status:** ⬜ PASS / ⬜ FAIL  
**Sheet Private ID:** _________  
**Sheet Public ID:** _________  
**Notas:**

### 2.3 Crear subcarpeta Imagenes

- [ ] **VERIFICAR:** Subcarpeta "Imagenes" creada dentro de carpeta empresa
- [ ] **MÉTODO:** Navegar en Drive a carpeta empresa → ver subcarpeta

**Timestamp:** _________  
**Status:** ⬜ PASS / ⬜ FAIL  
**Subfolder ID:** _________  
**Notas:**

### 2.4 Subir 3 imágenes test

- [ ] Navegar a sección Productos (si disponible)
- [ ] Crear 3 productos con imágenes
- [ ] **VERIFICAR:** Imágenes subidas a carpeta "Imagenes"
- [ ] **MÉTODO:** Drive → carpeta empresa → Imagenes → ver 3 archivos

**Timestamp:** _________  
**Status:** ⬜ PASS / ⬜ FAIL  
**Image Files:** _________, _________, _________  
**Notas:**

---

## 3. Operaciones Google Sheets

### 3.1 Escribir datos en Sheet Privado

- [ ] Crear categoría en app (ej: "Electrónica")
- [ ] **VERIFICAR:** Categoría aparece en Sheet Privado tab "Categories"
- [ ] **MÉTODO:** Abrir Sheet Privado → tab Categories → ver row

**Timestamp:** _________  
**Status:** ⬜ PASS / ⬜ FAIL  
**Row Data:** _________  
**Notas:**

### 3.2 Leer datos desde Sheet

- [ ] Agregar dato manualmente en Sheet Privado (ej: nueva categoría "Ropa")
- [ ] Ejecutar sync en app (refresh o esperar WorkManager)
- [ ] **VERIFICAR:** Dato aparece en app
- [ ] **MÉTODO:** Abrir lista categorías en app → ver "Ropa"

**Timestamp:** _________  
**Status:** ⬜ PASS / ⬜ FAIL  
**Notas:**

### 3.3 Preservación de fórmulas (si aplica)

- [ ] Agregar fórmula en Sheet: `=COUNTIF(Products!E:E, A2)`
- [ ] Escribir dato desde app que afecte esa celda
- [ ] **VERIFICAR:** Fórmula NO fue sobrescrita
- [ ] **MÉTODO:** Abrir Sheet → ver celda con fórmula intacta

**Timestamp:** _________  
**Status:** ⬜ PASS / ⬜ FAIL / ⬜ N/A (no implementado aún)  
**Notas:**

---

## 4. Validación de Seguridad (scope restringido)

### 4.1 Intentar listar archivos fuera de app

**NOTA:** Esta prueba requiere código adicional temporal o inspección de logs.

- [ ] (Opcional) Agregar log temporal en DriveApi:
  ```kotlin
  val result = driveService.files().list().execute()
  Log.d("S10_SECURITY", "Files visible: ${result.files.size}")
  ```
- [ ] Ejecutar app
- [ ] **VERIFICAR:** Solo archivos creados por app son visibles
- [ ] **ESPERADO:** NO se listan archivos personales del usuario

**Timestamp:** _________  
**Status:** ⬜ PASS / ⬜ FAIL / ⬜ SKIP (requiere código temporal)  
**Files Listed Count:** _________  
**Notas:**

---

## 5. Pruebas de Regresión

### 5.1 Flujo completo onboarding

- [ ] Desinstalar app
- [ ] Revocar permisos OAuth
- [ ] Reinstalar app
- [ ] Ejecutar flujo onboarding completo (SignIn → Create Workspace → Add Category → Add Product)
- [ ] **VERIFICAR:** Todo funciona end-to-end

**Timestamp:** _________  
**Duration:** _________ min  
**Status:** ⬜ PASS / ⬜ FAIL  
**Notas:**

### 5.2 Mensaje de privacidad UI (opcional)

- [ ] Revisar pantalla SignIn
- [ ] **VERIFICAR:** Mensaje de privacidad menciona scope reducido
- [ ] **ESPERADO:** Texto actualizado (si fue modificado en código)

**Status:** ⬜ PASS / ⬜ FAIL / ⬜ N/A  
**Notas:**

---

## 6. Resultados Finales

### Criterios de Éxito

- [ ] ✅ Autenticación exitosa con scope reducido
- [ ] ✅ Operaciones Drive funcionan (crear, leer, escribir)
- [ ] ✅ NO puede listar archivos fuera de carpeta app

### Decisión Técnica

**¿Adoptar `DriveScopes.DRIVE_FILE` en producción?**

- [ ] ✅ SÍ - Todas las pruebas PASS
- [ ] 🔄 PARCIAL - Algunas pruebas FAIL (documentar)
- [ ] ❌ NO - Cambio rompe funcionalidad crítica

### Tiempo Real

- **Estimado:** 1h
- **Real:** _________ h _________ min
- **Desviación:** +/- _________ min

### Hallazgos Importantes

1. _________
2. _________
3. _________

### Próximos Pasos

- [ ] Mergear cambios a `dev/mvp-febrero` (si exitoso)
- [ ] Actualizar `Spikes_Resultados.md` con hallazgos
- [ ] Documentar cambios UI en user stories (si aplica)

---

**Ejecutor:** Braian Rios  
**Fecha completado:** __/__/2026  
**Branch:** `spike/oauth-scope-reducido`
