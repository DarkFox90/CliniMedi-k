package plat.clinimedik.app.ui.screens.recepcion.detalle

import plat.clinimedik.app.data.model.TipoPago
import plat.clinimedik.app.data.model.TipoDocumento

sealed interface DetalleEvent {
    data object Regresar : DetalleEvent
    data object Reintentar : DetalleEvent
    data object PasarAConsulta : DetalleEvent

    data object FinalizarConsulta : DetalleEvent
    data object EditarDiagnostico : DetalleEvent
    data class CambiarDiagnostico(val valor: String) : DetalleEvent
    data class CambiarNotas(val valor: String) : DetalleEvent
    data object GuardarDiagnostico : DetalleEvent

    data object SolicitarUrgencia : DetalleEvent
    data class CambiarMotivoUrgencia(val motivo: String) : DetalleEvent
    data object ConfirmarUrgencia : DetalleEvent

    data object AbrirReceta : DetalleEvent
    data class CambiarMedicamento(val valor: String) : DetalleEvent
    data class CambiarDosis(val valor: String) : DetalleEvent
    data class CambiarFrecuencia(val valor: String) : DetalleEvent
    data class CambiarDuracion(val valor: String) : DetalleEvent
    data class CambiarIndicaciones(val valor: String) : DetalleEvent
    data object GuardarReceta : DetalleEvent
    data object CompartirReceta : DetalleEvent

    data object AbrirReferencia : DetalleEvent
    data class CambiarEspecialidad(val valor: String) : DetalleEvent
    data class CambiarDestino(val valor: String) : DetalleEvent
    data class CambiarMotivoReferencia(val valor: String) : DetalleEvent
    data object GuardarReferencia : DetalleEvent

    data object AbrirCobro : DetalleEvent
    data class SeleccionarTipoPago(val tipoPago: TipoPago) : DetalleEvent
    data class CambiarMonto(val valor: String) : DetalleEvent
    data class CambiarAseguradora(val valor: String) : DetalleEvent
    data class CambiarAutorizacion(val valor: String) : DetalleEvent
    data class CambiarCopago(val valor: String) : DetalleEvent
    data class CambiarMontoAseguradora(val valor: String) : DetalleEvent
    data object GuardarCobro : DetalleEvent

    data object AgendarSeguimiento : DetalleEvent
    data object AbrirFicha : DetalleEvent
    data class AlternarVisitaAnterior(val visitaId: String) : DetalleEvent

    data object AdjuntarDocumento : DetalleEvent
    data class AbrirDocumento(val documentoId: String) : DetalleEvent

    data class ElegirTipoDocumento(val tipo: TipoDocumento) : DetalleEvent

    data class DocumentoCapturado(val tipo: TipoDocumento, val ruta: String) : DetalleEvent

    data object CamaraNoDisponible : DetalleEvent

    data object VerCodigoQr : DetalleEvent
    data object CerrarPanel : DetalleEvent
}
