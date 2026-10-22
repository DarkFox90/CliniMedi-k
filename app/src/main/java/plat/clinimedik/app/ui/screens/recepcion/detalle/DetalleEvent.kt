package plat.clinimedik.app.ui.screens.recepcion.detalle

import plat.clinimedik.app.data.model.TipoPago

sealed interface DetalleEvent {
    data object Regresar : DetalleEvent
    data object PasarAConsulta : DetalleEvent
    data object FinalizarConsulta : DetalleEvent

    data object SolicitarUrgencia : DetalleEvent
    data class CambiarMotivoUrgencia(val motivo: String) : DetalleEvent
    data object ConfirmarUrgencia : DetalleEvent

    data object AbrirReceta : DetalleEvent
    data class CambiarMedicamento(val valor: String) : DetalleEvent
    data class CambiarDosis(val valor: String) : DetalleEvent
    data class CambiarFrecuencia(val valor: String) : DetalleEvent
    data class CambiarDuracion(val valor: String) : DetalleEvent
    data object GuardarReceta : DetalleEvent

    data object AbrirReferencia : DetalleEvent
    data class CambiarEspecialidad(val valor: String) : DetalleEvent
    data class CambiarDestino(val valor: String) : DetalleEvent
    data class CambiarMotivoReferencia(val valor: String) : DetalleEvent
    data object GuardarReferencia : DetalleEvent

    data object AbrirCobro : DetalleEvent
    data class SeleccionarTipoPago(val tipoPago: TipoPago) : DetalleEvent
    data class CambiarMonto(val valor: String) : DetalleEvent
    data object GuardarCobro : DetalleEvent

    data object AdjuntarDocumento : DetalleEvent
    data class AbrirDocumento(val documentoId: String) : DetalleEvent

    data object CerrarPanel : DetalleEvent
}