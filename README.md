# CliniMedi-k

CliniMedi-k es una app de Android en Kotlin con Jetpack Compose para un problema que los expedientes clínicos electrónicos no resuelven: el movimiento del día a día dentro del consultorio. No busca digitalizar el historial médico completo, busca ordenar el flujo de atención.

Cubre el registro de ingreso con cuestionario, la fila de espera por orden de llegada y urgencia con sus distintos estados (esperando, en consulta, atendido, referido), la identificación de pacientes recurrentes por código QR, la agenda de citas con confirmación por correo, el registro de recetas, referencias, cobros y documentos, y un panel de resumen del día.

La app funciona con dos roles. Recepción tiene acceso completo para registrar pacientes, mover la fila y llevar recetas, referencias, cobros y documentos. El médico tiene una vista de solo lectura con su fila, su agenda y el historial de sus pacientes.

Esta es la entrega final del curso Programación de Plataformas Móviles (UVG).

## Equipo

- Andrés Castro / 25039
- Estuardo García / 25057
- Carlos Pozuelos / 25104

## Descarga

La app se distribuye con Firebase App Distribution: https://appdistribution.firebase.dev/i/ea2bbff1986eb256

Al abrir el enlace, escribe tu correo y sigue las instrucciones para instalar la app. El mismo APK firmado está en `APK/CliniMedi-k.apk` para instalarlo directamente.

## Cómo usar la app

1. Instalar la app en un dispositivo o emulador con Android 8.0 o superior.
2. En la pantalla de inicio de sesión, elegir "Continuar con Google" o "Continuar sin cuenta". La segunda opción no necesita internet ni una cuenta de Google y permite revisar toda la app.
3. Elegir el rol del dispositivo (Recepción o Médico) y el médico asignado.
4. El menú de tres puntos de la parte superior (Inicio de Recepción y "Mis pacientes" del Médico) tiene "Cambiar configuración" y "Cerrar sesión". Al cerrar sesión, los datos de la clínica se conservan.
5. Los datos de prueba se cargan una sola vez, al abrir la app por primera vez, con fechas calculadas desde ese día. Para ver la fila y la agenda de prueba de hoy, la app debe instalarse el mismo día.
6. Para leer un código QR se usa la cámara mediante Google Play services, que descarga su lector la primera vez y necesita internet en ese momento. En emuladores sin Google Play services, el botón "Simular lectura" abre un paciente de prueba.
7. Para probar los correos, crear una cita con un correo real. El paciente recibe un correo con el botón "Confirmar mi cita", y la confirmación aparece en la agenda al volver a abrirla.

## Funcionalidades

| # | Funcionalidad | Cómo se cumple |
|---|---|---|
| 1 | Cuestionario de ingreso | Datos personales, antecedentes y seguro médico, con fechas y horas elegidas en calendario y selector |
| 2 | Búsqueda ágil | Búsqueda por nombre sin importar tildes y lista de todos los pacientes |
| 3 | Fila en tiempo real con prioridad | Orden por llegada y urgencia; en la fila de Recepción los tiempos de espera se actualizan cada minuto |
| 4 | Trazabilidad de estados | Esperando, en consulta, atendido y referido |
| 5 | Registro de referencias | Se registran al finalizar la consulta y se ven en el detalle y en el historial |
| 6 | Generación de código QR | Código real con ZXing; se puede compartir como imagen e imprimir |
| 7 | Lectura ágil de paciente | Lectura con la cámara mediante Google Code Scanner |
| 8 | Prevención de conflictos | Las citas no pueden cruzarse en un margen de 30 minutos; se pueden reprogramar, cancelar y registrar como llegada |
| 9 | Recordatorios de confirmación | Correo con botón para confirmar, recordatorio la víspera y aviso a la clínica de las citas sin confirmar |
| 10 | Control de recetas | Recetas con indicaciones por visita, que se pueden compartir |
| 11 | Métricas de recurrencia | Pacientes nuevos y recurrentes en el resumen del día |
| 12 | Panel de resumen diario | Tiempo promedio, afluencia por hora, lo cobrado en caja y lo que se cobra a aseguradoras; se puede enviar por correo |
| 13 | Registro de cobros | Cobro particular o con seguro (lo que paga el paciente y lo que paga el seguro) |
| 14 | Digitalización de documentos | Fotos tomadas con la cámara del sistema y visor de documentos en el detalle del paciente |
| 15 | Operación sin conexión | Todos los datos viven en el teléfono; los correos esperan a que vuelva la conexión y se reintentan solos |

