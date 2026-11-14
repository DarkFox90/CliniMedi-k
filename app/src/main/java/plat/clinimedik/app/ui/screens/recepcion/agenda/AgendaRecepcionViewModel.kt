package plat.clinimedik.app.ui.screens.recepcion.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.ui.components.SeleccionDia

class AgendaRecepcionViewModel(
    private val citaRepositorio: CitaRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(AgendaRecepcionUiState())
    val state: StateFlow<AgendaRecepcionUiState> = _state.asStateFlow()

    private val _efectos = Channel<AgendaRecepcionEfecto>()
    val efectos: Flow<AgendaRecepcionEfecto> = _efectos.receiveAsFlow()

    private val seleccionDia = SeleccionDia()
    private val reintentos = MutableStateFlow(0)

    init {
        observarCitas()
        citaRepositorio.actualizarConfirmaciones()
    }

    fun onEvent(event: AgendaRecepcionEvent) {
        when (event) {
            AgendaRecepcionEvent.NuevaCita -> viewModelScope.launch {
                _efectos.send(AgendaRecepcionEfecto.IrANuevaCita)
            }
            AgendaRecepcionEvent.DiaAnterior -> seleccionDia.anterior()
            AgendaRecepcionEvent.DiaSiguiente -> seleccionDia.siguiente()
            is AgendaRecepcionEvent.ElegirDia -> seleccionDia.elegir(event.fecha)
            AgendaRecepcionEvent.VolverAHoy -> seleccionDia.volverAHoy()
            AgendaRecepcionEvent.Reintentar -> {
                _state.update { it.copy(cargando = true, mensajeError = null) }
                reintentos.update { it + 1 }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarCitas() {
        val medico = configuracionRepositorio.configuracion
            .filterNotNull()
            .map { it.medicoId }
            .distinctUntilChanged()
        viewModelScope.launch {
            reintentos
                .flatMapLatest {
                    combine(medico, seleccionDia.dia) { medicoId, dia -> medicoId to dia }
                        .flatMapLatest { (medicoId, dia) ->
                            citaRepositorio.observarCitasDelDia(medicoId, dia.fecha)
                                .map { citas -> calcularAgendaRecepcion(citas, dia) }
                        }
                        .catch {
                            emit(_state.value.copy(cargando = false, mensajeError = MENSAJE_ERROR_CARGA))
                        }
                }
                .collect { nuevo -> _state.value = nuevo }
        }
    }

    companion object {
        private const val MENSAJE_ERROR_CARGA = "No se pudieron cargar las citas"

        val Factory = viewModelFactory {
            initializer {
                val app = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CliniMedikApp

                AgendaRecepcionViewModel(
                    app.container.citaRepositorio,
                    app.container.configuracionRepositorio
                )
            }
        }
    }
}

sealed interface AgendaRecepcionEfecto {
    data object IrANuevaCita : AgendaRecepcionEfecto
}
