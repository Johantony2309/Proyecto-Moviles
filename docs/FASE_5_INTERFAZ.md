# Fase 5 — Pulido de la interfaz

## Cambios

### Correcciones de Roberto (`8d74664`)

- Nombre visible y accesible: **Pastelería Andony**, centralizado en `app_name` para el lanzador, la cabecera de acceso, el logo de la tienda y el ticket. El logo junto al nombre en acceso es decorativo para evitar una lectura duplicada.
- Cabecera de tienda según altura de ventana: menos de 480 dp, cabecera 80 dp y logo 48 dp; entre 480 y 699 dp, 104/64 dp; desde 700 dp, 120/88 dp. Sustituye los 148/120 dp originales y conserva los espacios seguros del Scaffold.
- Controles activos: naranja `#B93600` con blanco. El logo original conserva su imagen y color.
- Ticket: aviso antes del QR de que el pedido solo se guarda en el dispositivo y no se envía al negocio ni confirma compra o reserva. Se elimina la instrucción que sugería retirar un pedido real. Carrito aclara lo mismo antes de confirmar.
- Imágenes locales por producto: ya implementadas y fuera de pendientes. Se corrigen las descripciones antiguas de README y arquitectura.

### Mejoras posteriores (`6dbb9ba`)

- `ShopScreen.kt`: la cabecera utiliza `expandedHeight` de `CenterAlignedTopAppBar`, en vez de forzar solo su alto externo. Se mantienen los tamaños adaptativos anteriores; la barra gestiona sus insets del sistema.
- `CartScreen.kt` y `FavoritesScreen.kt`: las tarjetas usan `BoxWithConstraints` y `FlowRow`. Cuando el ancho disponible es menor que `300.dp × fontScale`, imagen y contenido pasan a filas separadas, evitando comprimir el texto y las acciones junto a la imagen.
- `CartScreen.kt`: la cantidad tiene su propia línea y los botones pueden distribuirse en más de una fila. Las acciones «−» y «+» tienen descripciones que indican reducir/aumentar la cantidad del producto y un tamaño mínimo de 48 × 48 dp.
- `OrdersScreen.kt`: filtros y encabezado de cada pedido usan `FlowRow` para permitir saltos de línea cuando no caben horizontalmente.
- Barra inferior: los iconos son decorativos (`contentDescription = null`) porque cada pestaña ya tiene una etiqueta de texto. Se evita duplicar su nombre en la semántica; los contadores se mantienen.
- Las mejoras se limitan a composición y semántica de la UI. No modifican acciones de negocio ni la arquitectura Hilt documentada en [ARQUITECTURA.md](ARQUITECTURA.md).

## Recursos visuales y barra inferior

Los archivos de imagen se encuentran en `app/src/main/res/drawable/`:

| Archivo o componente | Función y uso comprobado | Procedencia/autoría verificable |
| --- | --- | --- |
| `croissant.png`, `cake.png`, `bread.png`, `roll.png` | Imágenes locales que `ProductPicture`, en `presentation/components/ShopComponents.kt`, selecciona mediante las claves `croissant`, `cake`, `bread` y `roll`. Se usan en catálogo, favoritos y carrito. | Recursos incluidos en el proyecto. No se ha verificado su fuente original, autoría ni licencia; no se atribuyen a Figma ni a un colaborador por haberlos incorporado al repositorio. |
| `ic_bakery.png` | Logo de Pastelería Andony: `BrandHeader` en acceso/configuración, cabecera de tienda y alternativa de `ProductPicture` cuando no hay una clave conocida. En acceso es decorativo junto al nombre; en tienda tiene el nombre de la app como descripción. | El archivo y su uso están comprobados; autoría, fuente original y licencia pendientes de acreditar. |
| `ShopScreen.kt`: `NavigationBar` / `NavigationBarItem` | Barra inferior Compose con Catálogo, Favoritos, Carrito y Pedidos. Favoritos y Carrito muestran contadores; Ticket mantiene seleccionada la pestaña Pedidos. | Componentes Material 3; iconos de `androidx.compose.material.icons`, no imágenes propias del proyecto. |
| `Icons.Filled.Home`, `Favorite`, `ShoppingCart` y `Icons.AutoMirrored.Filled.List` | Iconos de las cuatro pestañas, respectivamente. `AccountCircle` abre «Mi cuenta» en la cabecera. | Biblioteca de iconos Material de Compose, utilizada desde `ShopScreen.kt`. |

`ProductPicture` no añade una descripción de imagen redundante: el nombre del producto se muestra como texto en la tarjeta. El QR no es una captura ni un recurso estático: se genera localmente con ZXing y se dibuja en Compose. La documentación de recursos no acredita por sí sola una auditoría de accesibilidad.

## Contraste calculado

Se aplicó la fórmula de luminancia relativa sRGB de [WCAG 2.2, contraste mínimo](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html). Como referencia para texto normal se utiliza 4,5:1. Esto no constituye una auditoría completa de accesibilidad.

