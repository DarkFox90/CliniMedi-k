package plat.clinimedik.app.ui.screens.setup.rol

import plat.clinimedik.app.data.model.RolDispositivo
import plat.clinimedik.app.ui.components.etiqueta

data class OpcionRol(
    val rol: RolDispositivo,
    val titulo: String,
    val descripcion: String
)

data class RolUiState(
    val opciones: List<OpcionRol> = emptyList(),
    val rolSeleccionado: RolDispositivo? = null
) {
    val puedeContinuar: Boolean
        get() = rolSeleccionado != null
}

fun RolDispositivo.aOpcionRol(): OpcionRol = OpcionRol(
    rol = this,
    titulo = etiqueta(),
    descripcion = when (this) {
        RolDispositivo.RECEPCION -> "Registra pacientes y administra la fila, las citas y los cobros"
        RolDispositivo.MEDICO -> "Consulta la fila, la agenda y el historial de tus pacientes, en modo de solo lectura"
    }
)