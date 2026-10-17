# Fase 5 — Pulido de la interfaz

## Cambios

- Nombre visible y accesible: **Pastelería Andony**, centralizado en `app_name` para el lanzador, la cabecera de acceso, el logo de la tienda y el ticket. El logo junto al nombre en acceso es decorativo para evitar una lectura duplicada.
- Cabecera de tienda según altura de ventana: menos de 480 dp, cabecera 80 dp y logo 48 dp; entre 480 y 699 dp, 104/64 dp; desde 700 dp, 120/88 dp. Sustituye los 148/120 dp originales y conserva los espacios seguros del Scaffold.
- Controles activos: naranja `#B93600` con blanco. El logo original conserva su imagen y color.
- Ticket: aviso antes del QR de que el pedido solo se guarda en el dispositivo y no se envía al negocio ni confirma compra o reserva. Se elimina la instrucción que sugería retirar un pedido real. Carrito aclara lo mismo antes de confirmar.
- Imágenes locales por producto: ya implementadas y fuera de pendientes. Se corrigen las descripciones antiguas de README y arquitectura.

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

Compilación y revisión en teléfono USB en curso. Los resultados de la fase 4 no sustituyen la comprobación de estos cambios.
