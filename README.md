# CliniMedi-k

CliniMedi-k es una app de Android en Kotlin con Jetpack Compose para un problema que los expedientes clínicos electrónicos no resuelven: el movimiento del día a día dentro del consultorio. No busca digitalizar el historial médico completo, busca ordenar el flujo de atención.

Cubre el registro de ingreso con cuestionario, la fila de espera por orden de llegada y urgencia con sus distintos estados (esperando, en consulta, atendido, referido), la identificación de pacientes recurrentes por código QR, la agenda de citas, el registro de recetas, referencias y cobros, y un panel de resumen del día.

La app funciona con dos roles. Recepción tiene acceso completo para registrar pacientes, mover la fila y llevar recetas, referencias y cobros. El médico tiene una vista de solo lectura con su fila, su agenda y el historial de sus pacientes.

En esta versión todas las pantallas están conectadas con navegación, cada una maneja su estado con MVI y los datos se guardan en una base de datos local precargada con datos de prueba.

## Equipo

- Andrés Castro / 25039
- Estuardo García / 25057
- Carlos Pozuelos / 25104

## Cómo usar la app

1. Instalar el APK de la carpeta `APK` (`APK/CliniMedi-k.apk`) en un dispositivo o emulador con Android 8.0 o superior.
2. En la pantalla de inicio de sesión, elegir "Continuar con Google" o "Continuar sin cuenta". La segunda opción entra a la app sin conexión y sirve para revisarla sin necesidad de una cuenta de Google.
3. Elegir el rol del dispositivo (Recepción o Médico) y el médico asignado.
4. Para cambiar de rol o de médico, usar el ícono de ajustes de la parte superior de la pantalla de inicio de Recepción o de la fila del Médico.
5. Los datos de prueba se vuelven a cargar cada día, para que la fila, la agenda y los tiempos de espera correspondan al día en que se abre la app. Los cambios hechos durante el día se conservan al cerrar y abrir la app.
6. En "Escanear QR", el botón "Simular lectura" reemplaza la lectura con cámara en esta entrega.

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

## Servicios

| Servicio | Tipo | Rol en la app |
|---|---|---|
| Firebase Authentication con Google | Externo, autenticación | Inicio de sesión con la cuenta de Google. Es la llamada a internet de la app |
| Inicio de sesión de Google mediante Credential Manager | Externo, autenticación | Muestra el selector de cuentas de Google y entrega el token que valida Firebase |
| Base de datos local con datos de prueba (Room) | Propio, desarrollado por el equipo | Guarda médicos, pacientes, visitas, recetas, referencias, cobros, documentos y citas en el teléfono. Se precarga cada día con datos de prueba, porque no existe una API pública con datos de clínicas |
| Configuración del dispositivo (DataStore) | Propio | Guarda el rol y el médico asignado del teléfono |

Servicios previstos para siguientes fases (no están implementados en esta entrega):

| Servicio | Rol previsto |
|---|---|
| Cloud Firestore | Compartir los datos entre el teléfono de Recepción y el del médico en tiempo real |
| Resend | Enviar correos de confirmación y recordatorio de citas |

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
| Kotlinx Serialization JSON | 1.10.0 | Convierte las clases `@Serializable` de los destinos en rutas de navegación |
| Lifecycle ViewModel Compose | 2.11.0 | Crea los `ViewModel` desde las pantallas |
| Lifecycle Runtime y Lifecycle Runtime Compose | 2.11.0 | Observa el `StateFlow` del estado respetando el ciclo de vida (`collectAsStateWithLifecycle`) |
| Room 3 (runtime y compiler) | 3.0.3 | Base de datos local SQLite con tablas, consultas y transacciones |
| KSP | 2.3.11 | Genera el código de Room al compilar |
| DataStore Preferences | 1.2.1 | Guarda el rol y el médico del dispositivo |
| Firebase BOM y Firebase Authentication | 35.1.0 | Autenticación del usuario con Firebase |
| Credential Manager (`credentials` y `credentials-play-services-auth`) | 1.6.0 | Selector de cuentas para el inicio de sesión con Google |
| Google ID (`googleid`) | 1.2.1 | Opción de inicio de sesión con Google para Credential Manager |
| Google Services (plugin de Gradle) | 4.5.0 | Lee `google-services.json` para configurar Firebase |

