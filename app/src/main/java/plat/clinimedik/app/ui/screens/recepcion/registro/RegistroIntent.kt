package plat.clinimedik.app.ui.screens.recepcion.registro

sealed interface RegistroIntent {
    data object RegistrarNuevo : RegistroIntent
    data object EscanearQr : RegistroIntent
    data object Regresar : RegistroIntent
}