## Arquitectura

La app sigue el patrón MVI. Cada pantalla tiene su carpeta con cinco archivos:

- `Screen`: dibuja la interfaz a partir del estado y envía las acciones del usuario. Contiene las previews.
- `UiState`: el State de MVI. `data class` con todo lo que la pantalla muestra, incluido el estado de carga y los errores.
- `Event`: `sealed interface` con las acciones que puede realizar el usuario.
- `ViewModel`: recibe los `Event`, ejecuta la lógica, consulta los repositorios y expone el estado con `StateFlow`. Las acciones de una sola vez, como navegar o mostrar un mensaje, salen por un canal de efectos.
- `Route`: conecta la pantalla con su `ViewModel` y con la navegación.

Flujo: la pantalla envía un `Event`, el `ViewModel` lo procesa y actualiza el `UiState`, y la pantalla se vuelve a dibujar con el nuevo estado.

La navegación es type-safe: cada destino es una clase `@Serializable` en `ui/navigation/Rutas.kt` y entre pantallas solo se envía el identificador necesario (por ejemplo, `Detalle(visitaId)`). La app tiene tres grafos: configuración inicial, Recepción y Médico. Las barras inferiores conservan el estado de cada pestaña.

Los `ViewModel` no acceden a la base de datos directamente: usan repositorios (`data/repository`) que se crean en un contenedor de dependencias propio (`dependencyinjection/AppContainer`).

### Manejo de errores

Las pantallas que guardan datos usan `try/catch` y las que cargan información usan `.catch`, con un mensaje y el botón "Reintentar"; un error no cierra la app. Mientras se guarda, el botón se bloquea para evitar el doble toque. De los 18 `ViewModel` que leen o guardan datos, 16 tienen este manejo; los otros tres solo navegan entre pantallas y no lo necesitan. Quedan pendientes Historial del Médico y Pacientes del Médico, que solo leen de la base local, no dependen de internet ni guardan nada, por lo que el riesgo de error es muy bajo.

## Servicios

| Servicio | Tipo | Rol en la app |
|---|---|---|
| Firebase Authentication con Google y Credential Manager | Externo | Inicio de sesión con la cuenta de Google |
| Resend | Externo | Envía los correos de confirmación, recordatorio, avisos y resumen del día desde el dominio `clinimedik.dev` |
| Cloudflare Worker con Workers KV (`correos-worker/`) | Externo, desarrollado por el equipo | Guarda la llave de Resend fuera del APK, guarda las citas y sus confirmaciones, muestra la página "Confirmar mi cita" y corre dos tareas diarias: el recordatorio a las 08:00 y el aviso de citas sin confirmar a las 18:00 |
| Google Code Scanner (Google Play services) | Externo | Lectura del código QR con la cámara |
| Firebase App Distribution | Externo | Distribución del APK firmado |
| Base de datos local (Room) | Local | Guarda médicos, pacientes, visitas, citas, recetas, referencias, cobros y documentos. Los datos de prueba se cargan una sola vez, porque no existe una API pública con datos de clínicas |
| Configuración del dispositivo (DataStore) | Local | Guarda el rol y el médico asignado del teléfono |
| WorkManager | Local | Envía las citas al Worker cuando hay internet y reintenta si falla |

Todos los servicios se usan en planes gratuitos que no piden tarjeta. Los correos solo llevan nombre, fecha, hora y médico; nunca diagnósticos ni motivos de consulta.

