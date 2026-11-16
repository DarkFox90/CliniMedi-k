package plat.clinimedik.app.ui.screens.recepcion.nuevacita

import plat.clinimedik.app.data.model.Paciente

data class PacienteSugerido(
    val pacienteId: String,
    val nombre: String,
    val telefono: String,
    val correo: String
)

fun Paciente.aSugerido(): PacienteSugerido = PacienteSugerido(
    pacienteId = id,
    nombre = nombreCompleto,
    telefono = telefono,
    correo = correo
)

data class NuevaCitaUiState(
    val cargando: Boolean = false,
    val mensajeErrorCarga: String? = null,
    val cargaReintentable: Boolean = false,
    val pacienteId: String? = null,
    val paciente: String = "",
    val pacienteTelefono: String = "",
    val errorPaciente: String? = null,
    val sugerencias: List<PacienteSugerido> = emptyList(),
    val busquedaResuelta: String = "",
    val errorBusqueda: String? = null,
    val correo: String = "",
    val errorCorreo: String? = null,
    val fecha: String = "",
    val errorFecha: String? = null,
    val hora: String = "",
    val errorHora: String? = null,
    val motivo: String = "",
    val errorMotivo: String? = null,
    val guardando: Boolean = false,
    val mensajeError: String? = null
) {
    val pacienteElegido: Boolean
        get() = pacienteId != null

    val sugerenciasVisibles: List<PacienteSugerido>
        get() = if (pacienteId == null && busquedaResuelta == paciente.trim()) {
            sugerencias
        } else {
            emptyList()
        }

    val sinCoincidencias: Boolean
        get() = pacienteId == null &&
                paciente.trim().length >= MINIMO_BUSQUEDA &&
                busquedaResuelta == paciente.trim() &&
                sugerencias.isEmpty() &&
                errorBusqueda == null

    companion object {
        const val MINIMO_BUSQUEDA = 2
    }
}