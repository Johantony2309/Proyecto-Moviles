# Demostración offline y persistencia

## Guion para la presentación

1. Abrir la aplicación y entrar con la cuenta local de demostración. En Configuración, activar **Mantener mi sesión** si se quiere mostrar la reapertura sin introducir credenciales.
2. En Catálogo, buscar un producto y guardarlo como favorito.
3. Abrir Favoritos y pulsar **Añadir al carrito**. En Carrito, revisar la cantidad y confirmar el pedido.
4. En Pedidos, seleccionar **Todos**, abrir el pedido recién creado y mostrar su ticket QR. Anotar su número y total.
5. Volver a Favoritos y añadir otro producto al carrito, sin confirmarlo. Esto deja simultáneamente un favorito, un carrito pendiente y un pedido guardado: confirmar un pedido vacía el carrito por diseño.
6. Detener la aplicación desde los ajustes de Android (**Forzar detención**, sin borrar almacenamiento). Activar modo avión y comprobar también que Wi-Fi esté apagado; Android permite mantener Wi-Fi encendido en modo avión.
7. Reabrir la app. Mostrar el favorito, la cantidad pendiente del carrito y el mismo número/total del pedido. Abrir de nuevo su QR. Si no se activó mantener sesión, entrar con la cuenta demo: el acceso también es local.
8. Girar el dispositivo, abrir la búsqueda con el teclado y probar texto grande. Restaurar después los ajustes habituales del teléfono.

El ticket identifica un pedido guardado localmente. No envía el pedido a un servidor ni acredita un pago.

## Prueba automatizada por fases

`OfflineFlowTest` separa preparación, reapertura offline, revisión visual y limpieza. La separación permite detener el proceso entre ejecuciones, en lugar de confundir una recreación de Activity con la muerte real del proceso.

- `prepare`: respalda el carrito existente, crea un producto temporal y recorre Catálogo → Favoritos → Carrito → Confirmar → Pedidos → Ticket. Después deja una nueva unidad pendiente en el carrito.
- `offline`: exige modo avión, espera la sesión persistida y comprueba el favorito, la cantidad del carrito, el identificador del pedido, su token y total, además de las pantallas correspondientes.
- `layout`: exige escala de texto de al menos 1.5, verifica teclado y orientación horizontal, y guarda capturas. Las fases que abren el ticket también decodifican el QR desde la pantalla y comparan su contenido con el pedido persistido.
- `cleanup`: elimina exclusivamente los registros temporales y restaura el carrito respaldado y la preferencia de mantener sesión.

El respaldo se guarda en `files/phase4-fixture.json` de la app, por lo que sobrevive a la detención del proceso. Si una fase se interrumpe, ejecutar `cleanup` antes de repetir `prepare`. No borrar datos ni desinstalar la aplicación con el respaldo pendiente.

La batería general omite este caso cuando no se pasa `phase4`: las fases dependen de ajustes externos del teléfono y se ejecutan explícitamente. Los resultados de la validación se registran en [FASES_CORRECCION.md](FASES_CORRECCION.md).

```powershell
$deviceSerial = 'SERIAL_DEL_TELEFONO'
$runner = 'com.example.proyectopanaderia.test/androidx.test.runner.AndroidJUnitRunner'
$testClass = 'com.example.proyectopanaderia.OfflineFlowTest'
adb -s $deviceSerial shell am instrument -w -r -e class $testClass -e phase4 prepare $runner
adb -s $deviceSerial shell am force-stop com.example.proyectopanaderia
adb -s $deviceSerial shell cmd connectivity airplane-mode enable
adb -s $deviceSerial shell svc wifi disable
adb -s $deviceSerial shell am instrument -w -r -e class $testClass -e phase4 offline $runner
adb -s $deviceSerial shell settings put system font_scale 1.5
adb -s $deviceSerial shell am instrument -w -r -e class $testClass -e phase4 layout $runner
adb -s $deviceSerial shell am instrument -w -r -e class $testClass -e phase4 cleanup $runner
```

Antes de ejecutar el guion, registrar modo avión, Wi-Fi, escala de fuente, rotación y mantenimiento de pantalla encendida. Restaurar sus valores originales al terminar, incluso si una prueba falla. Utilizar siempre el serial USB para evitar ejecutar accidentalmente en un emulador abierto.
