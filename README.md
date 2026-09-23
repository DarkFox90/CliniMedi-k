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

- `main` – versión integrada del proyecto con el tema, los modelos de datos, los datos de prueba y las pantallas terminadas
- `andres` – configuración inicial del dispositivo y vistas del médico
- `estuardo` – inicio de recepción, fila, detalle del paciente, búsqueda y resumen del día
- `carlos` – agenda, nueva cita, registro de pacientes y pantallas de código QR

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
