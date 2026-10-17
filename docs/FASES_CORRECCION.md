# Registro de correcciones del proyecto

Este documento registra las fases solicitadas y sus comprobaciones. Complementa [ARQUITECTURA.md](ARQUITECTURA.md); no declara que todos los criterios de la rúbrica ni el CRUD visual completo estén terminados.

## Fase 1 — Problemas de Pedidos

**Problemas:** pulsar el filtro ya seleccionado podía activar una carga sin una nueva emisión; el ticket seleccionado se perdía al recrear la pantalla.

**Corrección vigente:** OrdersViewModel guarda el filtro en SavedStateHandle y no inicia una carga al seleccionarlo. Un valor idéntico no cambia el StateFlow. El ticket es un destino con argumento `orderId`; su ViewModel observa el pedido en Room. La solución de restauración de esta fase evolucionó hacia la navegación de la fase 2.

**Archivos:** OrdersViewModel.kt, OrdersState.kt, OrdersRoute.kt, TicketRoute.kt, TicketViewModel.kt, AppDestinations.kt y BakeryApp.kt.

**Prueba:** OrdersRegressionTest pulsa repetidamente Todos/Guardados/Archivados, abre un ticket, recrea la Activity, cambia orientación, visita Configuración y vuelve con los controles de la app y Atrás. Crea dos pedidos exclusivos de prueba y los elimina al terminar, sin vaciar el carrito ni borrar el historial existente.

## Fase 2 — Navegación

**Problema:** selección manual de sección y apertura de configuración/ticket mediante variables de composición.

**Corrección:** un NavHost/NavController, siete destinos, grafo autenticado, barra inferior conectada al controlador, argumento Long para ticket y consulta por cliente en Room. Salir limpia el historial autenticado y las pilas guardadas. La limpieza de rutas anidadas se ejecuta solo mientras el grafo SHOP está activo.

**Archivos:** AppDestinations.kt, BakeryApp.kt, ShopScreen.kt, TicketRoute.kt, TicketViewModel.kt, di/DataModule.kt y dependencia Navigation Compose.

**Pruebas:** NavigationFlowTest verifica Configuración antes y después del acceso, pestañas, Atrás, búsqueda conservada, recreación en Carrito/Configuración, filtro conservado y cierre de sesión. Comprueba que un nuevo acceso no recupere filtros de la sesión anterior y que Atrás desde acceso termine la Activity.

## Fase 3 — Organización de pantallas y estado

**Problema:** ShopViewModel concentraba catálogo, favoritos y carrito; OrdersViewModel y OrdersState estaban en el archivo visual. Las banderas de carga/error y mensajes persistidos en estado mezclaban responsabilidades.

**Correcciones:**

- CatalogViewModel, FavoritesViewModel y CartViewModel propios, gestionados por Hilt.
- Route obtiene el ViewModel y recoge estado; Content recibe estado y acciones. Acceso, Configuración, Pedidos y Ticket también siguen esa separación. ShopRoute mantiene únicamente el ViewModel de contadores del grafo.
- OrdersViewModel y OrdersState extraídos a archivos propios.
- Un StateFlow público de solo lectura por ViewModel; estado mutable privado.
- Loading, Content y Error excluyentes para negocio; LoginStatus excluyente para acceso y carga de preferencias.
- Error de escritura como aviso sin descartar contenido; bloqueo de escrituras repetidas mientras la operación está activa.
- Avisos y confirmación mediante Channel/Flow; consumo solo en RESUMED. Sesión y datos de pedidos permanecen como estado durable, no dependen de recibir un aviso.
- SavedStateHandle conserva consulta/categoría y filtro/detalle de Pedidos. La contraseña no se serializa.
- Se mantiene collectAsStateWithLifecycle. La inyección se migró a Hilt conservando la base de datos.

**Pruebas nuevas:** ViewModelStateTest usa Room en memoria para comprobar carga/error/reintento, error de escritura conservando contenido, aviso que no se repite al volver a suscribirse, sincronización catálogo/favoritos/carrito, restauración de selección y dos confirmaciones simultáneas que generan un solo pedido y un solo evento de navegación.

