package plat.clinimedik.app.ui.screens.recepcion.buscar

import plat.clinimedik.app.data.model.Paciente

val idsRecientes = listOf("pac-005", "pac-004", "pac-002")

data class PacienteEncontrado(
    val pacienteId: String,
    val nombre: String,
    val telefono: String
)

data class BuscarUiState(
    val cargando: Boolean = true,
    val texto: String = "",
    val recientes: List<PacienteEncontrado> = emptyList(),
    val resultados: List<PacienteEncontrado> = emptyList()
) {
    val buscando: Boolean
        get() = texto.isNotBlank()

    val pacientes: List<PacienteEncontrado>
        get() = if (buscando) resultados else recientes

    val tituloLista: String
        get() = when {
            !buscando -> "Recientes"
            resultados.size == 1 -> "1 resultado"
            else -> "${resultados.size} resultados"
        }

    val mensajeVacio: String
        get() = if (buscando) {
            "No se encontraron pacientes con “${texto.trim()}”"
        } else {
            "No hay búsquedas recientes"
        }
}

fun Paciente.aPacienteEncontrado(): PacienteEncontrado = PacienteEncontrado(
    pacienteId = id,
    nombre = nombreCompleto,
    telefono = telefono
)