### Sincronización entre teléfonos (fuera de esta entrega)

Planeamos que Recepción y el médico usaran teléfonos distintos y que los datos pasaran de uno a otro con Cloud Firestore. Por tiempo decidimos no incluirla: una sincronización a medias puede duplicar o perder datos de pacientes, y preferimos entregar una app estable con todas sus funcionalidades.

La app ya está preparada: después de guardar en Room, cada repositorio avisa el cambio a la interfaz `Sincronizador`. Hoy la implementa `SincronizadorLocal`, que no hace nada. Para agregar Firestore basta con crear otra clase que implemente esa interfaz y cambiarla en `AppContainer`; las pantallas y los `ViewModel` no cambian.

Para usar los dos roles en un mismo teléfono: registrar en Recepción, cerrar sesión y entrar como Médico con el mismo médico asignado.

### Otros pendientes

- "Recientes" en Buscar muestra pacientes de prueba fijos.
- Los datos de un paciente registrado no se pueden editar desde la app.

## Librerías

Versiones tomadas de `gradle/libs.versions.toml`:

| Librería | Versión | Propósito |
|---|---|---|
| Kotlin | 2.4.20 | Lenguaje de la app |
| Jetpack Compose (BOM) y Material 3 | BOM 2026.02.01 | Interfaz de usuario declarativa con componentes de Material Design 3 |
| Material Icons Core | Según el BOM | Íconos de la interfaz |
| Activity Compose | 1.13.0 | Muestra la interfaz de Compose dentro de la actividad |
| Core KTX | 1.19.0 | Extensiones de Kotlin para las APIs de Android |
| Navigation Compose | 2.10.2 | Navegación entre pantallas con destinos type-safe |
| Kotlinx Serialization JSON | 1.10.0 | Rutas de navegación y datos que se envían al Worker de correos |
| Lifecycle ViewModel Compose | 2.11.0 | Crea los `ViewModel` desde las pantallas |
| Lifecycle Runtime y Lifecycle Runtime Compose | 2.11.0 | Observa el `StateFlow` del estado respetando el ciclo de vida (`collectAsStateWithLifecycle`) |
| Room 3 (runtime y compiler) | 3.0.3 | Base de datos local SQLite con tablas, consultas y transacciones |
| KSP | 2.3.11 | Genera el código de Room al compilar |
| DataStore Preferences | 1.2.1 | Guarda el rol y el médico del dispositivo |
| Firebase BOM y Firebase Authentication | 35.1.0 | Autenticación del usuario con Firebase |
| Credential Manager (`credentials` y `credentials-play-services-auth`) | 1.6.0 | Selector de cuentas para el inicio de sesión con Google y cierre de sesión |
| Google ID (`googleid`) | 1.2.1 | Opción de inicio de sesión con Google para Credential Manager |
| Google Services (plugin de Gradle) | 4.5.0 | Lee `google-services.json` para configurar Firebase |
| OkHttp | 5.4.0 | Llamadas HTTPS al Worker de correos |
| WorkManager | 2.12.0 | Trabajos en segundo plano que esperan a que haya internet |
| ZXing Core | 3.5.4 | Genera la imagen del código QR |
| Google Code Scanner | 16.1.0 | Lee el código QR con la cámara |
| AndroidX Print | 1.1.0 | Imprime el código QR |

## Estructura