**Límite explícito:** los canales transportan efectos transitorios y no garantizan entrega tras muerte del proceso. El pedido ya confirmado permanece consultable en Room. La recreación de Activity y la restauración de valores SavedStateHandle se verifican; no se equiparan a una prueba de muerte real del proceso.

## Ejecución de verificación

Fecha de esta corrección: 14 de octubre de 2026. Medio solicitado: teléfono físico Samsung SM-A750G conectado por USB, sin emulador.

**Resultado:** compilación correcta de la app y del APK de pruebas; 8 pruebas unitarias y 9 instrumentadas aprobadas.

| Comprobación | Resultado |
| --- | --- |
| assembleDebug / assembleDebugAndroidTest | BUILD SUCCESSFUL |
| testDebugUnitTest | 8 aprobadas, 0 fallos |
| ViewModelStateTest | 3 aprobadas: errores/reintento, sincronización/restauración y confirmación única |
| RoomWorkflowTest | 4 aprobadas: transacciones, integridad histórica, borrado y catálogo de ejemplo |
| NavigationFlowTest | 1 aprobada: pestañas, Atrás, configuración, recreación y cierre de sesión |
| OrdersRegressionTest | 1 aprobada: filtros repetidos, ticket, orientación y regreso |

En la primera ejecución, las 7 pruebas de estado/Room pasaron y las 2 visuales fallaron al buscar nodos durante transiciones de pantalla. Se añadió espera explícita del contenido cargado y cierre del teclado de búsqueda antes de probar Atrás; las 2 pruebas visuales pasaron al repetirlas. Se conservaron las aserciones de restauración y todos los casos.

Las pruebas de ViewModels y repositorios usan bases en memoria. Las pruebas visuales utilizan la app instalada: navegan y cambian la sesión demo; OrdersRegressionTest elimina únicamente sus pedidos de prueba. No se desinstaló la aplicación ni se borró su almacenamiento.

