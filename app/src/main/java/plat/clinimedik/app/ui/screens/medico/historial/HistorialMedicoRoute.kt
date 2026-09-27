package plat.clinimedik.app.ui.screens.medico.historial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.ui.components.calcularEdad
import plat.clinimedik.app.ui.components.formatearFecha
import plat.clinimedik.app.ui.components.formatearHora

fun historialMedicoUiStateDePrueba(
    pacienteId: String = "pac-001",
    visitasExpandidas: Set<String> = emptySet()
): HistorialMedicoUiState {
    val paciente = FakeDataSource.paciente(pacienteId) ?: return HistorialMedicoUiState()
    val edad = calcularEdad(paciente.fechaNacimiento, FakeDataSource.hoy)

    val visitas = FakeDataSource.historialDe(paciente.id)
        .filter { it.estado == EstadoVisita.ATENDIDO || it.estado == EstadoVisita.REFERIDO }
        .map { visita ->
            val referencia = FakeDataSource.referenciaDe(visita.id)
            VisitaHistorial(
                id = visita.id,
                fecha = formatearFecha(visita.horaLlegada.toLocalDate()),
                motivo = visita.motivoConsulta,
                estado = visita.estado,
                diagnostico = visita.diagnostico ?: "Sin diagnóstico registrado",
                recetas = FakeDataSource.recetasDe(visita.id).map { receta ->
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
        nombre = paciente.nombreCompleto,
        datosPaciente = "$edad años · ${paciente.telefono}",
        alergias = paciente.alergias,
        enfermedadesPrevias = paciente.enfermedadesPrevias,
        ultimaActualizacion = "Última actualización: ${formatearHora(FakeDataSource.ahora)}",
        visitas = visitas,
        visitasExpandidas = visitasExpandidas
    )
}

@Composable
fun HistorialMedicoRoute(pacienteId: String = "pac-001") {
    val state = remember(pacienteId) { historialMedicoUiStateDePrueba(pacienteId) }
    HistorialMedicoScreen(state = state, onIntent = {})
}