- `data/model`: modelos de la app: médicos, pacientes, visitas, citas, recetas, referencias, cobros y documentos.
- `data/fake`: datos de prueba; la carga inicial los inserta en la base de datos y alimentan las previews.
- `data/local`: base de datos Room (tablas, consultas y carga inicial de datos de prueba).
- `data/preferencias`: configuración del dispositivo con DataStore.
- `data/repository`: repositorios que usan los `ViewModel` para leer y guardar datos, incluido el cierre de sesión.
- `data/remote`: llamadas al Worker de correos.
- `data/trabajos`: trabajos de WorkManager para enviar citas, consultar confirmaciones, avisar urgencias y enviar el resumen.
- `data/sincronizacion`: interfaz `Sincronizador`, preparada para compartir datos entre teléfonos.
- `dependencyinjection`: creación de la base de datos, DataStore, los servicios y los repositorios (`AppContainer`).
- `ui/theme`: colores, tipografía y formas basados en Material 3, con modo claro y oscuro.
- `ui/components`: componentes reutilizables entre pantallas, como la tarjeta de paciente, los chips de estado, los selectores de fecha y hora, el menú de sesión y el estado de error.
- `ui/navigation`: destinos y grafo de navegación.
- `ui/screens/setup`: configuración inicial del dispositivo: arranque, inicio de sesión, selección de rol y médico asignado.
- `ui/screens/recepcion`: vistas de recepción: inicio, fila de espera, detalle del paciente, búsqueda, agenda, nueva cita, registro, cuestionario, código QR, escaneo, paciente encontrado, visor de documentos y resumen del día.
- `ui/screens/medico`: vistas de solo lectura del médico: su fila, su agenda, sus pacientes y el historial de cada uno.
- `correos-worker`: código del Cloudflare Worker que envía los correos con Resend.

## Ramas

El trabajo se organiza en ramas de feature que se integran a `main` mediante Pull Request.

Entrega final:

- `feature/final-datos-correos` – Andrés Castro: carga inicial única y versión 2 de la base, consulta y detalle completo, cobro con seguro y resumen por caja y aseguradoras, selectores de fecha y hora, cambio de día en las agendas, fila de días anteriores y correos con Resend y Cloudflare Worker.
- `feature/final-qr-agenda` – Carlos Pozuelos: código QR real (generar, compartir, imprimir y leer), documentos con cámara y visor, panel de la cita en la agenda, seguro médico en el cuestionario y en paciente encontrado, mensajes con Snackbar, ícono adaptativo y modo oscuro sin destello al abrir.
- `feature/final-firebase` – Estuardo García: cierre de sesión con menú de tres puntos, firma de release y Firebase App Distribution, y manejo de errores en Selección de médico, Inicio de Recepción y Fila del Médico.

Quinto Entregable:

- `feature/room-datos` – Andrés Castro: base de datos Room, repositorios, DataStore, rutas de navegación, fila de recepción, detalle del paciente y resumen del día.
- `feature/login-configuracion` – Estuardo García: inicio de sesión con Firebase, arranque, rol, médico asignado, inicio de recepción, búsqueda y pantallas del médico.
- `feature/navegacion-registro` – Carlos Pozuelos: navegación y barras inferiores, agenda de recepción, nueva cita, registro, cuestionario, código QR, escaneo y paciente encontrado.

Cuarto Entregable:

- `feature/fila-expediente` – Andrés Castro: base compartida (tema, modelos, datos de prueba, componentes), fila de recepción, detalle del paciente e historial del médico.
- `feature/setup-inicio` – Estuardo García: configuración inicial, inicio de recepción, búsqueda, resumen del día, fila y pacientes del médico.
- `feature/agenda-registro` – Carlos Pozuelos: agenda de recepción y del médico, nueva cita, registro, cuestionario de ingreso y pantallas de código QR.

## APK y ejecución

- El APK firmado de la entrega final está en `APK/CliniMedi-k.apk` y en Firebase App Distribution.
- Para ejecutar desde el código: clonar el repositorio, abrir la carpeta en Android Studio, esperar la sincronización de Gradle y ejecutar en un emulador o dispositivo con Android 8.0 o superior. El archivo `google-services.json` ya está en `app/`.
- La dirección y la clave del Worker de correos van en `local.properties` (`CORREOS_URL` y `CORREOS_CLAVE`), que no se sube al repositorio. Sin ellas la app funciona igual, pero no envía correos.
- La llave de firma, sus contraseñas y la llave de Resend tampoco están en el repositorio.