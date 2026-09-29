package plat.clinimedik.app.ui.screens.setup.rol

import plat.clinimedik.app.data.model.RolDispositivo

sealed interface RolIntent {
    data class SeleccionarRol(val rol: RolDispositivo) : RolIntent
    data object Continuar : RolIntent
}