Comandos reproducibles con el JDK y SDK configurados:

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest
$deviceSerial = 'SERIAL_DEL_TELEFONO'
adb -s $deviceSerial install -r app/build/outputs/apk/debug/app-debug.apk
adb -s $deviceSerial install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s $deviceSerial shell am instrument -w -r -e class 'com.example.proyectopanaderia.ViewModelStateTest,com.example.proyectopanaderia.NavigationFlowTest,com.example.proyectopanaderia.OrdersRegressionTest,com.example.proyectopanaderia.RoomWorkflowTest' com.example.proyectopanaderia.test/androidx.test.runner.AndroidJUnitRunner
```

En este entorno se invoca la distribución local Gradle 9.5.0 con el JDK disponible y `--offline --max-workers=2`. La instrumentación se ejecuta por ADB directamente para no depender de descargar componentes adicionales del ejecutor Gradle.

## Fase 4 — Flujo completo y persistencia

Se añadió [el guion de demostración offline](DEMOSTRACION_OFFLINE.md) y `OfflineFlowTest`, con fases separadas de preparación, reapertura sin conexión, revisión visual y limpieza. La prueba respalda el carrito previo en un archivo que sobrevive a la detención del proceso y elimina solamente sus registros temporales al finalizar.

Se actualizaron los selectores antiguos de LoginFlowTest y las esperas de arranque/recreación de Compose. Se añadieron identificadores de prueba a las tarjetas de productos, favoritos y pedidos, y una descripción accesible al QR.

La ejecución del 14 de octubre de 2026 obtuvo compilación correcta, 8 pruebas unitarias aprobadas y lint con 0 errores y 23 advertencias. La batería instrumentada general aprobó 14 casos; el caso offline se omitió deliberadamente en esa batería para ejecutarlo por fases. Se detectó que el reposo automático del teléfono interfería con las pruebas visuales. Los registros locales están en `build/phase4/`.

Al retomar el 16 de octubre, la verificación actual de compilación, pruebas unitarias y lint terminó con BUILD SUCCESSFUL: 8 pruebas unitarias aprobadas y lint con 0 errores y 16 advertencias. Esta ejecución de lint utilizó las dependencias locales en modo offline. El teléfono USB siguió conectado, pero permanecía en reposo incluso después del comando de despertar. El intento inicial del recorrido se detuvo antes de acceder a la app; la fase de limpieza restauró el carrito y retiró sus datos temporales correctamente.

### Correcciones encontradas durante la validación

- Encabezado compacto cuando la ventana tiene menos de 480 dp de altura: el logo deja espacio útil para el contenido horizontal. Se utiliza la altura de `LocalWindowInfo`, convertida con la densidad actual.
- Tamaño del QR limitado por el espacio disponible en Ticket. Sus módulos se dibujan en píxeles enteros y centrados: el dibujo fraccionario producía líneas finas entre módulos y falló la lectura de la captura vertical. Después del ajuste, el lector decodificó el contenido esperado en vertical y horizontal.
- Las pruebas esperan la transición nativa de orientación y desplazan las listas perezosas hasta el elemento antes de buscar sus acciones. Volver al carrito conserva su posición de desplazamiento; no se presupone que el título siga visible.
- La prueba de ticket decodifica el QR de una captura real de pantalla y lo compara con `qrPayload` guardado en Room, con espera acotada del fotograma. No se limita a comprobar la existencia del Canvas.

### Verificación final del 16 de octubre de 2026

Dispositivo: Samsung SM-A750G, Android 10, conectado por USB. Se instaló la compilación actual conservando el almacenamiento.

| Comprobación | Resultado |
| --- | --- |
| Compilación de app y APK instrumentado | BUILD SUCCESSFUL |
| Pruebas unitarias | 8 aprobadas, 0 fallos |
| Lint | 0 errores, 16 advertencias de recursos, etiqueta y versiones de dependencias |
| Batería instrumentada general | 14 aprobadas, 0 fallos; OfflineFlowTest omitida aquí y ejecutada por fases explícitas |
| `prepare` | Aprobada: Catálogo → Favoritos → Carrito → Confirmar → Pedidos → Ticket; lectura del QR y nueva unidad pendiente |
| Detención real | `am force-stop`; `pidof` sin proceso antes de reabrir |
| Sin conexión | Modo avión activado, Wi-Fi desactivado; `Active default network: none` |
| `offline` | Aprobada: sesión restaurada, favorito, cantidad del carrito, pedido, total, token y QR conservados |
| `layout` | Aprobada: teclado visible, búsqueda accesible, texto al 150 %, giro horizontal, búsqueda conservada, confirmar accesible y QR legible |
| `cleanup` | Aprobada: datos temporales retirados y carrito/preferencia previa restaurados |

El pedido temporal de la ejecución final fue el #21, total $1,50. Se eliminó mediante la limpieza de la prueba; las capturas conservan la evidencia. Los logs completos están en `build/phase4/prepare-final.txt`, `offline-final.txt`, `layout-final.txt`, `cleanup-final.txt`, `network-final.txt` e `instrumentation-final.txt`.

La batería general terminó en 36,231 s e informa `OK (15 tests)`: incluye 14 casos aprobados y el caso offline omitido por su condición explícita. Cubre preferencias, acceso, navegación/Atrás/cierre de sesión, regresiones de Pedidos, Room y estados de ViewModels. Las pruebas de acceso cambian la sesión de demostración. La validación no borró el almacenamiento de la app.

Se verificó la restauración del teléfono: modo avión 0, Wi-Fi 1, datos móviles 1, fuente 1.1, rotación automática 0, orientación de usuario 0 y mantener pantalla encendida al cargar 0. El archivo `phase4-fixture.json` ya no existe tras la limpieza.

Evidencias: [carrito offline](evidencias/fase4/phase4-offline-cart.png), [ticket offline](evidencias/fase4/phase4-offline-ticket.png), [teclado](evidencias/fase4/phase4-keyboard.png), [carrito horizontal](evidencias/fase4/phase4-landscape-cart.png) y [QR horizontal](evidencias/fase4/phase4-landscape-ticket.png).

Alcance: validación en este teléfono con Android 10 y fuente al 150 %. La decodificación desde la captura comprueba la imagen renderizada; no equivale a una prueba con la cámara de otro teléfono. No se generalizan estos resultados a todos los dispositivos ni a una evaluación completa de TalkBack.

## Fase 5 — Pulido de la interfaz actual

Consultar [FASE_5_INTERFAZ.md](FASE_5_INTERFAZ.md) para los cambios de cabecera, contraste, nombre accesible y alcance local del ticket, junto con el estado de la comparación con el prototipo. Las imágenes por producto ya están implementadas.
