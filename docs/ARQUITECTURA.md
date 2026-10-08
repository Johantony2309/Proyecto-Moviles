# Arquitectura y modelo local de Panadería

## Alcance de esta etapa

Esta entrega llega únicamente hasta la pantalla 02 de Figma: Catálogo, después del inicio de sesión. Incluye búsqueda, filtros, lectura de productos, favoritos y adición al carrito con persistencia Room y MVVM. Las pantallas de favoritos, carrito, ticket, historial y administración se dejan para otra etapa. Se conserva la base de datos y sus repositorios preparados; el CRUD completo accesible al usuario sigue pendiente.

El acceso y las preferencias mantienen el archivo `panaderia_local` y las mismas claves; no se pierden los ajustes existentes. La base Room `panaderia.db` es independiente y se abre al usar un repositorio de negocio. No se agregan datos ficticios al historial ni se reinserta automáticamente información que el usuario elimine.

## Lectura de las interfaces de Figma

Referencia: [Panadería — Interfaces](https://www.figma.com/design/cBI3iCNtyTH0wDwsSIY8cW/Panader%C3%ADa-%E2%80%94-Interfaces?node-id=0-1).

La revisión se hizo visualmente en el navegador porque el conector continúa limitado por su cuota. Las dos imágenes inferiores se consideran interfaces completas: **Tus favoritos** y **Tus pedidos**. No son recursos decorativos. No se ha comprobado el cableado de todas las interacciones del prototipo.

| Interfaz observada | Información y acciones | Soporte local |
| --- | --- | --- |
| Acceso | Correo, contraseña y acceso. El prototipo también muestra registro, recuperación y proveedores sociales. | Demo local ya implementada; registro y proveedores no se incluyen en esta etapa. |
| Catálogo | Buscar; filtrar Todo/Panes/Pasteles/Dulces; imagen, nombre, descripción, precio; favorito y añadir. | Categorías, productos, favoritos y carrito. “Todo” es un filtro, no una categoría persistida. |
| Tu carrito | Cantidades, quitar producto, subtotal, retiro sin costo y crear pedido. | Carrito por cliente y creación atómica del pedido con sus detalles. |
| Ticket QR | Código del pedido, QR, resumen y total. Aclara que no es comprobante de pago y no se ha enviado a la panadería. | Token único local y detalles históricos. No se requiere tabla de pagos ni servidor. |
| Tus favoritos | Productos guardados; quitar favorito; añadir al carrito. | Relación única cliente–producto. |
| Tus pedidos | Filtros Todos/Guardados/Archivados, fecha, importe, ver pedido y QR, repetir. | Pedidos con estado SAVED/ARCHIVED y sus detalles. |

El diseño actual no muestra formularios de administración de maestros ni edición/eliminación completa de pedidos. Para cumplir el CRUD académico, la próxima fase deberá añadir esos controles y sus confirmaciones, manteniendo el estilo de Figma.

## Carpetas

Raíz Kotlin: `app/src/main/java/com/example/proyectopanaderia/`.

```text
MainActivity.kt                 Punto de entrada Android y splash
app/                           Composición, Application y contenedor de dependencias
presentation/
  login/                       Pantalla, estado y ViewModel de acceso
  settings/                    Pantalla de configuración
  components/                  Componentes visuales reutilizables
  catalog/                     Pantalla de catálogo, búsqueda y filtros
  shop/                        Contenedor y ViewModel del catálogo autenticado
ui/theme/                      Colores y tipografía compartidos
domain/
  auth/                        Cuenta pública de demostración
  model/                       Modelos Kotlin sin dependencias de Room ni Compose
  repository/                  Contratos de preferencias, catálogo, clientes, compras y pedidos
data/
  local/
    preferences/               Implementación de Preferences DataStore
    database/
      entity/                  Entidades y claves foráneas
      dao/                     Consultas y operaciones SQL
      relation/                Lecturas de agregados con @Relation y @Transaction
      BakeryDatabase.kt        Base de datos Room, versión 1
      DatabaseConverters.kt    Conversión del estado de pedido
  repository/                  Implementaciones, mapeos y transacciones
```

`app/schemas/` contiene el esquema exportado de Room, que debe versionarse. `docs/` guarda decisiones, trazabilidad de pantallas y trabajo DCU. Las carpetas de nuevas pantallas se crean cuando contengan código real, evitando paquetes vacíos o pantallas de relleno.

Flujo de dependencia: **pantalla → ViewModel → contrato del repositorio → implementación → DAO → Room**. `AppContainer` conecta esas piezas mediante inyección por constructor. Las pantallas no abren bases de datos; los ViewModels no reciben DAO ni Context. DataStore sigue el mismo esquema mediante `PreferencesRepository`.

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
- Catálogo y clientes exponen crear, consultar, editar y eliminar. Pedidos expone crear desde carrito, consultar lista/detalle, modificar cantidades, archivar/recuperar, eliminar y repetir. La conexión de estas acciones a controles visuales queda pendiente.
- El QR se representará con `panaderia:pedido:v1:<token>`. El token identifica un pedido local, no certifica pagos ni envía datos. El renderizador QR se incorporará al construir esa pantalla.
- Las imágenes se referencian por una clave estable local, no por URLs temporales de Figma ni IDs numéricos de recursos persistidos. Las tarjetas utilizan por ahora el símbolo local de panadería; las ilustraciones exactas siguen pendientes por la cuota de Figma.
- Una sola instancia de Room por proceso, creada de forma diferida por `AppContainer`; operaciones suspendidas y lecturas `Flow`. Sin `allowMainThreadQueries` y sin borrado destructivo al migrar. Un cambio de esquema exige subir la versión y escribir su migración.
- Tanto DataStore como Room se excluyen de copia a la nube y transferencia automática para mantener esta entrega únicamente en el dispositivo.

## Siguientes pasos del proceso DCU

1. Validar con usuarios las tareas de buscar productos, guardar favoritos, preparar un pedido, recuperar el QR y repetir una compra.
2. Revisar dónde se ubicarán la administración de maestros y las acciones de editar/eliminar que exige la rúbrica y no aparecen en el prototipo.
3. Continuar, cuando se autorice la próxima etapa, con maestros; favoritos/carrito; creación/ticket; historial y CRUD. El catálogo ya está conectado.
4. Evaluar comprensión de “Guardado local”, “Archivado” y “no es comprobante de pago”, además de recuperación ante errores y confirmación de eliminación.
5. Registrar problemas, cambios y nueva evaluación. Estos pasos están planificados; no se declara que ya se realizaron pruebas con usuarios.

## Referencias técnicas

- [Room y configuración de su compilador](https://developer.android.com/jetpack/androidx/releases/room).
- [KSP con las herramientas de Android](https://developer.android.com/build/migrate-to-ksp).
