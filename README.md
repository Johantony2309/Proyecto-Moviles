# Pastelería Andony · aplicación Android local

Aplicación Android nativa en Kotlin y Jetpack Compose. Interfaces disponibles: acceso de demostración, splash, configuración local, catálogo, favoritos, carrito y pedidos. El catálogo permite buscar, filtrar por categoría, guardar favoritos y añadir productos al carrito mediante Room y MVVM. Favoritos permite consultar los productos guardados, quitarlos o añadirlos al carrito. Carrito permite modificar cantidades, quitar productos, consultar subtotal y total, y confirmar el pedido: la operación reutiliza `createFromCart()`, guarda el pedido con sus productos y cantidades, y vacía el carrito en una sola transacción. Pedidos permite consultar el historial con filtros Todos/Guardados/Archivados, fecha, total, estado y detalle de productos. Ticket consulta el pedido en Room por su identificador y genera un QR local escaneable. La administración de maestros queda aplazada por decisión de alcance; el CRUD visual completo sigue pendiente. No hay registro ni servidor.

## Documentación

- [Evidencias actualizadas, fecha, commit y alcance del criterio documental](docs/EVIDENCIAS.md).
- [Casos de uso: reglas de dominio y conservación de transacciones Room](docs/CASOS_DE_USO.md).
- [Arquitectura Hilt, capas, estado y parámetros de navegación](docs/ARQUITECTURA.md).
- [Interfaz, accesibilidad, imágenes, logo y barra inferior](docs/FASE_5_INTERFAZ.md).
- [Guion de demostración offline y persistencia](docs/DEMOSTRACION_OFFLINE.md).
- [Registro de correcciones, verificaciones y evidencias por fase](docs/FASES_CORRECCION.md).

## Probar el acceso

- Correo: `demo@panaderia.com`
- Contraseña: `Pan12345`
- El botón **Usar datos de demostración** completa ambos campos; después pulsa **Iniciar sesión**.
- Son credenciales públicas de una demostración, no un mecanismo de autenticación para usuarios reales.

## Explorar el catálogo

Después de acceder, pulsa **Cargar catálogo de ejemplo** si no hay productos. Esta acción explícita carga los cuatro productos de referencia y sus categorías sin sustituir un catálogo existente. Buscar y filtrar no modifica los datos. Los corazones y Añadir persisten localmente. El menú inferior abre **Favoritos**, **Carrito** y **Pedidos**; desde carrito se ajustan cantidades, se quitan productos y se muestra el total del pedido local de demostración. En carrito, **Confirmar pedido** guarda el pedido con sus productos, cantidades y total, vacía el carrito y abre **Pedidos** para consultarlo. La confirmación se bloquea mientras está en curso y valida que el carrito no esté vacío. Room conserva favoritos y carrito al volver a abrir la aplicación. **Mi cuenta** permite abrir Configuración o cerrar sesión. El perfil local se crea una sola vez por correo.

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
- Fondo crema, logo de Pastelería Andony e imágenes locales por producto implementados. Los controles usan naranja oscuro para mejorar el contraste con blanco. La fidelidad completa al prototipo sigue pendiente de comparación visual con la referencia original.
- Cabecera y logo adaptativos según altura de ventana; tarjetas de carrito/favoritos ajustadas al ancho y escala de texto, filtros de pedidos con salto de línea y botones de cantidad con descripciones accesibles. El carrito y el ticket aclaran que el pedido se guarda en el dispositivo y no se envía al negocio.

**PENDIENTE en la interfaz actual:** validación manual con TalkBack, pantallas pequeñas/horizontales, teclado, texto grande y contraste visual, además de la comparación con Figma. Las capturas de fase 4 son históricas y no acreditan las mejoras posteriores.

### Próxima evaluación con usuarios

Proponer a 3–5 personas: (1) entrar con la cuenta demo, (2) corregir una contraseña equivocada, (3) recordar el correo sin mantener sesión, (4) activar mantener sesión y reabrir, (5) cerrar sesión y reabrir. Registrar finalización sin ayuda, tiempo, errores, comprensión de las dos preferencias y comentarios. Revisar también TalkBack, tamaño de texto grande, orientación horizontal y teclado abierto. Ajustar el diseño según esos resultados; no se afirma que esta validación ya se haya realizado.

## Desarrollo y verificación

