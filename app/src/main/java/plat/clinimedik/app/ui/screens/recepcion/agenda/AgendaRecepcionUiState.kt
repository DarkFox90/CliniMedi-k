package plat.clinimedik.app.ui.screens.recepcion.agenda

import java.time.LocalDate
import java.time.LocalDateTime
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.EstadoCita
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.ui.components.DiaVisible
import plat.clinimedik.app.ui.components.formatearFechaLarga
import plat.clinimedik.app.ui.components.formatearHora

data class ElementoCita(
    val citaId: String,
    val hora: String,
    val nombrePaciente: String,
    val motivo: String,
    val estadoConfirmacion: EstadoConfirmacion,
    val recordatorioEnviado: Boolean?,
    val estado: EstadoCita,
    val fechaHora: LocalDateTime,
    val tienePaciente: Boolean
) {
    val admiteAcciones: Boolean
        get() = estado == EstadoCita.PROGRAMADA
}

data class FormularioReprogramar(
    val fecha: String = "",
    val hora: String = "",
    val errorFecha: String? = null,
    val errorHora: String? = null
)

sealed interface PanelAgenda {
    val citaId: String

    data class Acciones(override val citaId: String) : PanelAgenda

    data class Reprogramar(
        override val citaId: String,
        val formulario: FormularioReprogramar = FormularioReprogramar()
    ) : PanelAgenda

    data class ConfirmarCancelacion(override val citaId: String) : PanelAgenda
}

data class AgendaRecepcionUiState(
    val cargando: Boolean = true,
    val fecha: LocalDate = LocalDate.now(),
    val textoFecha: String = "",
    val esHoy: Boolean = true,
    val elementos: List<ElementoCita> = emptyList(),
    val mensajeError: String? = null,
    val panel: PanelAgenda? = null,
    val guardando: Boolean = false
) {
    val mensajeSinCitas: String
        get() = if (esHoy) "No hay citas programadas para hoy" else "No hay citas programadas para este día"
}

fun calcularAgendaRecepcion(
    citas: List<Cita>,
    dia: DiaVisible
): AgendaRecepcionUiState = AgendaRecepcionUiState(
    cargando = false,
    fecha = dia.fecha,
    textoFecha = formatearFechaLarga(dia.fecha),
    esHoy = dia.esHoy,
    elementos = citas.map { cita ->
        ElementoCita(
            citaId = cita.id,
            hora = formatearHora(cita.fechaHora),
            nombrePaciente = cita.nombrePaciente,
            motivo = cita.motivo,
            estadoConfirmacion = cita.estadoConfirmacion,
            recordatorioEnviado = cita.recordatorioEnviado.takeUnless { cita.correoPaciente.isNullOrBlank() },
            estado = cita.estado,
            fechaHora = cita.fechaHora,
            tienePaciente = cita.pacienteId != null
        )
    }
)