Librerías previstas para siguientes fases: ZXing core (generar el código QR), Google Code Scanner (leer el código QR) y Retrofit u OkHttp (llamar al servicio de correo).

## Alcance de esta entrega

- El código QR que se muestra tras registrar a un paciente es una imagen de muestra. Lo que sí se genera es el texto del código (`clinimedik:<id del paciente>`), que se comparte con "Enviar por WhatsApp". "Imprimir código" muestra el aviso "Disponible en la siguiente fase".
- "Escanear QR" no usa la cámara: el botón "Simular lectura" devuelve un paciente de los datos de prueba.
- El inicio de sesión con Google requiere que el SHA-1 de la llave de firma esté registrado en Firebase. Si no funciona en un dispositivo, "Continuar sin cuenta" permite revisar toda la app.
- Los datos de prueba no se sincronizan entre dispositivos; cada teléfono usa su propia base de datos local.

## Estructura

- `data/model`: modelos de la app: médicos, pacientes, visitas, citas, recetas, referencias, cobros y documentos.
- `data/fake`: datos de prueba; la carga diaria los inserta en la base de datos y alimentan las previews.
- `data/local`: base de datos Room (tablas, consultas y carga diaria de datos de prueba).
- `data/preferencias`: configuración del dispositivo con DataStore.
- `data/repository`: repositorios que usan los `ViewModel` para leer y guardar datos.
- `dependencyinjection`: creación de la base de datos, DataStore y los repositorios (`AppContainer`).
- `ui/theme`: colores, tipografía y formas basados en Material 3.
- `ui/components`: componentes reutilizables entre pantallas, como la tarjeta de paciente, los chips de estado y las barras de navegación.
- `ui/navigation`: destinos y grafo de navegación.
- `ui/screens/setup`: configuración inicial del dispositivo: arranque, inicio de sesión, selección de rol y médico asignado.
- `ui/screens/recepcion`: vistas de recepción: inicio, fila de espera, detalle del paciente, búsqueda, agenda, nueva cita, registro, cuestionario, código QR, escaneo, paciente encontrado y resumen del día.
- `ui/screens/medico`: vistas de solo lectura del médico: su fila, su agenda, sus pacientes y el historial de cada uno.

## Ramas

El trabajo se organiza en ramas de feature que se integran a `main` mediante Pull Request.

Quinto Entregable:

- `feature/room-datos` – Andrés Castro: base de datos Room, repositorios, DataStore, rutas de navegación, fila de recepción, detalle del paciente y resumen del día.
- `feature/login-configuracion` – Estuardo García: inicio de sesión con Firebase, arranque, rol, médico asignado, inicio de recepción, búsqueda y pantallas del médico.
- `feature/navegacion-registro` – Carlos Pozuelos: navegación y barras inferiores, agenda de recepción, nueva cita, registro, cuestionario, código QR, escaneo y paciente encontrado.

Cuarto Entregable (historial):

- `feature/fila-expediente` – Andrés Castro: base compartida (tema, modelos, datos de prueba, componentes), fila de recepción, detalle del paciente e historial del médico.
- `feature/setup-inicio` – Estuardo García: configuración inicial, inicio de recepción, búsqueda, resumen del día, fila y pacientes del médico.
- `feature/agenda-registro` – Carlos Pozuelos: agenda de recepción y del médico, nueva cita, registro, cuestionario de ingreso y pantallas de código QR.

## APK y ejecución

- El APK de esta entrega está en `APK/CliniMedi-k.apk`.
- Para ejecutar desde el código: clonar el repositorio, abrir la carpeta en Android Studio, esperar la sincronización de Gradle y ejecutar en un emulador o dispositivo con Android 8.0 o superior. El archivo `google-services.json` ya está en `app/`.
