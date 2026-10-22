package plat.clinimedik.app.ui.screens.recepcion.detalle

import java.time.LocalDate
import plat.clinimedik.app.data.model.Documento
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.TipoDocumento
import plat.clinimedik.app.data.model.TipoPago
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.data.model.VisitaConPaciente
import plat.clinimedik.app.ui.components.calcularEdad
import plat.clinimedik.app.ui.components.formatearFecha

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
    val cargando: Boolean = true,
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

fun VisitaConPaciente.aDetalleUiState(
    documentos: List<Documento>,
    historial: List<Visita>,
    hoy: LocalDate
): DetalleUiState {
    val edad = calcularEdad(paciente.fechaNacimiento, hoy)
    return DetalleUiState(
        cargando = false,
        visitaId = visita.id,
        nombre = paciente.nombreCompleto,
        datosContacto = "$edad años · ${paciente.telefono}",
        motivoConsulta = visita.motivoConsulta,
        estado = visita.estado,
        esUrgente = visita.esUrgente,
        motivoPrioridad = visita.motivoPrioridad,
        alergias = paciente.alergias,
        enfermedadesPrevias = paciente.enfermedadesPrevias,
        documentos = documentos.map { documento ->
            DocumentoDetalle(
                id = documento.id,
                tipo = documento.tipo,
                fecha = formatearFecha(documento.fecha.toLocalDate())
            )
        },
        visitasAnteriores = historial
            .filter { it.horaLlegada.toLocalDate().isBefore(hoy) }
            .map { anterior ->
                VisitaAnterior(
                    id = anterior.id,
                    fecha = formatearFecha(anterior.horaLlegada.toLocalDate()),
                    motivo = anterior.motivoConsulta,
                    diagnostico = anterior.diagnostico ?: "Sin diagnóstico registrado"
                )
            }
    )
}
