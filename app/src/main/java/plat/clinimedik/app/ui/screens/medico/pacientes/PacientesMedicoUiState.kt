package plat.clinimedik.app.ui.screens.medico.pacientes

import java.text.Collator
import java.util.Locale
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.Paciente
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.ui.components.formatearFecha

data class PacienteDelMedico(
    val pacienteId: String,
    val nombre: String,
    val detalle: String
)

data class PacientesMedicoUiState(
    val cargando: Boolean = true,
    val subtitulo: String = "",
    val texto: String = "",
    val pacientes: List<PacienteDelMedico> = emptyList()
) {
    val tituloLista: String
        get() = if (pacientes.size == 1) "1 paciente" else "${pacientes.size} pacientes"

    val mensajeVacio: String
        get() = if (texto.isNotBlank()) {
            "No se encontraron pacientes con “${texto.trim()}”"
        } else {
            "Aún no hay pacientes con visitas"
        }
}

private val ordenAlfabetico: Collator = Collator.getInstance(Locale.forLanguageTag("es"))

fun Paciente.aPacienteDelMedico(historial: List<Visita>): PacienteDelMedico {
    val terminadas = historial.filter { it.estado == EstadoVisita.ATENDIDO || it.estado == EstadoVisita.REFERIDO }
    val ultima = terminadas.firstOrNull()
    val detalle = if (ultima == null) {
        "Sin visitas terminadas"
    } else {
        val cantidad = if (terminadas.size == 1) "1 visita" else "${terminadas.size} visitas"
        "$cantidad · última: ${formatearFecha(ultima.horaLlegada.toLocalDate())}"
    }
    return PacienteDelMedico(pacienteId = id, nombre = nombreCompleto, detalle = detalle)
}

fun List<PacienteDelMedico>.ordenadosPorNombre(): List<PacienteDelMedico> =
    sortedWith { a, b -> ordenAlfabetico.compare(a.nombre, b.nombre) }
