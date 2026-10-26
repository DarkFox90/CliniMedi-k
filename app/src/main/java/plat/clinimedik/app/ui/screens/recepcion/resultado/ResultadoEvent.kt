package plat.clinimedik.app.ui.screens.recepcion.resultado

sealed interface ResultadoEvent {
    data object VerHistorial : ResultadoEvent
    data object AgregarAFila : ResultadoEvent
    data class CambiarMotivo(val texto: String) : ResultadoEvent
    data object ConfirmarAgregarAFila : ResultadoEvent
    data object CancelarAgregarAFila : ResultadoEvent
    data object EscanearOtro : ResultadoEvent
    data object Regresar : ResultadoEvent
}