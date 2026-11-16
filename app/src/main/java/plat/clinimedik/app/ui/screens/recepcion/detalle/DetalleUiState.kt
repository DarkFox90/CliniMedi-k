package plat.clinimedik.app.ui.screens.recepcion.detalle

import java.time.LocalDate
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.Cobro
import plat.clinimedik.app.data.model.Documento
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.Receta
import plat.clinimedik.app.data.model.Referencia
import plat.clinimedik.app.data.model.TipoDocumento
import plat.clinimedik.app.data.model.TipoPago
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.data.model.VisitaConPaciente
import plat.clinimedik.app.ui.components.calcularEdad
import plat.clinimedik.app.ui.components.etiqueta
import plat.clinimedik.app.ui.components.formatearFecha
import plat.clinimedik.app.ui.components.formatearHora
import plat.clinimedik.app.ui.components.formatearMonto
import plat.clinimedik.app.ui.components.leerMonto

private const val SIN_REGISTRAR = "Sin registrar"

data class DocumentoDetalle(
    val id: String,
    val tipo: TipoDocumento,
    val fecha: String
)

data class RecetaDetalle(
    val id: String,
    val medicamento: String,
    val indicacion: String,
    val indicaciones: String?
)

data class ReferenciaDetalle(
    val especialidad: String,
    val destino: String,
    val motivo: String
)

data class CobroDetalle(
    val tipoPago: String,
    val monto: String,
    val detalleSeguro: String?
)

data class VisitaAnterior(
    val id: String,
    val fecha: String,
    val motivo: String,
    val diagnostico: String,
    val notas: String?,
    val recetas: List<RecetaDetalle>,
    val referencia: ReferenciaDetalle?
)

data class FormularioDiagnostico(
    val diagnostico: String = "",
    val notas: String = "",
    val errorDiagnostico: String? = null
)

data class FormularioReceta(
    val medicamento: String = "",
    val dosis: String = "",
    val frecuencia: String = "",
    val duracion: String = "",
    val indicaciones: String = "",
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
    val aseguradora: String = "",
    val numeroAutorizacion: String = "",
    val copago: String = "",
    val montoAseguradora: String = "",
    val errorTipoPago: String? = null,
    val errorMonto: String? = null,
    val errorAseguradora: String? = null,
    val errorCopago: String? = null,
    val errorMontoAseguradora: String? = null
) {
    val esSeguro: Boolean
        get() = tipoPago == TipoPago.SEGURO

    /** Copago más monto a la aseguradora, o `null` mientras alguno de los dos no sea un monto válido. */
    val totalSeguro: Double?
        get() {
            val copagoLeido = leerMonto(copago) ?: return null
            val aseguradoraLeida = leerMonto(montoAseguradora) ?: return null
            return copagoLeido + aseguradoraLeida
        }
}

sealed interface PanelDetalle {
    data class Urgencia(
        val motivo: String = "",
        val mensajeError: String? = null
    ) : PanelDetalle

    /** Con [finalizaConsulta] en `true`, guardar también termina la consulta. */
    data class Diagnostico(
        val formulario: FormularioDiagnostico = FormularioDiagnostico(),
        val finalizaConsulta: Boolean = true
    ) : PanelDetalle

    data class Receta(val formulario: FormularioReceta = FormularioReceta()) : PanelDetalle

    data class Referencia(val formulario: FormularioReferencia = FormularioReferencia()) : PanelDetalle

    data class Cobro(val formulario: FormularioCobro = FormularioCobro()) : PanelDetalle

    data object Documento : PanelDetalle
}

data class DetalleUiState(
    val cargando: Boolean = true,
    val mensajeError: String? = null,
    val guardando: Boolean = false,
    val visitaId: String = "",
    val pacienteId: String = "",
    val nombre: String = "",
    val datosContacto: String = "",
    val correo: String = "",
    val aseguradora: String? = null,
    val motivoConsulta: String = "",
    val estado: EstadoVisita = EstadoVisita.ESPERANDO,
    val esUrgente: Boolean = false,
    val motivoPrioridad: String? = null,
    val alergias: String = "",
    val enfermedadesPrevias: String = "",
    val tratamientosPrevios: String = "",
    val cirugias: String = "",
    val diagnostico: String? = null,
    val notas: String? = null,
    val recetas: List<RecetaDetalle> = emptyList(),
    val referencia: ReferenciaDetalle? = null,
    val cobro: CobroDetalle? = null,
    val textoReceta: String? = null,
    val proximaCita: String? = null,
    val ultimaReferencia: String? = null,
    val documentos: List<DocumentoDetalle> = emptyList(),
    val visitasAnteriores: List<VisitaAnterior> = emptyList(),
    val visitaAnteriorAbiertaId: String? = null,
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

    /** La consulta ya terminó: con alta o con referencia. */
    val consultaTerminada: Boolean
        get() = estado == EstadoVisita.ATENDIDO || estado == EstadoVisita.REFERIDO

    val motivoSeguimiento: String
        get() = "Seguimiento: $motivoConsulta"
}

/**
 * Arma el estado del Detalle. [recetas] y [referencias] son todas las del paciente;
 * aquí se separan las de esta visita y las de las visitas anteriores.
 */
