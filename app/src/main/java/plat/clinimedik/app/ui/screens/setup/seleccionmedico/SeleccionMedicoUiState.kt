package plat.clinimedik.app.ui.screens.setup.seleccionmedico

import plat.clinimedik.app.data.model.Medico

data class FormularioMedico(
    val nombre: String = "",
    val especialidad: String = "",
    val errorNombre: String? = null,
    val errorEspecialidad: String? = null
)

sealed interface MedicoElegido {
    data class Existente(val medicoId: String) : MedicoElegido

    data class Nuevo(val formulario: FormularioMedico = FormularioMedico()) : MedicoElegido
}

data class SeleccionMedicoUiState(
    val medicos: List<Medico> = emptyList(),
    val eleccion: MedicoElegido? = null
) {
    val medicoSeleccionadoId: String?
        get() = (eleccion as? MedicoElegido.Existente)?.medicoId

    val formulario: FormularioMedico?
        get() = (eleccion as? MedicoElegido.Nuevo)?.formulario

    val puedeContinuar: Boolean
        get() = eleccion != null
}
