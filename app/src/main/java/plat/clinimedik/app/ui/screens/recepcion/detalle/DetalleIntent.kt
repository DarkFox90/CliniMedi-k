package plat.clinimedik.app.ui.screens.recepcion.detalle

import plat.clinimedik.app.data.model.TipoPago

sealed interface DetalleIntent {
    data object Regresar : DetalleIntent
    data object PasarAConsulta : DetalleIntent
    data object FinalizarConsulta : DetalleIntent

    data object SolicitarUrgencia : DetalleIntent
    data class CambiarMotivoUrgencia(val motivo: String) : DetalleIntent
    data object ConfirmarUrgencia : DetalleIntent

    data object AbrirReceta : DetalleIntent
    data class CambiarMedicamento(val valor: String) : DetalleIntent
    data class CambiarDosis(val valor: String) : DetalleIntent
    data class CambiarFrecuencia(val valor: String) : DetalleIntent
    data class CambiarDuracion(val valor: String) : DetalleIntent
    data object GuardarReceta : DetalleIntent

    data object AbrirReferencia : DetalleIntent
    data class CambiarEspecialidad(val valor: String) : DetalleIntent
    data class CambiarDestino(val valor: String) : DetalleIntent
    data class CambiarMotivoReferencia(val valor: String) : DetalleIntent
    data object GuardarReferencia : DetalleIntent

    data object AbrirCobro : DetalleIntent
    data class SeleccionarTipoPago(val tipoPago: TipoPago) : DetalleIntent
    data class CambiarMonto(val valor: String) : DetalleIntent
    data object GuardarCobro : DetalleIntent

    data object AdjuntarDocumento : DetalleIntent
    data class AbrirDocumento(val documentoId: String) : DetalleIntent

    data object CerrarPanel : DetalleIntent
}