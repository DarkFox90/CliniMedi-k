package plat.clinimedik.app.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SetupGraph

@Serializable
data object RecepcionGraph

@Serializable
data object MedicoGraph

@Serializable
data object Arranque

@Serializable
data object Login

@Serializable
data object Rol

@Serializable
data class SeleccionMedico(val rol: String)

@Serializable
data object Inicio

@Serializable
data object Fila

@Serializable
data object AgendaRecepcion

@Serializable
data object Buscar

@Serializable
data class Detalle(val visitaId: String)

@Serializable
data object NuevaCita

@Serializable
data object Registro

@Serializable
data object Cuestionario

@Serializable
data class CodigoQr(val pacienteId: String)

@Serializable
data object Escaneo

@Serializable
data class Resultado(val pacienteId: String)

@Serializable
data object Resumen

@Serializable
data object FilaMedico

@Serializable
data object AgendaMedico

@Serializable
data object PacientesMedico

@Serializable
data class HistorialMedico(val pacienteId: String)
