package plat.clinimedik.app.ui.screens.setup.rol

import plat.clinimedik.app.data.model.RolDispositivo

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