| Combinación | Antes | Después |
| --- | --- | --- |
| Blanco sobre naranja | 3,40:1 (`#FC4C01`) | 5,85:1 (`#B93600`) |
| Naranja sobre fondo crema `#FFFBF4` | 3,30:1 | 5,67:1 |

La pareja blanco/naranja también mejora la distinción de iconos blancos. Los estados deshabilitados y la imagen del logo se consideran por separado; no se afirma que todos los componentes cumplan únicamente a partir de estos dos cálculos.

## Comparación con el prototipo

Referencia proporcionada: [Panadería — Interfaces](https://www.figma.com/design/cBI3iCNtyTH0wDwsSIY8cW/Panader%C3%ADa-%E2%80%94-Interfaces?node-id=0-1).

La consulta del archivo fue rechazada por el límite de llamadas del plan Starter de Figma. La apertura alternativa en el navegador integrado agotó su tiempo de espera. No se obtuvo una imagen del prototipo para cotejarla: **la fidelidad visual completa sigue pendiente**. Para cerrar esa comparación se necesitan capturas exportadas o acceso al archivo cuando se restablezca la cuota.

Al disponer de la referencia, comparar acceso, catálogo, favoritos, carrito, pedidos y ticket: distribución, tipografía, colores, espaciado, logo, imágenes y estados vacíos. Documentar las diferencias intencionales de contraste, adaptación horizontal y aviso local.

## Validación

Estado: **implementación y verificaciones locales completadas; validación manual y comparación visual PENDIENTES**.

Verificaciones de la fase 2 realizadas el 17 de octubre de 2026, correspondientes al código incluido en `6dbb9ba`:

| Tarea ejecutada por separado | Resultado |
| --- | --- |
| `:app:assembleDebug` | Correcto |
| `:app:testDebugUnitTest` | 8 pruebas aprobadas, 0 fallos |
| `:app:lintDebug` | 0 errores, 24 advertencias de versiones, recursos y etiquetas |
| `git diff --check` | Sin errores |

El primer comando conjunto se interrumpió por el tiempo límite de Shell; las tareas posteriores terminaron correctamente. Las advertencias de lint no se declaran resueltas. Esta fase documental registra esos resultados; no constituye una nueva ejecución de pruebas.

### Comprobaciones manuales pendientes

- **PENDIENTE — TalkBack:** orden de foco, lectura de nombres, contadores, botones de cantidad y ausencia de anuncios duplicados.
- **PENDIENTE — pantallas pequeñas y horizontales:** cabecera/logo con barras del sistema, teclado y acceso a todas las acciones sin recortes.
- **PENDIENTE — texto grande:** revisar filtros, tarjetas, cantidad, barra inferior y ticket, incluida escala del 150 % o superior. No basta con que el código permita saltos de línea.
- **PENDIENTE — contraste visual:** comprobar textos, iconos y estados habilitados/deshabilitados en la app actual; los cálculos de colores anteriores no sustituyen esa revisión.
- **PENDIENTE — ticket:** capturar el aviso local antes del QR en la versión actual y comprobar su lectura y legibilidad.
- **PENDIENTE — Figma:** obtener acceso o exportaciones y comparar las pantallas según el apartado anterior.

### Evidencias anteriores y actuales

Las cinco capturas existentes pertenecen a la **fase 4 del 16 de octubre**, anterior a `8d74664` y `6dbb9ba`:

| Captura en `docs/evidencias/fase4/` | Contenido y límite |
| --- | --- |
| [phase4-offline-cart.png](evidencias/fase4/phase4-offline-cart.png) | Carrito vertical y cantidad pendiente; muestra el naranja y el texto de retiro anteriores. |
| [phase4-offline-ticket.png](evidencias/fase4/phase4-offline-ticket.png) | Ticket #21 y QR anteriores; todavía muestra la instrucción de presentar el código al retirar. |
| [phase4-keyboard.png](evidencias/fase4/phase4-keyboard.png) | Búsqueda con teclado visible en la interfaz anterior. |
| [phase4-landscape-cart.png](evidencias/fase4/phase4-landscape-cart.png) | Resumen y confirmación en horizontal de la versión anterior. |
| [phase4-landscape-ticket.png](evidencias/fase4/phase4-landscape-ticket.png) | QR horizontal de la versión anterior; no demuestra la cabecera ni el aviso local actuales. |

Se conservan como evidencia histórica, no como prueba de las nuevas correcciones. La ejecución offline y sus límites se registran en [FASES_CORRECCION.md](FASES_CORRECCION.md). **No hay capturas nuevas de fase 5 en el repositorio**: la evidencia actual disponible es la revisión de código y los resultados locales anteriores. Para la entrega faltan capturas de la versión actual con identificación del commit, dispositivo, orientación y escala de texto; el informe Word no se ha generado en esta fase.
