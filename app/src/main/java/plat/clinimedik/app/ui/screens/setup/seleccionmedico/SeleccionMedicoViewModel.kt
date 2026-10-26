package plat.clinimedik.app.ui.screens.setup.seleccionmedico

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.model.RolDispositivo
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.ui.navigation.SeleccionMedico

class SeleccionMedicoViewModel(
    savedStateHandle: SavedStateHandle,
    private val medicoRepositorio: MedicoRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio
) : ViewModel() {

    private val rol: RolDispositivo = RolDispositivo.valueOf(savedStateHandle.toRoute<SeleccionMedico>().rol)

    private val _state = MutableStateFlow(SeleccionMedicoUiState())
    val state: StateFlow<SeleccionMedicoUiState> = _state.asStateFlow()

    private val _efectos = Channel<SeleccionMedicoEfecto>()
    val efectos: Flow<SeleccionMedicoEfecto> = _efectos.receiveAsFlow()

    init {
        observarMedicos()
    }

    fun onEvent(event: SeleccionMedicoEvent) {
        when (event) {
            is SeleccionMedicoEvent.SeleccionarMedico -> _state.update {
                it.copy(eleccion = MedicoElegido.Existente(event.id))
            }
            SeleccionMedicoEvent.MostrarFormulario -> _state.update {
                it.copy(eleccion = MedicoElegido.Nuevo())
            }
            is SeleccionMedicoEvent.CambiarNombre -> actualizarFormulario {
                it.copy(nombre = event.texto, errorNombre = null)
            }
            is SeleccionMedicoEvent.CambiarEspecialidad -> actualizarFormulario {
                it.copy(especialidad = event.texto, errorEspecialidad = null)
            }
            SeleccionMedicoEvent.Continuar -> continuar()
        }
    }

    private fun observarMedicos() {
        viewModelScope.launch {
            medicoRepositorio.observarMedicos().collect { medicos ->
                _state.update { it.copy(cargando = false, medicos = medicos) }
            }
        }
    }

    private fun actualizarFormulario(cambio: (FormularioMedico) -> FormularioMedico) {
        _state.update { actual ->
            val eleccion = actual.eleccion as? MedicoElegido.Nuevo ?: return@update actual
            actual.copy(eleccion = eleccion.copy(formulario = cambio(eleccion.formulario)))
        }
    }

    private fun continuar() {
        if (_state.value.guardando) return
        when (val eleccion = _state.value.eleccion) {
            is MedicoElegido.Existente -> guardarConfiguracion(eleccion.medicoId)
            is MedicoElegido.Nuevo -> crearMedico(eleccion)
            null -> Unit
        }
    }

    private fun crearMedico(eleccion: MedicoElegido.Nuevo) {
        val formulario = eleccion.formulario
        val errorNombre = if (formulario.nombre.isBlank()) "El nombre es obligatorio" else null
        val errorEspecialidad = if (formulario.especialidad.isBlank()) "La especialidad es obligatoria" else null
        if (errorNombre != null || errorEspecialidad != null) {
            _state.update {
                it.copy(
                    eleccion = eleccion.copy(
                        formulario = formulario.copy(errorNombre = errorNombre, errorEspecialidad = errorEspecialidad)
                    )
                )
            }
            return
        }
        _state.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val medicoId = medicoRepositorio.crear(formulario.nombre, formulario.especialidad)
            terminar(medicoId)
        }
    }

    private fun guardarConfiguracion(medicoId: String) {
        _state.update { it.copy(guardando = true) }
        viewModelScope.launch { terminar(medicoId) }
    }

    private suspend fun terminar(medicoId: String) {
        configuracionRepositorio.guardar(rol, medicoId)
        _efectos.send(SeleccionMedicoEfecto.ConfiguracionGuardada(rol))
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                SeleccionMedicoViewModel(
                    createSavedStateHandle(),
                    app.container.medicoRepositorio,
                    app.container.configuracionRepositorio
                )
            }
        }
    }
}

sealed interface SeleccionMedicoEfecto {
    data class ConfiguracionGuardada(val rol: RolDispositivo) : SeleccionMedicoEfecto
}