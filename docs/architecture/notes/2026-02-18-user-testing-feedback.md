# Feedback de pruebas rápidas de usuario (2026-02-18)

## Estado
Registro breve de hallazgos UX/UI reportados en prueba espontánea y su resolución técnica.

## Hallazgos y resolución

1. **Insets en pantallas full-screen con CTA sticky (navegación por botones)**
   - Estado: **Resuelto**
   - Ajuste: `navigationBarsPadding()` en `ProductFormScreen`, `CategoryFormScreen`, `OrderManualScreen`, `OrderDetailScreen`, `CartSummary`, `CompanySelectionView`.

2. **Drawer: nombre/email de usuario tapado en footer**
   - Estado: **Resuelto**
   - Ajuste: `navigationBarsPadding()` en footer de usuario de `Drawer`.

3. **Badge del carrito crece demasiado con cantidades altas**
   - Estado: **Resuelto**
   - Ajuste: badge visual cap a `99+` en `ClientCatalogScreen` y `ProductDetailScreen`.

4. **Tap en item de carrito no abre detalle del producto**
   - Estado: **Resuelto**
   - Ajuste: navegación a `ProductDetail` cliente desde card de carrito (`CartScreen` + `NavHost` + `OrderProductListItem`).

5. **Cantidad de producto sin tope efectivo (anti-troll)**
   - Estado: **Resuelto**
   - Ajuste: cap hard de 99 unidades por producto en capa de datos (`CartRepository.addItem` / `updateQuantity`).

6. **Feedback cuando se intenta superar 99 unidades**
   - Estado: **Resuelto**
   - Ajuste:
     - Detalle de producto: snackbar explícita al superar cap.
     - Grilla catálogo cliente: snackbar explícita al llegar al cap (antes era silencioso).

7. **Notificaciones de sync tras ediciones en pantallas internas (producto/categoría)**
   - Estado: **Resuelto**
   - Ajuste: señal de retorno desde formularios + trigger de `syncAndNotify` en tabs `Products`/`Categories` al volver a `Home`.

8. **Label largo en tab de configuración con fuentes grandes**
   - Estado: **Resuelto**
   - Ajuste: tab renombrada a **EMPRESA** + icono `Domain` en `BottomBar`.
