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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.model.Cobro
import plat.clinimedik.app.data.model.Receta
import plat.clinimedik.app.data.model.Referencia
import plat.clinimedik.app.data.model.TipoPago
import plat.clinimedik.app.data.model.VisitaConPaciente
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio
import plat.clinimedik.app.ui.navigation.Detalle

class DetalleViewModel(
    savedStateHandle: SavedStateHandle,
    private val visitaRepositorio: VisitaRepositorio,
    private val pacienteRepositorio: PacienteRepositorio,
    private val citaRepositorio: CitaRepositorio,
    private val medicoRepositorio: MedicoRepositorio
) : ViewModel() {

    private val visitaId: String = savedStateHandle.toRoute<Detalle>().visitaId

    private val _state = MutableStateFlow(DetalleUiState())
    val state: StateFlow<DetalleUiState> = _state.asStateFlow()

    private val _efectos = Channel<DetalleEfecto>()
    val efectos: Flow<DetalleEfecto> = _efectos.receiveAsFlow()

    private val reintentos = MutableStateFlow(0)

    init {
        observarVisita()
    }

    fun onEvent(event: DetalleEvent) {
        when (event) {
            DetalleEvent.Regresar -> enviar(DetalleEfecto.Regresar)
            DetalleEvent.Reintentar -> {
                _state.update { it.copy(cargando = true, mensajeError = null) }
                reintentos.update { it + 1 }
            }
            DetalleEvent.PasarAConsulta -> guardar(mensajeExito = null) {
                visitaRepositorio.pasarAConsulta(visitaId)
            }

            DetalleEvent.FinalizarConsulta -> abrirDiagnostico(finalizaConsulta = true)
            DetalleEvent.EditarDiagnostico -> abrirDiagnostico(finalizaConsulta = false)
            is DetalleEvent.CambiarDiagnostico -> actualizarDiagnostico {
                it.copy(diagnostico = event.valor, errorDiagnostico = null)
            }
            is DetalleEvent.CambiarNotas -> actualizarDiagnostico { it.copy(notas = event.valor) }
            DetalleEvent.GuardarDiagnostico -> guardarDiagnostico()

            DetalleEvent.SolicitarUrgencia -> abrirPanel(PanelDetalle.Urgencia())
            is DetalleEvent.CambiarMotivoUrgencia -> actualizarUrgencia { it.copy(motivo = event.motivo, mensajeError = null) }
            DetalleEvent.ConfirmarUrgencia -> confirmarUrgencia()

            DetalleEvent.AbrirReceta -> abrirPanel(PanelDetalle.Receta())
            is DetalleEvent.CambiarMedicamento -> actualizarReceta { it.copy(medicamento = event.valor, errorMedicamento = null) }
            is DetalleEvent.CambiarDosis -> actualizarReceta { it.copy(dosis = event.valor, errorDosis = null) }
            is DetalleEvent.CambiarFrecuencia -> actualizarReceta { it.copy(frecuencia = event.valor, errorFrecuencia = null) }
            is DetalleEvent.CambiarDuracion -> actualizarReceta { it.copy(duracion = event.valor, errorDuracion = null) }
            is DetalleEvent.CambiarIndicaciones -> actualizarReceta { it.copy(indicaciones = event.valor) }
            DetalleEvent.GuardarReceta -> guardarReceta()
            DetalleEvent.CompartirReceta -> _state.value.textoReceta?.let { texto ->
                enviar(DetalleEfecto.CompartirTexto(texto = texto, titulo = "Compartir receta"))
            }

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

            DetalleEvent.AgendarSeguimiento -> _state.value.let { actual ->
                if (actual.pacienteId.isNotBlank()) {
                    enviar(DetalleEfecto.IrANuevaCita(actual.pacienteId, actual.motivoSeguimiento))
                }
            }
            DetalleEvent.AbrirFicha -> _state.value.pacienteId.takeIf { it.isNotBlank() }?.let { pacienteId ->
                enviar(DetalleEfecto.IrAFicha(pacienteId))
            }
            is DetalleEvent.AlternarVisitaAnterior -> _state.update { actual ->
                actual.copy(
                    visitaAnteriorAbiertaId = event.visitaId.takeIf { it != actual.visitaAnteriorAbiertaId }
                )
            }

            DetalleEvent.AdjuntarDocumento -> enviar(DetalleEfecto.MostrarMensaje(MENSAJE_SIGUIENTE_FASE))
            is DetalleEvent.AbrirDocumento -> enviar(DetalleEfecto.MostrarMensaje(MENSAJE_SIGUIENTE_FASE))

            DetalleEvent.CerrarPanel -> _state.update { it.copy(panel = null) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarVisita() {
        viewModelScope.launch {
            reintentos
                .flatMapLatest {
                    visitaRepositorio.observarVisita(visitaId)
                        .flatMapLatest { visitaConPaciente ->
                            if (visitaConPaciente == null) {
                                flowOf<CargaDetalle>(CargaDetalle.NoEncontrada)
                            } else {
                                datosDe(visitaConPaciente)
                            }
                        }
                        .catch { emit(CargaDetalle.Error) }
                }
                .collect { carga ->
                    when (carga) {
                        is CargaDetalle.Lista -> _state.update { actual ->
                            carga.datos.copy(
                                guardando = actual.guardando,
                                visitaAnteriorAbiertaId = actual.visitaAnteriorAbiertaId,
                                panel = actual.panel
                            )
                        }
                        CargaDetalle.NoEncontrada -> {
                            _efectos.send(DetalleEfecto.MostrarMensaje("No se encontró la visita"))
                            _efectos.send(DetalleEfecto.Regresar)
                        }
                        CargaDetalle.Error -> _state.update {
                            it.copy(cargando = false, mensajeError = "No se pudo cargar el expediente")
                        }
                    }
                }
        }
    }

    private fun datosDe(visitaConPaciente: VisitaConPaciente): Flow<CargaDetalle> {
        val pacienteId = visitaConPaciente.paciente.id
        val registros = combine(
            visitaRepositorio.observarRecetasDePaciente(pacienteId),
            visitaRepositorio.observarReferenciasDePaciente(pacienteId),
            visitaRepositorio.observarCobroDeVisita(visitaId)
        ) { recetas, referencias, cobro ->
            RegistrosPaciente(recetas, referencias, cobro)
        }
        return combine(
            pacienteRepositorio.observarDocumentos(pacienteId),
            visitaRepositorio.observarHistorial(pacienteId),
            registros,
            citaRepositorio.observarProximaCita(pacienteId),
            medicoRepositorio.observarMedico(visitaConPaciente.visita.medicoId)
        ) { documentos, historial, registro, proximaCita, medico ->
            CargaDetalle.Lista(
                visitaConPaciente.aDetalleUiState(
                    documentos = documentos,
                    historial = historial,
                    recetas = registro.recetas,
                    referencias = registro.referencias,
                    cobro = registro.cobro,
                    proximaCita = proximaCita,
                    nombreMedico = medico?.nombre.orEmpty(),
                    hoy = LocalDate.now()
                )
            )
        }
    }

    private fun abrirPanel(panel: PanelDetalle) {
        _state.update { it.copy(panel = panel) }
    }

    private fun abrirDiagnostico(finalizaConsulta: Boolean) {
        val actual = _state.value
        abrirPanel(
            PanelDetalle.Diagnostico(
                formulario = FormularioDiagnostico(
                    diagnostico = actual.diagnostico.orEmpty(),
                    notas = actual.notas.orEmpty()
                ),
                finalizaConsulta = finalizaConsulta
            )
        )
    }

    private fun actualizarDiagnostico(cambio: (FormularioDiagnostico) -> FormularioDiagnostico) {
        _state.update { actual ->
            val panel = actual.panel as? PanelDetalle.Diagnostico ?: return@update actual
            actual.copy(panel = panel.copy(formulario = cambio(panel.formulario)))
        }
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

    private fun guardarDiagnostico() {
        val panel = _state.value.panel as? PanelDetalle.Diagnostico ?: return
        val formulario = panel.formulario
        if (formulario.diagnostico.isBlank()) {
            actualizarDiagnostico { it.copy(errorDiagnostico = "Escribe el diagnóstico") }
            return
        }
        if (panel.finalizaConsulta) {
            guardar(mensajeExito = "Consulta finalizada") {
                visitaRepositorio.finalizarConsulta(visitaId, formulario.diagnostico, formulario.notas)
            }
        } else {
            guardar(mensajeExito = "Diagnóstico guardado") {
                visitaRepositorio.guardarDiagnostico(visitaId, formulario.diagnostico, formulario.notas)
            }
        }
    }

    private fun confirmarUrgencia() {
        val panel = _state.value.panel as? PanelDetalle.Urgencia ?: return
        if (panel.motivo.isBlank()) {
            actualizarUrgencia { it.copy(mensajeError = "Escribe el motivo de la prioridad") }
            return
        }
        guardar(mensajeExito = null) {
            visitaRepositorio.marcarUrgente(visitaId, panel.motivo)
        }
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
        guardar(mensajeExito = "Receta registrada") {
            visitaRepositorio.guardarReceta(
                visitaId = visitaId,
                medicamento = formulario.medicamento,
                dosis = formulario.dosis,
                frecuencia = formulario.frecuencia,
                duracion = formulario.duracion,
                indicaciones = formulario.indicaciones
            )
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
        guardar(mensajeExito = "Referencia registrada") {
            visitaRepositorio.guardarReferencia(
                visitaId = visitaId,
                especialidad = formulario.especialidad,
                destino = formulario.destino,
                motivo = formulario.motivo
            )
        }
    }

    private fun abrirCobro() {
        if (_state.value.cobro != null) {
            enviar(DetalleEfecto.MostrarMensaje("Esta visita ya tiene cobro registrado"))
        } else {
            abrirPanel(PanelDetalle.Cobro())
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
        guardar(mensajeExito = "Cobro registrado") {
            visitaRepositorio.guardarCobro(visitaId, tipoPago, monto)
        }
    }

    /**
     * Ejecuta una escritura. Mientras dura, `guardando` queda en `true` y se ignoran toques repetidos.
     * Si sale bien, cierra el panel abierto; si falla, lo deja abierto con los datos escritos y avisa.
     */
    private fun guardar(mensajeExito: String?, accion: suspend () -> Unit) {
        if (_state.value.guardando) return
        _state.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                accion()
                _state.update { it.copy(panel = null) }
                mensajeExito?.let { _efectos.send(DetalleEfecto.MostrarMensaje(it)) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _efectos.send(DetalleEfecto.MostrarMensaje(MENSAJE_ERROR_GUARDAR))
            } finally {
                _state.update { it.copy(guardando = false) }
            }
        }
    }

    private fun errorSiVacio(valor: String): String? =
        if (valor.isBlank()) "Este campo es obligatorio" else null

    private fun enviar(efecto: DetalleEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        private const val MENSAJE_SIGUIENTE_FASE = "Disponible en la siguiente fase"
        private const val MENSAJE_ERROR_GUARDAR = "No se pudo guardar. Intenta de nuevo."

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                DetalleViewModel(
                    createSavedStateHandle(),
                    app.container.visitaRepositorio,
                    app.container.pacienteRepositorio,
                    app.container.citaRepositorio,
                    app.container.medicoRepositorio
                )
            }
        }
    }
}

private data class RegistrosPaciente(
    val recetas: List<Receta>,
    val referencias: List<Referencia>,
    val cobro: Cobro?
)

private sealed interface CargaDetalle {
    data class Lista(val datos: DetalleUiState) : CargaDetalle
    data object NoEncontrada : CargaDetalle
    data object Error : CargaDetalle
}

sealed interface DetalleEfecto {
    data object Regresar : DetalleEfecto
    data class MostrarMensaje(val texto: String) : DetalleEfecto
    data class CompartirTexto(val texto: String, val titulo: String) : DetalleEfecto
    data class IrANuevaCita(val pacienteId: String, val motivo: String) : DetalleEfecto
    data class IrAFicha(val pacienteId: String) : DetalleEfecto
}
