package plat.clinimedik.app.ui.screens.setup.seleccionmedico

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
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

    private val reintentos = MutableStateFlow(0)

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
            SeleccionMedicoEvent.Reintentar -> {
                _state.update { it.copy(cargando = true, mensajeError = null) }
                reintentos.update { it + 1 }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarMedicos() {
        viewModelScope.launch {
            // Cada "Reintentar" cambia el número y vuelve a empezar la carga.
            reintentos
                .flatMapLatest {
                    medicoRepositorio.observarMedicos()
                        .catch {
                            _state.update { actual ->
                                actual.copy(cargando = false, mensajeError = MENSAJE_ERROR_CARGA)
                            }
                        }
                }
                .collect { medicos ->
                    _state.update { it.copy(cargando = false, mensajeError = null, medicos = medicos) }
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
            is MedicoElegido.Existente -> guardar { terminar(eleccion.medicoId) }
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
        guardar {
            val medicoId = medicoRepositorio.crear(formulario.nombre, formulario.especialidad)
            // Si después falla guardar la configuración, "Continuar" reintenta sin crear otro médico.
            _state.update { it.copy(eleccion = MedicoElegido.Existente(medicoId)) }
            terminar(medicoId)
        }
    }

    /** Bloquea el botón mientras guarda y lo libera si algo falla. */
    private fun guardar(accion: suspend () -> Unit) {
        _state.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                accion()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update { it.copy(guardando = false) }
                _efectos.send(SeleccionMedicoEfecto.MostrarMensaje(MENSAJE_ERROR_GUARDAR))
            }
        }
    }

    private suspend fun terminar(medicoId: String) {
        configuracionRepositorio.guardar(rol, medicoId)
        _efectos.send(SeleccionMedicoEfecto.ConfiguracionGuardada(rol))
    }

    companion object {
        private const val MENSAJE_ERROR_CARGA = "No se pudo cargar la lista de médicos"
        private const val MENSAJE_ERROR_GUARDAR = "No se pudo guardar la configuración. Intenta de nuevo."

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
    data class MostrarMensaje(val texto: String) : SeleccionMedicoEfecto
}