Abrir esta carpeta en Android Studio, sincronizar Gradle y ejecutar el módulo `app`. El proyecto conserva sus versiones originales de SDK y herramientas; requiere la plataforma Android 37 y el JDK solicitado por `gradle/gradle-daemon-jvm.properties`.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
# Con un emulador o dispositivo conectado:
.\gradlew.bat :app:connectedDebugAndroidTest
```

Verificación del **18 de octubre de 2026**, código `28abdbda9100c8d56dec70e330f62a295863af43`: **22 pruebas unitarias aprobadas, 0 fallos, 0 errores y 0 omitidas**, repetidas con `:app:testDebugUnitTest --rerun`; lint con **0 errores y 24 advertencias**. `:app:assembleDebug` y `:app:compileDebugAndroidTestKotlin` finalizaron correctamente reutilizando resultados vigentes (UP-TO-DATE). Los [informes adjuntos y comandos](docs/EVIDENCIAS.md#verificación-del-código-actual) distinguen pruebas ejecutadas, compilación y evidencia histórica. No se repitió instrumentación ni validación visual en esta actualización.

El criterio recibido es **«Actualizar documentación y evidencias», prioridad Media, sin puntos directos**. La rúbrica completa no está disponible: no se atribuyen a este criterio requisitos de otras etapas, como CRUD visual completo, un mínimo de tablas o un informe Word. Las cinco capturas disponibles son históricas del 16/10; su fecha, commit de incorporación y límites se detallan en el [inventario](docs/EVIDENCIAS.md#capturas-históricas-disponibles). Faltan capturas de la interfaz actual.

Las pruebas unitarias de `MoneyTest` verifican importes exactos y entradas inválidas; `OrderModelTest` verifica las reglas del modelo de pedidos. `RoomWorkflowTest` contiene pruebas instrumentadas de transacciones y datos de ejemplo, que utilizan una base aislada en un dispositivo.

Las pruebas unitarias de `DemoAccountTest` comprueban validación y rechazo de credenciales incorrectas. Las pruebas instrumentadas de `LocalPreferencesTest` utilizan archivos reales de DataStore en Android para comprobar las opciones predeterminadas, borrado de datos recordados, reapertura del almacenamiento y cierre de sesión. Se ejecutan en Android porque el reemplazo de archivos de su implementación no funciona igual al ejecutarla directamente sobre Windows. `LoginFlowTest` comprueba errores del formulario, acceso, configuración, recreación y cierre de sesión.

Referencias oficiales: [Preferences DataStore](https://developer.android.com/topic/libraries/architecture/datastore) y [SplashScreen](https://developer.android.com/develop/ui/views/launch/splash-screen/migrate).

La navegación se define en `BakeryApp` con un único `NavHost` y `NavController`: acceso, configuración, catálogo, favoritos, carrito, pedidos y ticket. `AppDestinations` centraliza las rutas. La barra inferior guarda y restaura el estado de las pestañas; al cerrar sesión se eliminan el historial autenticado y sus estados guardados.

Pedidos abre `ticket/{orderId}` con un argumento `Long`. `TicketViewModel` consulta el pedido del cliente actual mediante `OrderRepository.order()` y observa Room, sin recibir una copia del pedido desde otra pantalla. Navigation conserva el argumento al recrear la actividad. El ticket no crea ni modifica pedidos.

`NavigationFlowTest` verifica pestañas, Configuración, Atrás, recreación y cierre de sesión. `OrdersRegressionTest` verifica filtros repetidos, ticket, giro y regreso a Pedidos. Se ejecutan con un emulador o dispositivo conectado.

## Organización de pantallas y estado

Catálogo, Favoritos y Carrito tienen ViewModels independientes. Pedidos separa su ViewModel y su estado del archivo visual. Las funciones Route obtienen los ViewModels y recogen el estado con `collectAsStateWithLifecycle()`; Content recibe datos y callbacks.

Configuración utiliza su propio `SettingsViewModel`, con alcance del destino; `LoginViewModel` conserva la sesión con alcance Activity. `FilterCatalogUseCase` filtra el catálogo y los casos de validación de pedidos se ejecutan dentro de las transacciones de `RoomOrderRepository`. La navegación usa barra inferior en ancho compacto y barra lateral en ventanas medianas/amplias; las cuadrículas ajustan columnas al ancho y escala de fuente. La [arquitectura](docs/ARQUITECTURA.md) explica sus responsabilidades y límites.

Cada ViewModel expone un único StateFlow de solo lectura. Las pantallas de negocio usan `ScreenState.Loading/Content/Error` y los avisos puntuales usan un canal expuesto como Flow. Confirmar un pedido emite `OrderCreated`, que el coordinador de navegación convierte en la apertura de Pedidos. Los datos confirmados permanecen en Room.

`ViewModelStateTest` cubre errores y reintentos, sincronización entre pantallas, restauración de filtros y confirmación duplicada. El registro de fases contiene los resultados de la ejecución en el teléfono USB.

El [guion de demostración offline](docs/DEMOSTRACION_OFFLINE.md) explica cómo comprobar favoritos, carrito y pedidos después de forzar la detención y reabrir sin conexión. `OfflineFlowTest` permite repetir el recorrido por fases y restaurar los datos previos. Los resultados y correcciones se documentan en [FASES_CORRECCION.md](docs/FASES_CORRECCION.md).
