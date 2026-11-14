package plat.clinimedik.app.ui.screens.recepcion.buscar

import plat.clinimedik.app.data.model.Paciente

val idsRecientes = listOf("pac-005", "pac-004", "pac-002")

data class PacienteEncontrado(
    val pacienteId: String,
    val nombre: String,
    val telefono: String
)

/**
 * Con el campo vacío la pantalla muestra dos listas: recientes y todos los pacientes.
 * Al escribir, muestra solo los resultados de la búsqueda.
 */
data class BuscarUiState(
    val cargando: Boolean = true,
    val texto: String = "",
    val recientes: List<PacienteEncontrado> = emptyList(),
    val todos: List<PacienteEncontrado> = emptyList(),
    val resultados: List<PacienteEncontrado> = emptyList(),
    val mensajeError: String? = null
) {
    val buscando: Boolean
        get() = texto.isNotBlank()

    val sinPacientes: Boolean
        get() = if (buscando) resultados.isEmpty() else recientes.isEmpty() && todos.isEmpty()

    val tituloResultados: String
        get() = if (resultados.size == 1) "1 resultado" else "${resultados.size} resultados"

    val tituloTodos: String
        get() = "Todos los pacientes (${todos.size})"

    val mensajeVacio: String
        get() = if (buscando) {
            "No se encontraron pacientes con “${texto.trim()}”"
        } else {
            "Todavía no hay pacientes registrados"
        }
}

fun Paciente.aPacienteEncontrado(): PacienteEncontrado = PacienteEncontrado(
    pacienteId = id,
    nombre = nombreCompleto,
    telefono = telefono
)
