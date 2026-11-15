package plat.clinimedik.app.ui.screens.recepcion.escaneo

sealed interface EscaneoEvent {
    data object Regresar : EscaneoEvent
    data object EscanearConCamara : EscaneoEvent
    data object SimularLectura : EscaneoEvent
    data class CodigoLeido(val texto: String) : EscaneoEvent
    data object LecturaCancelada : EscaneoEvent
    data object LectorNoDisponible : EscaneoEvent
}