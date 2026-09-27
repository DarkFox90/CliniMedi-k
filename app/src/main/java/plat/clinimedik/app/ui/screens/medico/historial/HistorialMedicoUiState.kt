package plat.clinimedik.app.ui.screens.medico.historial

import plat.clinimedik.app.data.model.EstadoVisita

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
    val nombre: String = "",
    val datosPaciente: String = "",
    val alergias: String = "",
    val enfermedadesPrevias: String = "",
    val ultimaActualizacion: String = "",
    val visitas: List<VisitaHistorial> = emptyList(),
    val visitasExpandidas: Set<String> = emptySet()
)