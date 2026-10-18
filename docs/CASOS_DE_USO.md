# Apartado 5: casos de uso

Se extrae lógica existente a `domain/usecase`. Las tres clases tienen `operator fun invoke`, reciben modelos de dominio y usan inyección por constructor con Hilt. No dependen de Room ni de clases de presentación.

| Caso de uso | Regla extraída | Integración |
| --- | --- | --- |
| `ValidateCheckoutUseCase` | Carrito no vacío, productos disponibles, cantidades de 1 a 999 y detección de desbordamiento del total. | `RoomOrderRepository.createFromCart` |
| `ValidateOrderItemChangeUseCase` | Pedido existente y guardado, pertenencia del producto, cantidades de 0 a 999, impedir eliminar la última línea y comprobar importes con precios históricos. | `RoomOrderRepository.updateItemQuantity` |
| `FilterCatalogUseCase` | Búsqueda por nombre o descripción sin distinguir mayúsculas, eliminación de espacios exteriores y filtro por categoría; restablece una categoría eliminada. | `CatalogViewModel` |

## Atomicidad de Room

Los casos de validación se invocan **dentro de `db.withTransaction`**, después de leer el estado actual y antes de escribir. Crear el pedido, insertar sus líneas y vaciar el carrito siguen siendo una única transacción. La edición también conserva la lectura, validación y modificación en una sola transacción. No se consulta el carrito desde el ViewModel para validarlo antes de guardar, lo que evitaría compartir la misma instantánea. La transacción de repetición de pedidos no cambia.

Los valores por defecto de los constructores mantienen la compatibilidad con las pruebas que crean repositorios y ViewModels directamente; Hilt puede resolver los casos de uso mediante `@Inject`.

## Pruebas

Código de referencia: `28abdbda9100c8d56dec70e330f62a295863af43`. En la revisión documental del **18/10/2026** se repitieron las 22 pruebas unitarias (9 de `UseCasesTest`), sin fallos, errores ni omisiones. Los [XML y el resumen](EVIDENCIAS.md#verificación-del-código-actual) quedan adjuntos. La ejecución Room descrita abajo es un resultado previo; no se repitió instrumentación en esta revisión.

`UseCasesTest` cubre límites de cantidades, carrito vacío, disponibilidad, desbordamiento de líneas y del total, pedidos archivados o inexistentes, pertenencia de líneas, eliminación de la última línea, precios históricos y filtros del catálogo.

Verificación del 18 de octubre de 2026: `assembleDebug` y `compileDebugAndroidTestKotlin` correctos; 22 pruebas unitarias aprobadas (9 nuevas). Las 6 pruebas de `RoomWorkflowTest` pasaron en un Samsung SM-A750G con Android 10, incluidas las dos nuevas comprobaciones de rechazo sin alterar datos. Se utilizó una base Room en memoria.

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin
# Con un dispositivo o emulador conectado, verificar también las transacciones reales:
.\gradlew.bat :app:connectedDebugAndroidTest
```
