# Panadería · acceso y catálogo local

Aplicación Android nativa en Kotlin y Jetpack Compose. Interfaces disponibles: acceso de demostración, splash, configuración local, catálogo, favoritos, carrito y pedidos. El catálogo permite buscar, filtrar por categoría, guardar favoritos y añadir productos al carrito mediante Room y MVVM. Favoritos permite consultar los productos guardados, quitarlos o añadirlos al carrito. Carrito permite modificar cantidades, quitar productos, consultar subtotal y total, y confirmar el pedido: la operación reutiliza `createFromCart()`, guarda el pedido con sus productos y cantidades, y vacía el carrito en una sola transacción. Pedidos permite consultar el historial con filtros Todos/Guardados/Archivados, fecha, total, estado y detalle de productos. Ticket dispone de una pantalla preparada para recibir un pedido existente y generar un QR local escaneable. La administración de maestros queda aplazada por decisión de alcance; el CRUD visual completo sigue pendiente. No hay registro ni servidor.

Consulta [la arquitectura, las tablas y su relación con Figma](docs/ARQUITECTURA.md). La documentación identifica las dos interfaces inferiores del diseño (Favoritos y Tus pedidos); la consulta y la creación de pedidos desde el carrito ya están implementadas.

## Probar el acceso

- Correo: `demo@panaderia.com`
- Contraseña: `Pan12345`
- El botón **Usar datos de demostración** completa ambos campos; después pulsa **Iniciar sesión**.
- Son credenciales públicas de una demostración, no un mecanismo de autenticación para usuarios reales.

## Explorar el catálogo

Después de acceder, pulsa **Cargar catálogo de ejemplo** si no hay productos. Esta acción explícita carga los cuatro productos de referencia y sus categorías sin sustituir un catálogo existente. Buscar y filtrar no modifica los datos. Los corazones y Añadir persisten localmente. El menú inferior abre **Favoritos**, **Carrito** y **Pedidos**; desde carrito se ajustan cantidades, se quitan productos y se muestra el total con retiro sin costo. En carrito, **Confirmar pedido** guarda el pedido con sus productos, cantidades y total, vacía el carrito y abre **Pedidos** para consultarlo. La confirmación se bloquea mientras está en curso y valida que el carrito no esté vacío. Room conserva favoritos y carrito al volver a abrir la aplicación. **Mi cuenta** permite abrir Configuración o cerrar sesión. El perfil local se crea una sola vez por correo.

## Persistencia local

En Android se utiliza Preferences DataStore en lugar del `localStorage` del navegador. El archivo privado es `files/datastore/panaderia_local.preferences_pb`.

| Preferencia | Valor inicial | Comportamiento |
| --- | --- | --- |
| Recordar mi correo | Activada | Guarda el correo después de acceder; desactivarla borra el correo recordado. |
| Mantener mi sesión | Desactivada | Al activarla, conserva la sesión de demostración entre aperturas. Desactivarla elimina la sesión persistida sin cerrar la sesión actual. |

Cerrar sesión elimina siempre la sesión persistida. Las preferencias permanecen. La contraseña solo vive en memoria durante la entrada: no se guarda en DataStore ni en el estado de restauración de Android. El archivo de DataStore queda excluido de las copias de seguridad y de la transferencia entre dispositivos.

El splash nativo permanece mientras se leen las preferencias, sin retrasos artificiales. Si la lectura falla o supera cinco segundos, se ofrece reintentar. Una rotación conserva la sesión actual mediante ViewModel; al terminar el proceso se recupera únicamente la sesión que el usuario haya elegido mantener.

## Diseño centrado en el usuario (DCU)

Esta entrega aplica criterios de DCU, pendientes de validación con usuarios reales. Hipótesis inicial: la persona quiere acceder sin confusión, reconocer los errores y controlar qué recuerda su teléfono.

- Lenguaje cercano, etiquetas permanentes y errores específicos junto a cada campo.
- Botones amplios, formulario desplazable, ancho limitado en pantallas grandes y espacio seguro para teclado y barras del sistema.
- Contraseña ocultable, teclado de correo y acciones Siguiente/Listo.
- Acceso directo al catálogo, estados de carga, resultados vacíos y recuperación ante errores.
- Sesión persistente voluntaria; explicación de cada opción y desactivación reversible.
- Estilo crema y terracota autorizado como aproximación al prototipo. El símbolo de panadería es provisional; la fidelidad exacta a Figma queda pendiente por el límite del conector.

### Próxima evaluación con usuarios

Proponer a 3–5 personas: (1) entrar con la cuenta demo, (2) corregir una contraseña equivocada, (3) recordar el correo sin mantener sesión, (4) activar mantener sesión y reabrir, (5) cerrar sesión y reabrir. Registrar finalización sin ayuda, tiempo, errores, comprensión de las dos preferencias y comentarios. Revisar también TalkBack, tamaño de texto grande, orientación horizontal y teclado abierto. Ajustar el diseño según esos resultados; no se afirma que esta validación ya se haya realizado.

## Desarrollo y verificación

Abrir esta carpeta en Android Studio, sincronizar Gradle y ejecutar el módulo `app`. El proyecto conserva sus versiones originales de SDK y herramientas; requiere la plataforma Android 37 y el JDK solicitado por `gradle/gradle-daemon-jvm.properties`.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
# Con un emulador o dispositivo conectado:
.\gradlew.bat :app:connectedDebugAndroidTest
```

Las pruebas unitarias de `MoneyTest` verifican importes exactos y entradas inválidas; `OrderModelTest` verifica las reglas del modelo de pedidos preparado para otra etapa. `RoomWorkflowTest` contiene pruebas instrumentadas de transacciones y datos de ejemplo, que requieren un dispositivo y no se han ejecutado en esta entrega.

Las pruebas unitarias de `DemoAccountTest` comprueban validación y rechazo de credenciales incorrectas. Las pruebas instrumentadas de `LocalPreferencesTest` utilizan archivos reales de DataStore en Android para comprobar las opciones predeterminadas, borrado de datos recordados, reapertura del almacenamiento y cierre de sesión. Se ejecutan en Android porque el reemplazo de archivos de su implementación no funciona igual al ejecutarla directamente sobre Windows. `LoginFlowTest` comprueba errores del formulario, acceso, configuración, recreación y cierre de sesión.

Referencias oficiales: [Preferences DataStore](https://developer.android.com/topic/libraries/architecture/datastore) y [SplashScreen](https://developer.android.com/develop/ui/views/launch/splash-screen/migrate).

El ticket recibe un `Order` desde el módulo de pedidos, usa su `qrPayload` y no crea ni modifica pedidos. La creación desde el carrito y su consulta en **Pedidos** ya están conectadas; falta que la pantalla de pedidos abra `TicketScreen` con el pedido seleccionado y gestione el regreso.
