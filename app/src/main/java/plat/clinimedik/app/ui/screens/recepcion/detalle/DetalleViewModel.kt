package plat.clinimedik.app.ui.screens.recepcion.detalle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.model.TipoPago
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio
import plat.clinimedik.app.ui.navigation.Detalle

class DetalleViewModel(
    savedStateHandle: SavedStateHandle,
    private val visitaRepositorio: VisitaRepositorio,
    private val pacienteRepositorio: PacienteRepositorio
) : ViewModel() {

    private val visitaId: String = savedStateHandle.toRoute<Detalle>().visitaId

    private val _state = MutableStateFlow(DetalleUiState())
    val state: StateFlow<DetalleUiState> = _state.asStateFlow()

    private val _efectos = Channel<DetalleEfecto>()
    val efectos: Flow<DetalleEfecto> = _efectos.receiveAsFlow()

    init {
        observarVisita()
    }

    fun onEvent(event: DetalleEvent) {
        when (event) {
            DetalleEvent.Regresar -> enviar(DetalleEfecto.Regresar)
            DetalleEvent.PasarAConsulta -> viewModelScope.launch { visitaRepositorio.pasarAConsulta(visitaId) }
            DetalleEvent.FinalizarConsulta -> viewModelScope.launch { visitaRepositorio.finalizarConsulta(visitaId) }

            DetalleEvent.SolicitarUrgencia -> abrirPanel(PanelDetalle.Urgencia())
            is DetalleEvent.CambiarMotivoUrgencia -> actualizarUrgencia { it.copy(motivo = event.motivo, mensajeError = null) }
            DetalleEvent.ConfirmarUrgencia -> confirmarUrgencia()

            DetalleEvent.AbrirReceta -> abrirPanel(PanelDetalle.Receta())
            is DetalleEvent.CambiarMedicamento -> actualizarReceta { it.copy(medicamento = event.valor, errorMedicamento = null) }
            is DetalleEvent.CambiarDosis -> actualizarReceta { it.copy(dosis = event.valor, errorDosis = null) }
            is DetalleEvent.CambiarFrecuencia -> actualizarReceta { it.copy(frecuencia = event.valor, errorFrecuencia = null) }
            is DetalleEvent.CambiarDuracion -> actualizarReceta { it.copy(duracion = event.valor, errorDuracion = null) }
            DetalleEvent.GuardarReceta -> guardarReceta()

            DetalleEvent.AbrirReferencia -> abrirPanel(PanelDetalle.Referencia())
            is DetalleEvent.CambiarEspecialidad -> actualizarReferencia { it.copy(especialidad = event.valor, errorEspecialidad = null) }
            is DetalleEvent.CambiarDestino -> actualizarReferencia { it.copy(destino = event.valor, errorDestino = null) }
            is DetalleEvent.CambiarMotivoReferencia -> actualizarReferencia { it.copy(motivo = event.valor, errorMotivo = null) }
            DetalleEvent.GuardarReferencia -> guardarReferencia()

            DetalleEvent.AbrirCobro -> abrirCobro()
            is DetalleEvent.SeleccionarTipoPago -> actualizarCobro {
                it.copy(
                    tipoPago = event.tipoPago,
                    monto = if (event.tipoPago == TipoPago.CORTESIA) "" else it.monto,
                    errorTipoPago = null,
                    errorMonto = null
                )
            }
            is DetalleEvent.CambiarMonto -> actualizarCobro { it.copy(monto = event.valor, errorMonto = null) }
            DetalleEvent.GuardarCobro -> guardarCobro()

            DetalleEvent.AdjuntarDocumento -> enviar(DetalleEfecto.MostrarMensaje(MENSAJE_SIGUIENTE_FASE))
            is DetalleEvent.AbrirDocumento -> enviar(DetalleEfecto.MostrarMensaje(MENSAJE_SIGUIENTE_FASE))

            DetalleEvent.CerrarPanel -> _state.update { it.copy(panel = null) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarVisita() {
        viewModelScope.launch {
            visitaRepositorio.observarVisita(visitaId)
                .flatMapLatest { visitaConPaciente ->
                    if (visitaConPaciente == null) {
                        flowOf(null)
                    } else {
                        combine(
                            pacienteRepositorio.observarDocumentos(visitaConPaciente.paciente.id),
                            visitaRepositorio.observarHistorial(visitaConPaciente.paciente.id)
                        ) { documentos, historial ->
                            visitaConPaciente.aDetalleUiState(documentos, historial, LocalDate.now())
                        }
                    }
                }
                .collect { datos ->
                    if (datos == null) {
                        enviar(DetalleEfecto.MostrarMensaje("No se encontró la visita"))
                        enviar(DetalleEfecto.Regresar)
                    } else {
                        _state.update { actual -> datos.copy(panel = actual.panel) }
                    }
                }
        }
    }

    private fun abrirPanel(panel: PanelDetalle) {
        _state.update { it.copy(panel = panel) }
    }

    private fun actualizarUrgencia(cambio: (PanelDetalle.Urgencia) -> PanelDetalle.Urgencia) {
        _state.update { actual ->
            val panel = actual.panel as? PanelDetalle.Urgencia ?: return@update actual
            actual.copy(panel = cambio(panel))
        }
    }

    private fun actualizarReceta(cambio: (FormularioReceta) -> FormularioReceta) {
        _state.update { actual ->
            val panel = actual.panel as? PanelDetalle.Receta ?: return@update actual
            actual.copy(panel = PanelDetalle.Receta(cambio(panel.formulario)))
        }
    }

    private fun actualizarReferencia(cambio: (FormularioReferencia) -> FormularioReferencia) {
        _state.update { actual ->
            val panel = actual.panel as? PanelDetalle.Referencia ?: return@update actual
            actual.copy(panel = PanelDetalle.Referencia(cambio(panel.formulario)))
        }
    }

    private fun actualizarCobro(cambio: (FormularioCobro) -> FormularioCobro) {
        _state.update { actual ->
            val panel = actual.panel as? PanelDetalle.Cobro ?: return@update actual
            actual.copy(panel = PanelDetalle.Cobro(cambio(panel.formulario)))
        }
    }

    private fun confirmarUrgencia() {
        val panel = _state.value.panel as? PanelDetalle.Urgencia ?: return
        if (panel.motivo.isBlank()) {
            actualizarUrgencia { it.copy(mensajeError = "Escribe el motivo de la prioridad") }
            return
        }
        _state.update { it.copy(panel = null) }
        viewModelScope.launch { visitaRepositorio.marcarUrgente(visitaId, panel.motivo) }
    }

    private fun guardarReceta() {
        val panel = _state.value.panel as? PanelDetalle.Receta ?: return
        val formulario = panel.formulario
        val validado = formulario.copy(
            errorMedicamento = errorSiVacio(formulario.medicamento),
            errorDosis = errorSiVacio(formulario.dosis),
            errorFrecuencia = errorSiVacio(formulario.frecuencia),
            errorDuracion = errorSiVacio(formulario.duracion)
        )
        if (listOf(validado.errorMedicamento, validado.errorDosis, validado.errorFrecuencia, validado.errorDuracion).any { it != null }) {
            _state.update { it.copy(panel = PanelDetalle.Receta(validado)) }
            return
        }
        _state.update { it.copy(panel = null) }
        viewModelScope.launch {
            visitaRepositorio.guardarReceta(
                visitaId = visitaId,
                medicamento = formulario.medicamento,
                dosis = formulario.dosis,
                frecuencia = formulario.frecuencia,
                duracion = formulario.duracion
            )
            _efectos.send(DetalleEfecto.MostrarMensaje("Receta registrada"))
        }
    }

    private fun guardarReferencia() {
        val panel = _state.value.panel as? PanelDetalle.Referencia ?: return
        val formulario = panel.formulario
        val validado = formulario.copy(
            errorEspecialidad = errorSiVacio(formulario.especialidad),
            errorDestino = errorSiVacio(formulario.destino),
            errorMotivo = errorSiVacio(formulario.motivo)
        )
        if (listOf(validado.errorEspecialidad, validado.errorDestino, validado.errorMotivo).any { it != null }) {
            _state.update { it.copy(panel = PanelDetalle.Referencia(validado)) }
            return
        }
        _state.update { it.copy(panel = null) }
        viewModelScope.launch {
            visitaRepositorio.guardarReferencia(
                visitaId = visitaId,
                especialidad = formulario.especialidad,
                destino = formulario.destino,
                motivo = formulario.motivo
            )
            _efectos.send(DetalleEfecto.MostrarMensaje("Referencia registrada"))
        }
    }

    private fun abrirCobro() {
        viewModelScope.launch {
            if (visitaRepositorio.tieneCobro(visitaId)) {
                _efectos.send(DetalleEfecto.MostrarMensaje("Esta visita ya tiene cobro registrado"))
            } else {
                abrirPanel(PanelDetalle.Cobro())
            }
        }
    }

    private fun guardarCobro() {
        val panel = _state.value.panel as? PanelDetalle.Cobro ?: return
        val formulario = panel.formulario
        val tipoPago = formulario.tipoPago
        if (tipoPago == null) {
            actualizarCobro { it.copy(errorTipoPago = "Selecciona el tipo de pago") }
            return
        }
        val monto = if (tipoPago == TipoPago.CORTESIA) {
            0.0
        } else {
            formulario.monto.trim().replace(",", "").toDoubleOrNull()?.takeIf { it > 0 }
        }
        if (monto == null) {
            actualizarCobro { it.copy(errorMonto = "Ingresa un monto mayor a 0") }
            return
        }
        _state.update { it.copy(panel = null) }
        viewModelScope.launch {
            visitaRepositorio.guardarCobro(visitaId, tipoPago, monto)
            _efectos.send(DetalleEfecto.MostrarMensaje("Cobro registrado"))
        }
    }

    private fun errorSiVacio(valor: String): String? =
        if (valor.isBlank()) "Este campo es obligatorio" else null

    private fun enviar(efecto: DetalleEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        private const val MENSAJE_SIGUIENTE_FASE = "Disponible en la siguiente fase"

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                DetalleViewModel(
                    createSavedStateHandle(),
                    app.container.visitaRepositorio,
                    app.container.pacienteRepositorio
                )
            }
        }
    }
}

sealed interface DetalleEfecto {
    data object Regresar : DetalleEfecto
    data class MostrarMensaje(val texto: String) : DetalleEfecto
}
