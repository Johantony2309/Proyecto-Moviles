# Arquitectura y estado de Pastelería Andony

La app incluye acceso de demostración, configuración, catálogo, favoritos, carrito, pedidos y ticket QR. Funciona localmente con Room y Preferences DataStore. La administración visual completa de maestros y otras acciones CRUD quedan fuera de estas correcciones.

## Capas y dependencias

`Route → ViewModel → contrato de repositorio → implementación → DAO/Room o DataStore`.

- `di/DataModule.kt`: módulos Hilt que proporcionan una instancia de Room por proceso, DataStore y repositorios. Los ViewModels se gestionan mediante Hilt e inyección por constructor.
- `app/BakeryApp.kt`: NavHost, control de sesión, selección de pestañas y efectos de navegación/avisos.
- `presentation/<funcionalidad>/*Route.kt`: obtiene el ViewModel, recoge su estado con `collectAsStateWithLifecycle()` y conecta sus acciones al Content.
- `presentation/<funcionalidad>/*Screen.kt`: contiene las funciones `*Content`, que reciben estado y callbacks, sin acceder a repositorios ni obtener ViewModels.
- `presentation/common/`: estados excluyentes, mecánica compartida de carga/operaciones y consumo de eventos.
- `domain/`: modelos, contratos, validación monetaria y cuenta demo.
- `data/`: entidades, DAO, relaciones, repositorios Room y preferencias.

## Responsabilidad y alcance de cada ViewModel

| ViewModel | Estado y acciones | Alcance |
| --- | --- | --- |
| LoginViewModel | Carga de preferencias, sesión, errores de credenciales y cambios de preferencias | Activity; compartido por LoginRoute, SettingsRoute y coordinador de sesión |
| CatalogViewModel | Productos filtrados, categorías, favoritos, búsqueda, adición al carrito y ejemplos | Destino Catálogo |
| FavoritesViewModel | Productos favoritos, quitar favorito y añadir al carrito | Destino Favoritos |
| CartViewModel | Líneas del carrito, cantidades y confirmación | Destino Carrito |
| OrdersViewModel | Pedidos, filtro, detalle expandido, archivar y recuperar | Destino Pedidos |
| TicketViewModel | Pedido consultado por cliente e identificador | Destino Ticket |
| ShopViewModel | Solo cantidades de favoritos y carrito para la barra inferior | Grafo autenticado SHOP |

`ShopRoute`, definido junto al grafo, obtiene el ViewModel de contadores. `ShopContent` dibuja el marco y la barra inferior; cada destino obtiene su propio ViewModel. Las listas se sincronizan mediante los flujos de Room, sin pasar un ViewModel de negocio compartido entre pantallas.

Configuración y acceso comparten deliberadamente LoginViewModel porque las preferencias afectan a la sesión actual. SettingsRoute obtiene esa misma instancia mediante su propietario Activity y la fábrica del contenedor.

## Estado de pantalla

Cada ViewModel expone **un solo StateFlow público de solo lectura**, llamado `state`; el MutableStateFlow es privado. Los ViewModels de negocio heredan esta mecánica de `ScreenViewModel<T>`.

`ScreenState<T>` tiene tres variantes:

1. `Loading`: lectura inicial o reintento en curso.
2. `Content(data, busy)`: información disponible; `busy` bloquea otra escritura simultánea en ese ViewModel.
3. `Error(message)`: fallo de lectura, con botón Reintentar.

Una lista vacía es contenido válido. Un fallo al escribir conserva el contenido y emite un aviso; no convierte una pantalla legible en una pantalla de error de carga. Al fallar la lectura se utiliza Error; no se combinan banderas contradictorias de carga y error.

Login usa `LoginStatus.LOADING/READY/ERROR` dentro de su único LoginState. Las propiedades derivadas `loading/loadError` permiten integrar el splash, sin poder asignar ambas a la vez. El error de credenciales pertenece al formulario y permanece hasta editarlo o reintentar.

## Eventos y restauración

- Los avisos y `OrderCreated(orderId)` salen por un `Channel(BUFFERED)` privado expuesto como Flow mediante `receiveAsFlow()`.
- `ObserveEvents` tiene un consumidor por productor y utiliza `repeatOnLifecycle(RESUMED)`. El callback actualizado se conserva mediante `rememberUpdatedState`.
- El coordinador mantiene el SnackbarHost fuera de cada destino, por lo que el aviso de confirmación puede mostrarse al abrir Pedidos.
- Los eventos consumidos no se reproducen a un nuevo suscriptor. El canal no es almacenamiento persistente ni promete entrega exactamente una vez ante muerte del proceso; los pedidos siguen en Room y la sesión sigue siendo estado, aunque un aviso transitorio se pierda.
- Catálogo conserva búsqueda y categoría mediante SavedStateHandle. Pedidos conserva filtro y pedido expandido del mismo modo. Las fábricas utilizan `createSavedStateHandle()`.
- LoginRoute conserva correo y validación local; la contraseña se mantiene únicamente en memoria y no se guarda en un Bundle ni DataStore.
- NavController conserva el destino y el argumento Long de Ticket. TicketViewModel vuelve a observar el pedido en Room por cliente e ID.
- La barra inferior guarda/restaura sus pilas. Cerrar sesión elimina el grafo autenticado y las pilas guardadas; un nuevo acceso comienza con filtros nuevos.

