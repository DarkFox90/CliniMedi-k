package plat.clinimedik.app.ui.screens.recepcion.detalle

import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.TipoDocumento
import plat.clinimedik.app.data.model.TipoPago

data class DocumentoDetalle(
    val id: String,
    val tipo: TipoDocumento,
    val fecha: String
)

data class VisitaAnterior(
    val id: String,
    val fecha: String,
    val motivo: String,
    val diagnostico: String
)

data class FormularioReceta(
    val medicamento: String = "",
    val dosis: String = "",
    val frecuencia: String = "",
    val duracion: String = "",
    val errorMedicamento: String? = null,
    val errorDosis: String? = null,
    val errorFrecuencia: String? = null,
    val errorDuracion: String? = null
)

data class FormularioReferencia(
    val especialidad: String = "",
    val destino: String = "",
    val motivo: String = "",
    val errorEspecialidad: String? = null,
    val errorDestino: String? = null,
    val errorMotivo: String? = null
)

data class FormularioCobro(
    val tipoPago: TipoPago? = null,
    val monto: String = "",
    val errorTipoPago: String? = null,
    val errorMonto: String? = null
)

sealed interface PanelDetalle {
    data class Urgencia(
        val motivo: String = "",
        val mensajeError: String? = null
    ) : PanelDetalle

    data class Receta(val formulario: FormularioReceta = FormularioReceta()) : PanelDetalle

    data class Referencia(val formulario: FormularioReferencia = FormularioReferencia()) : PanelDetalle

    data class Cobro(val formulario: FormularioCobro = FormularioCobro()) : PanelDetalle
}

data class DetalleUiState(
    val visitaId: String = "",
    val nombre: String = "",
    val datosContacto: String = "",
    val motivoConsulta: String = "",
    val estado: EstadoVisita = EstadoVisita.ESPERANDO,
    val esUrgente: Boolean = false,
    val motivoPrioridad: String? = null,
    val alergias: String = "",
    val enfermedadesPrevias: String = "",
    val documentos: List<DocumentoDetalle> = emptyList(),
    val visitasAnteriores: List<VisitaAnterior> = emptyList(),
    val panel: PanelDetalle? = null
) {
    val esUrgenteEnEspera: Boolean
        get() = esUrgente && estado == EstadoVisita.ESPERANDO

    val puedeMarcarseUrgente: Boolean
        get() = estado == EstadoVisita.ESPERANDO && !esUrgente

    val puedeRegistrarConsulta: Boolean
        get() = estado != EstadoVisita.ESPERANDO

    val tieneAccionDeEstado: Boolean
        get() = estado == EstadoVisita.ESPERANDO || estado == EstadoVisita.EN_CONSULTA
}