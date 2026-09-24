# CliniMedi-k

CliniMedi-k es una app de Android en Kotlin con Jetpack Compose para el problema que los expedientes clínicos electrónicos: el movimiento del día a día dentro del consultorio. No busca digitalizar el historial médico completo, busca ordenar el flujo de atención.

Cubre el registro de ingreso con cuestionario, la fila de espera por orden de llegada y urgencia con sus distintos estados (esperando, en consulta, atendido, referido), la identificación de pacientes recurrentes por código QR, la agenda de citas, el registro de recetas, referencias y cobros, y un panel de resumen del día. 

La app funciona con dos roles. Recepción tiene acceso completo para registrar pacientes, mover la fila y llevar recetas, referencias y cobros. El médico tiene una vista de solo lectura con su fila, su agenda y el historial de sus pacientes.

En esta versión las pantallas están implementadas a nivel visual con datos de prueba, todavía sin navegación ni lógica.

## Equipo

- Andrés Castro / 25039
- Estuardo García / 25057
- Carlos Pozuelos / 25104

## Estructura

- `data/model` – modelos de datos de la app: médicos, pacientes, visitas, citas, recetas, referencias, cobros y documentos
- `data/fake` – datos de prueba que alimentan las pantallas mientras no hay base de datos
- `ui/theme` – colores, tipografía y formas basados en Material 3
- `ui/components` – componentes reutilizables entre pantallas, como la tarjeta de paciente, los chips de estado y la barra de navegación
- `ui/screens/setup` – configuración inicial del dispositivo: inicio de sesión, selección de rol y médico asignado
- `ui/screens/recepcion` – vistas de recepción: fila de espera, detalle del paciente, búsqueda, agenda, registro de pacientes, códigos QR y resumen del día
- `ui/screens/medico` – vistas de solo lectura del médico: su fila, el historial de sus pacientes y su agenda

Cada pantalla se divide en cuatro archivos: `Screen`, que solo dibuja la interfaz y contiene sus previews; `Route`, que obtiene los datos y se los pasa a la `Screen`; `UiState`, con el estado que muestra la pantalla; e `Intent`, con las acciones que puede realizar el usuario.

## Herramientas

- Kotlin
- Android Studio
- Jetpack Compose
- Material 3

**Para ejecutar este proyecto:**

1. Clonar el repositorio.
2. Cambiar a la rama correspondiente (ej. `git checkout main`).
3. Abrir la carpeta en Android Studio.
4. Esperar a que Gradle sincronice las dependencias.
5. Ejecutar en un emulador o dispositivo físico con Android 8.0 o superior con el botón "Run".