## Navegación

`AppDestinations` define acceso, configuración, catálogo, favoritos, carrito, pedidos y `ticket/{orderId}`. No se usa `section`, `settingsOpen` ni `ticketOrder` como mecanismo de navegación.

Atrás desde Ticket vuelve a Pedidos; desde Configuración vuelve al destino anterior. Desde una pestaña secundaria vuelve a Catálogo. El ticket sigue siendo un detalle de Pedidos para la selección de la barra inferior.

## Tablas

| Tabla | Tipo | Campos principales |
| --- | --- | --- |
| `categories` | Maestra | `id`, `name` único, `position` |
| `products` | Maestra | `id`, `categoryId`, `name`, `description`, `priceCents`, `imageKey`, `available` |
| `customers` | Maestra | `id`, `name`, `email` único |
| `orders` | Transaccional | `id`, `customerId`, `createdAt`, `status`, `ticketToken` único |
| `order_items` | Detalle transaccional | `id`, `orderId`, `productId` opcional, `productName`, `unitPriceCents`, `quantity` |
| `cart_items` | Apoyo | Clave compuesta `customerId` + `productId`, `quantity` |
| `favorites` | Apoyo | Clave compuesta `customerId` + `productId`, `createdAt` |

Se superan las dos tablas maestras mínimas. Clientes conserva el propietario de pedidos, carrito y favoritos; su formulario no aparece en Figma y se implementará en una etapa posterior. Al abrir el catálogo, la cuenta demo resuelve o crea una única fila de cliente por correo antes de observar carrito y favoritos.

Relaciones: categoría 1:N productos; cliente 1:N pedidos; pedido 1:N detalles; cliente N:M productos mediante favoritos y carrito. Cada detalle puede conservar una referencia opcional al producto original.

## Reglas de datos y CRUD

- Importes en centavos de USD (`Long`), nunca `Float` o `Double`. El ejemplo de Figma, dos croissants de $1,50 y una torta de $8,00, suma 1100 centavos. El total se deriva de los detalles y no se duplica en otra columna.
- El detalle conserva nombre y precio al crear el pedido; editar el catálogo no cambia el histórico. Eliminar un producto mantiene esos datos y deja su referencia en `null`.
- Eliminar un pedido elimina sus detalles. Eliminar productos o clientes elimina su carrito y favoritos. No se elimina una categoría con productos ni un cliente con pedidos: las claves foráneas lo impiden.
- Cantidades entre 1 y 999; 0 significa quitar una línea. Un pedido no puede quedar vacío: para quitar el último producto se elimina el pedido completo.
- Crear pedido, copiar los detalles y vaciar el carrito ocurre en una única transacción. Una falla revierte toda la operación.
- Actualizar cantidades conserva el precio histórico. Para editar un pedido archivado primero se recupera a Guardados. Archivar es reversible y no equivale a eliminar.
- Repetir añade productos al carrito existente utilizando el catálogo actual. Si algún producto ya no existe, está deshabilitado o supera el límite de cantidad, se revierte la operación completa; no se produce una repetición parcial silenciosa.
- Catálogo y clientes exponen crear, consultar, editar y eliminar. Pedidos expone crear desde carrito, consultar lista/detalle, modificar cantidades, archivar/recuperar, eliminar y repetir. La UI conecta catálogo, favoritos, carrito, creación de pedidos, consulta, archivar/recuperar y ticket; la administración de maestros y editar/eliminar/repetir pedidos siguen pendientes.
- El QR se representa con `panaderia:pedido:v1:<token>`. El token identifica un pedido local, no certifica pagos ni envía datos. Se genera localmente con ZXing.
- Las imágenes se referencian por una clave estable local, no por URLs temporales de Figma ni IDs numéricos de recursos persistidos. `ProductPicture` muestra las imágenes locales de croissant, cake, bread y roll; el logo se utiliza únicamente como alternativa cuando la clave no tiene imagen conocida. Las imágenes por producto están implementadas. La equivalencia visual con el prototipo requiere una comparación aparte.
- Una sola instancia de Room por proceso, proporcionada por Hilt con alcance `@Singleton`; operaciones suspendidas y lecturas `Flow`. Sin `allowMainThreadQueries` y sin borrado destructivo al migrar. Un cambio de esquema exige subir la versión y escribir su migración.
- Tanto DataStore como Room se excluyen de copia a la nube y transferencia automática para mantener esta entrega únicamente en el dispositivo.


## Verificación y trazabilidad

Consultar [FASES_CORRECCION.md](FASES_CORRECCION.md) para los problemas originales, cambios y evidencias de las fases 1, 2 y 3. Las correcciones no cambian el esquema de Room ni requieren migración o borrado de datos.

Las pruebas automatizadas no sustituyen una evaluación DCU con usuarios. Esa evaluación, junto con accesibilidad y formularios de administración, sigue siendo trabajo posterior.

## Referencias

- [Estado de la UI](https://developer.android.com/topic/architecture/ui-layer/state-production).
- [Eventos de la UI y límites de entrega](https://developer.android.com/topic/architecture/ui-layer/events).
- [SavedStateHandle](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate).
