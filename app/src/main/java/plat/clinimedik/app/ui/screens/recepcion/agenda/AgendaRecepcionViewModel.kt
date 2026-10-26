
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.ui.components.formatearFechaLarga
import plat.clinimedik.app.ui.components.formatearHora
import java.time.LocalDate

class AgendaRecepcionViewModel(
    private val citaRepositorio: CitaRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(AgendaRecepcionUiState())
    val state: StateFlow<AgendaRecepcionUiState> = _state.asStateFlow()

    private val _efectos = Channel<AgendaRecepcionEfecto>()
    val efectos: Flow<AgendaRecepcionEfecto> = _efectos.receiveAsFlow()

    init {
        observarCitas()
    }

    fun onEvent(event: AgendaRecepcionEvent) {
        when (event) {
            AgendaRecepcionEvent.NuevaCita -> {
                viewModelScope.launch {
                    _efectos.send(AgendaRecepcionEfecto.IrANuevaCita)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarCitas() {
        viewModelScope.launch {
            configuracionRepositorio.configuracion
                .filterNotNull()
                .flatMapLatest { configuracion ->
                    citaRepositorio.observarCitasDelDia(configuracion.medicoId)
                }
                .collect { citas ->
                    _state.update {
                        it.copy(
                            cargando = false,
                            subtitulo = formatearFechaLarga(LocalDate.now()),
                            elementos = citas.map { cita ->
                                ElementoCita(
                                    citaId = cita.id,
                                    hora = formatearHora(cita.fechaHora),
                                    nombrePaciente = cita.nombrePaciente,
                                    motivo = cita.motivo,
                                    estadoConfirmacion = cita.estadoConfirmacion,
                                    recordatorioEnviado = cita.recordatorioEnviado
                                )
                            }
                        )
                    }
                }
        }
    }

    companion object {
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
