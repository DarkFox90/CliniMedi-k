package plat.clinimedik.app.ui.screens.recepcion.resultado

sealed interface ResultadoEvent {
    data object VerHistorial : ResultadoEvent
    data object EscanearOtro : ResultadoEvent
    data object Regresar : ResultadoEvent
}