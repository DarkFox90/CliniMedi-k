package plat.clinimedik.app.ui.screens.recepcion.resumen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio

class ResumenViewModel(
    private val visitaRepositorio: VisitaRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio,
    private val medicoRepositorio: MedicoRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(ResumenUiState())
    val state: StateFlow<ResumenUiState> = _state.asStateFlow()

    private val _efectos = Channel<ResumenEfecto>()
    val efectos: Flow<ResumenEfecto> = _efectos.receiveAsFlow()

    init {
        observarResumen()
    }

    fun onEvent(event: ResumenEvent) {
        when (event) {
            ResumenEvent.Regresar -> viewModelScope.launch { _efectos.send(ResumenEfecto.Regresar) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarResumen() {
        viewModelScope.launch {
            configuracionRepositorio.configuracion
                .filterNotNull()
                .flatMapLatest { configuracion ->
                    combine(
                        visitaRepositorio.observarFilaDelDia(configuracion.medicoId),
                        visitaRepositorio.observarCobrosDelDia(configuracion.medicoId),
                        medicoRepositorio.observarMedico(configuracion.medicoId)
                    ) { fila, cobros, medico ->
                        calcularResumen(fila, cobros, medico?.nombre.orEmpty(), LocalDate.now())
                    }
                }
                .collect { resumen -> _state.value = resumen }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                ResumenViewModel(
                    app.container.visitaRepositorio,
                    app.container.configuracionRepositorio,
                    app.container.medicoRepositorio
                )
            }
        }
    }
}

sealed interface ResumenEfecto {
    data object Regresar : ResumenEfecto
}
