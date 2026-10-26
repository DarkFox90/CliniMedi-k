package plat.clinimedik.app.ui.screens.medico.fila

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDateTime
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

class FilaMedicoViewModel(
    private val visitaRepositorio: VisitaRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio,
    private val medicoRepositorio: MedicoRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(FilaMedicoUiState())
    val state: StateFlow<FilaMedicoUiState> = _state.asStateFlow()

    private val _efectos = Channel<FilaMedicoEfecto>()
    val efectos: Flow<FilaMedicoEfecto> = _efectos.receiveAsFlow()

    init {
        observarFila()
    }

    fun onEvent(event: FilaMedicoEvent) {
        when (event) {
            is FilaMedicoEvent.AbrirHistorial -> enviar(FilaMedicoEfecto.AbrirHistorial(event.pacienteId))
            FilaMedicoEvent.CambiarConfiguracion -> enviar(FilaMedicoEfecto.IrARol)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarFila() {
        viewModelScope.launch {
            configuracionRepositorio.configuracion
                .filterNotNull()
                .flatMapLatest { configuracion ->
                    combine(
                        visitaRepositorio.observarFilaDelDia(configuracion.medicoId),
                        medicoRepositorio.observarMedico(configuracion.medicoId)
                    ) { fila, medico ->
                        calcularFilaMedico(fila, medico?.nombre.orEmpty(), LocalDateTime.now())
                    }
                }
                .collect { fila -> _state.value = fila }
        }
    }

    private fun enviar(efecto: FilaMedicoEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                FilaMedicoViewModel(
                    app.container.visitaRepositorio,
                    app.container.configuracionRepositorio,
                    app.container.medicoRepositorio
                )
            }
        }
    }
}

sealed interface FilaMedicoEfecto {
    data class AbrirHistorial(val pacienteId: String) : FilaMedicoEfecto
    data object IrARol : FilaMedicoEfecto
}