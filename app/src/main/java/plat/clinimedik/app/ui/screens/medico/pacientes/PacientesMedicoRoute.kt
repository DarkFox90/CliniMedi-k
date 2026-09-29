package plat.clinimedik.app.ui.screens.medico.pacientes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.Collator
import java.util.Locale
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.ui.components.ContenedorMedico
import plat.clinimedik.app.ui.components.PestanaMedico
import plat.clinimedik.app.ui.components.formatearFecha

private val ordenAlfabetico: Collator = Collator.getInstance(Locale.forLanguageTag("es"))

private fun Visita.terminoConsulta(): Boolean =
    estado == EstadoVisita.ATENDIDO || estado == EstadoVisita.REFERIDO

private fun detalleDeVisitas(pacienteId: String): String {
    val terminadas = FakeDataSource.historialDe(pacienteId).filter { it.terminoConsulta() }
    val ultima = terminadas.firstOrNull() ?: return "Sin visitas terminadas"
    val cantidad = if (terminadas.size == 1) "1 visita" else "${terminadas.size} visitas"
    return "$cantidad · última: ${formatearFecha(ultima.horaLlegada.toLocalDate())}"
}

private fun pacientesDelMedico(medicoId: String): List<PacienteDelMedico> =
    FakeDataSource.visitas
        .filter { it.medicoId == medicoId }
        .map { it.pacienteId }
        .distinct()
        .mapNotNull { FakeDataSource.paciente(it) }
        .sortedWith { a, b -> ordenAlfabetico.compare(a.nombreCompleto, b.nombreCompleto) }
        .map { paciente ->
            PacienteDelMedico(
                pacienteId = paciente.id,
                nombre = paciente.nombreCompleto,
                detalle = detalleDeVisitas(paciente.id)
            )
        }

fun pacientesMedicoUiStateDePrueba(texto: String = ""): PacientesMedicoUiState {
    val medico = FakeDataSource.medicoActual
    val todos = pacientesDelMedico(medico.id)
    val pacientes = if (texto.isBlank()) {
        todos
    } else {
        val encontrados = FakeDataSource.buscarPacientes(texto).map { it.id }.toSet()
        todos.filter { it.pacienteId in encontrados }
    }
    return PacientesMedicoUiState(
        subtitulo = "${medico.nombre} · Todos los pacientes",
        texto = texto,
        pacientes = pacientes
    )
}

@Composable
fun PacientesMedicoRoute() {
    val state = remember { pacientesMedicoUiStateDePrueba() }
    ContenedorMedico(seleccion = PestanaMedico.PACIENTES) {
        PacientesMedicoScreen(state = state, onIntent = {})
    }
}
