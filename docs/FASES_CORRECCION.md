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
