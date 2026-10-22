package plat.clinimedik.app.ui.screens.recepcion.registro

sealed interface RegistroEvent {
    data object RegistrarNuevo : RegistroEvent
    data object EscanearQr : RegistroEvent
    data object Regresar : RegistroEvent
}