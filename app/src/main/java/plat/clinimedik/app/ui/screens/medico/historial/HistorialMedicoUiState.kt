package plat.clinimedik.app.ui.screens.medico.historial

import java.time.LocalDateTime
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.Paciente
import plat.clinimedik.app.data.model.Receta
import plat.clinimedik.app.data.model.Referencia
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.ui.components.calcularEdad
import plat.clinimedik.app.ui.components.formatearFecha
import plat.clinimedik.app.ui.components.formatearHora

data class RecetaHistorial(
    val medicamento: String,
    val indicaciones: String
)

data class VisitaHistorial(
    val id: String,
    val fecha: String,
    val motivo: String,
    val estado: EstadoVisita,
    val diagnostico: String,
    val recetas: List<RecetaHistorial>,
    val referencia: String?,
    val notas: String?
)

data class HistorialMedicoUiState(
    val cargando: Boolean = true,
    val nombre: String = "",
    val datosPaciente: String = "",
    val alergias: String = "",
    val enfermedadesPrevias: String = "",
    val ultimaActualizacion: String = "",
    val visitas: List<VisitaHistorial> = emptyList(),
    val visitasExpandidas: Set<String> = emptySet()
)

fun calcularHistorial(
    paciente: Paciente,
    historial: List<Visita>,
    recetas: List<Receta>,
    referencias: List<Referencia>,
    ahora: LocalDateTime
): HistorialMedicoUiState {
    val edad = calcularEdad(paciente.fechaNacimiento, ahora.toLocalDate())
    val visitas = historial
        .filter { it.estado == EstadoVisita.ATENDIDO || it.estado == EstadoVisita.REFERIDO }
        .map { visita ->
            val referencia = referencias.firstOrNull { it.visitaId == visita.id }
            VisitaHistorial(
                id = visita.id,
                fecha = formatearFecha(visita.horaLlegada.toLocalDate()),
                motivo = visita.motivoConsulta,
                estado = visita.estado,
                diagnostico = visita.diagnostico ?: "Sin diagnóstico registrado",
                recetas = recetas.filter { it.visitaId == visita.id }.map { receta ->
                    RecetaHistorial(
                        medicamento = receta.medicamento,
                        indicaciones = "${receta.dosis} · ${receta.frecuencia} · ${receta.duracion}"
                    )
                },
                referencia = referencia?.let { "${it.especialidad} con ${it.destino}. ${it.motivo}." },
                notas = visita.notas
            )
        }
    return HistorialMedicoUiState(
        cargando = false,
        nombre = paciente.nombreCompleto,
        datosPaciente = "$edad años · ${paciente.telefono}",
        alergias = paciente.alergias,
        enfermedadesPrevias = paciente.enfermedadesPrevias,
        ultimaActualizacion = "Última actualización: ${formatearHora(ahora)}",
        visitas = visitas
    )
}