package plat.clinimedik.app.ui.screens.recepcion.inicio

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
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio

class InicioRecepcionViewModel(
    private val visitaRepositorio: VisitaRepositorio,
    private val citaRepositorio: CitaRepositorio,
    private val medicoRepositorio: MedicoRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(InicioRecepcionUiState())
    val state: StateFlow<InicioRecepcionUiState> = _state.asStateFlow()

    private val _efectos = Channel<InicioRecepcionEfecto>()
    val efectos: Flow<InicioRecepcionEfecto> = _efectos.receiveAsFlow()

    init {
        observarInicio()
    }

    fun onEvent(event: InicioRecepcionEvent) {
        when (event) {
            is InicioRecepcionEvent.AbrirPaciente -> enviar(InicioRecepcionEfecto.AbrirDetalle(event.visitaId))
            InicioRecepcionEvent.NuevoPaciente -> enviar(InicioRecepcionEfecto.IrARegistro)
            InicioRecepcionEvent.EscanearQr -> enviar(InicioRecepcionEfecto.IrAEscaneo)
            InicioRecepcionEvent.VerResumen -> enviar(InicioRecepcionEfecto.IrAResumen)
            is InicioRecepcionEvent.AbrirCita -> enviar(InicioRecepcionEfecto.IrAAgenda)
            InicioRecepcionEvent.CambiarConfiguracion -> enviar(InicioRecepcionEfecto.IrARol)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarInicio() {
        viewModelScope.launch {
            configuracionRepositorio.configuracion
                .filterNotNull()
                .flatMapLatest { configuracion ->
                    combine(
                        visitaRepositorio.observarFilaDelDia(configuracion.medicoId),
                        citaRepositorio.observarCitasDelDia(configuracion.medicoId),
                        medicoRepositorio.observarMedico(configuracion.medicoId)
                    ) { fila, citas, medico ->
                        calcularInicio(fila, citas, medico?.nombre.orEmpty(), LocalDateTime.now())
                    }
                }
                .collect { inicio -> _state.value = inicio }
        }
    }

    private fun enviar(efecto: InicioRecepcionEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                InicioRecepcionViewModel(
                    app.container.visitaRepositorio,
                    app.container.citaRepositorio,
                    app.container.medicoRepositorio,
                    app.container.configuracionRepositorio
                )
            }
        }
    }
}

sealed interface InicioRecepcionEfecto {
    data class AbrirDetalle(val visitaId: String) : InicioRecepcionEfecto
    data object IrARegistro : InicioRecepcionEfecto
    data object IrAEscaneo : InicioRecepcionEfecto
    data object IrAResumen : InicioRecepcionEfecto
    data object IrAAgenda : InicioRecepcionEfecto
    data object IrARol : InicioRecepcionEfecto
}