fun VisitaConPaciente.aDetalleUiState(
    documentos: List<Documento>,
    historial: List<Visita>,
    recetas: List<Receta>,
    referencias: List<Referencia>,
    cobro: Cobro?,
    proximaCita: Cita?,
    nombreMedico: String,
    hoy: LocalDate
): DetalleUiState {
    val edad = calcularEdad(paciente.fechaNacimiento, hoy)
    val recetasPorVisita = recetas.groupBy { it.visitaId }
    val referenciaPorVisita = referencias.associateBy { it.visitaId }
    val anteriores = historial
        .filter { it.id != visita.id && it.horaLlegada.isBefore(visita.horaLlegada) }
        .sortedByDescending { it.horaLlegada }
    val recetasDeEstaVisita = recetasPorVisita[visita.id].orEmpty()

    return DetalleUiState(
        cargando = false,
        visitaId = visita.id,
        pacienteId = paciente.id,
        nombre = paciente.nombreCompleto,
        datosContacto = "$edad años · ${paciente.telefono}",
        correo = paciente.correo.ifBlank { SIN_REGISTRAR },
        aseguradora = paciente.aseguradora,
        motivoConsulta = visita.motivoConsulta,
        estado = visita.estado,
        esUrgente = visita.esUrgente,
        motivoPrioridad = visita.motivoPrioridad,
        alergias = paciente.alergias.ifBlank { SIN_REGISTRAR },
        enfermedadesPrevias = paciente.enfermedadesPrevias.ifBlank { SIN_REGISTRAR },
        tratamientosPrevios = paciente.tratamientosPrevios.ifBlank { SIN_REGISTRAR },
        cirugias = paciente.cirugias.ifBlank { SIN_REGISTRAR },
        diagnostico = visita.diagnostico,
        notas = visita.notas,
        recetas = recetasDeEstaVisita.map { it.aDetalle() },
        referencia = referenciaPorVisita[visita.id]?.aDetalle(),
        cobro = cobro?.aDetalle(),
        textoReceta = if (recetasDeEstaVisita.isEmpty()) {
            null
        } else {
            textoReceta(paciente.nombreCompleto, nombreMedico, visita.horaLlegada.toLocalDate(), recetasDeEstaVisita)
        },
        proximaCita = proximaCita?.let { cita ->
            "${formatearFecha(cita.fechaHora.toLocalDate())} · ${formatearHora(cita.fechaHora)} · ${cita.motivo}"
        },
        ultimaReferencia = anteriores
            .firstNotNullOfOrNull { anterior -> referenciaPorVisita[anterior.id]?.let { anterior to it } }
            ?.let { (anterior, referencia) ->
                "${referencia.especialidad} · ${referencia.destino} · ${formatearFecha(anterior.horaLlegada.toLocalDate())}"
            },
        documentos = documentos.map { documento ->
            DocumentoDetalle(
                id = documento.id,
                tipo = documento.tipo,
                fecha = formatearFecha(documento.fecha.toLocalDate())
            )
        },
        visitasAnteriores = anteriores.map { anterior ->
            VisitaAnterior(
                id = anterior.id,
                fecha = formatearFecha(anterior.horaLlegada.toLocalDate()),
                motivo = anterior.motivoConsulta,
                diagnostico = anterior.diagnostico ?: "Sin diagnóstico registrado",
                notas = anterior.notas,
                recetas = recetasPorVisita[anterior.id].orEmpty().map { it.aDetalle() },
                referencia = referenciaPorVisita[anterior.id]?.aDetalle()
            )
        }
    )
}

private fun Receta.aDetalle(): RecetaDetalle = RecetaDetalle(
    id = id,
    medicamento = medicamento,
    indicacion = "$dosis · $frecuencia · $duracion",
    indicaciones = indicaciones
)

private fun Referencia.aDetalle(): ReferenciaDetalle = ReferenciaDetalle(
    especialidad = especialidad,
    destino = destino,
    motivo = motivo
)

private fun Cobro.aDetalle(): CobroDetalle {
    val partesSeguro = listOfNotNull(
        aseguradora,
        numeroAutorizacion?.let { "Autorización $it" },
        copago?.let { "Paciente ${formatearMonto(it)}" },
        montoAseguradora?.let { "Seguro ${formatearMonto(it)}" }
    )
    return CobroDetalle(
        tipoPago = tipoPago.etiqueta(),
        monto = formatearMonto(monto),
        detalleSeguro = partesSeguro.joinToString(" · ").ifBlank { null }
    )
}

/** Texto que se comparte con "Compartir receta". No lleva diagnóstico ni notas. */
private fun textoReceta(
    nombrePaciente: String,
    nombreMedico: String,
    fecha: LocalDate,
    recetas: List<Receta>
): String = buildString {
    appendLine("Receta médica")
    appendLine("Paciente: $nombrePaciente")
    if (nombreMedico.isNotBlank()) {
        appendLine("Médico: $nombreMedico")
    }
    appendLine("Fecha: ${formatearFecha(fecha)}")
    recetas.forEachIndexed { indice, receta ->
        appendLine()
        appendLine("${indice + 1}. ${receta.medicamento}")
        appendLine("   ${receta.dosis} · ${receta.frecuencia} · ${receta.duracion}")
        receta.indicaciones?.let { appendLine("   Indicaciones: $it") }
    }
}.trimEnd()
