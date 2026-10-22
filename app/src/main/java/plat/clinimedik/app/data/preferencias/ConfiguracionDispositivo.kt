package plat.clinimedik.app.data.preferencias

import plat.clinimedik.app.data.model.RolDispositivo

data class ConfiguracionDispositivo(
    val rol: RolDispositivo,
    val medicoId: String
)
