package plat.clinimedik.app.ui.screens.setup.rol

import plat.clinimedik.app.data.model.RolDispositivo

sealed interface RolEvent {
    data class SeleccionarRol(val rol: RolDispositivo) : RolEvent
    data object Continuar : RolEvent
}
