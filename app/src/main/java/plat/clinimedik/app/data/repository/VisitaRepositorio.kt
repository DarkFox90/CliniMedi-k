package plat.clinimedik.app.data.repository

import androidx.room3.withWriteTransaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import plat.clinimedik.app.data.local.CliniMedikDatabase
import plat.clinimedik.app.data.local.dao.CobroDao
import plat.clinimedik.app.data.local.dao.PacienteDao
import plat.clinimedik.app.data.local.dao.RecetaDao
import plat.clinimedik.app.data.local.dao.ReferenciaDao
import plat.clinimedik.app.data.local.dao.VisitaDao
import plat.clinimedik.app.data.local.entity.CobroEntity
import plat.clinimedik.app.data.local.entity.RecetaEntity
import plat.clinimedik.app.data.local.entity.ReferenciaEntity
import plat.clinimedik.app.data.local.entity.VisitaEntity
import plat.clinimedik.app.data.local.entity.aEntity
import plat.clinimedik.app.data.local.entity.aModelo
import plat.clinimedik.app.data.model.Cobro
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.Receta
import plat.clinimedik.app.data.model.Referencia
import plat.clinimedik.app.data.model.TipoPago
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.data.model.VisitaConPaciente
import plat.clinimedik.app.data.sincronizacion.Sincronizador

class VisitaRepositorio(
    private val database: CliniMedikDatabase,
    private val visitaDao: VisitaDao,
    private val pacienteDao: PacienteDao,
    private val recetaDao: RecetaDao,
    private val referenciaDao: ReferenciaDao,
    private val cobroDao: CobroDao,
    private val sincronizador: Sincronizador
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarFilaDelDia(medicoId: String): Flow<List<VisitaConPaciente>> =
        diaActual().flatMapLatest { hoy -> filaDe(medicoId, hoy) }

    private fun filaDe(medicoId: String, hoy: LocalDate): Flow<List<VisitaConPaciente>> {
        return combine(
            visitaDao.observarDelDia(medicoId, inicioDelDia(hoy), finDelDia(hoy)),
            pacienteDao.observarTodos()
        ) { visitas, pacientes ->
            val pacientesPorId = pacientes.associateBy { it.id }
            visitas
                .mapNotNull { visita ->
                    pacientesPorId[visita.pacienteId]?.let { VisitaConPaciente(visita.aModelo(), it.aModelo()) }
                }
                .sortedWith(
                    compareBy<VisitaConPaciente> { prioridadEnFila(it.visita) }
                        .thenBy { it.visita.horaLlegada }
                )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarVisita(visitaId: String): Flow<VisitaConPaciente?> =
        visitaDao.observarPorId(visitaId).flatMapLatest { visita ->
            if (visita == null) {
                flowOf(null)
            } else {
                pacienteDao.observarPorId(visita.pacienteId).map { paciente ->
                    paciente?.let { VisitaConPaciente(visita.aModelo(), it.aModelo()) }
                }
            }
        }

    fun observarHistorial(pacienteId: String): Flow<List<Visita>> =
        visitaDao.observarDePaciente(pacienteId).map { lista -> lista.map { it.aModelo() } }

    fun observarRecetasDePaciente(pacienteId: String): Flow<List<Receta>> =
        recetaDao.observarDePaciente(pacienteId).map { lista -> lista.map { it.aModelo() } }

    fun observarReferenciasDePaciente(pacienteId: String): Flow<List<Referencia>> =
        referenciaDao.observarDePaciente(pacienteId).map { lista -> lista.map { it.aModelo() } }

    /** La referencia de la visita más reciente del paciente que tenga una. */
    fun observarUltimaReferencia(pacienteId: String): Flow<Referencia?> =
        referenciaDao.observarUltimaDePaciente(pacienteId).map { it?.aModelo() }

    fun observarRecetasDeVisita(visitaId: String): Flow<List<Receta>> =
        recetaDao.observarDeVisita(visitaId).map { lista -> lista.map { it.aModelo() } }

    fun observarReferenciaDeVisita(visitaId: String): Flow<Referencia?> =
        referenciaDao.observarDeVisita(visitaId).map { it?.aModelo() }

    fun observarCobroDeVisita(visitaId: String): Flow<Cobro?> =
        cobroDao.observarDeVisita(visitaId).map { it?.aModelo() }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarCobrosDelDia(medicoId: String): Flow<List<Cobro>> =
        diaActual().flatMapLatest { hoy ->
            cobroDao.observarDelDia(medicoId, inicioDelDia(hoy), finDelDia(hoy))
                .map { lista -> lista.map { it.aModelo() } }
        }

    suspend fun tieneCobro(visitaId: String): Boolean =
        cobroDao.obtenerDeVisita(visitaId) != null

    suspend fun visitaActivaDeHoy(pacienteId: String, medicoId: String): Visita? {
        val hoy = LocalDate.now()
        return visitaDao.obtenerActivaDelDia(pacienteId, medicoId, inicioDelDia(hoy), finDelDia(hoy))?.aModelo()
    }

    suspend fun agregarAFila(pacienteId: String, medicoId: String, motivo: String): String {
        val visita = Visita(
            id = UUID.randomUUID().toString(),
            pacienteId = pacienteId,
            medicoId = medicoId,
            motivoConsulta = motivo.trim(),
            horaLlegada = LocalDateTime.now(),
            estado = EstadoVisita.ESPERANDO
        ).aEntity()
        visitaDao.insertar(visita)
        publicar { sincronizador.publicarVisita(visita) }
        return visita.id
    }

    suspend fun marcarUrgente(visitaId: String, motivo: String) {
        actualizarYPublicar(visitaId) { it.copy(esUrgente = true, motivoPrioridad = motivo.trim()) }
    }

    suspend fun pasarAConsulta(visitaId: String) {
        actualizarYPublicar(visitaId) {
            it.copy(estado = EstadoVisita.EN_CONSULTA, horaInicioConsulta = LocalDateTime.now())
        }
    }

    /**
     * Termina la consulta. Si [diagnostico] o [notas] llegan con texto, se guardan en la visita;
     * si llegan vacíos o `null`, se conserva lo que la visita ya tenía.
     */
    suspend fun finalizarConsulta(visitaId: String, diagnostico: String? = null, notas: String? = null) {
        actualizarYPublicar(visitaId) {
            it.copy(
                estado = EstadoVisita.ATENDIDO,
                horaFin = LocalDateTime.now(),
                diagnostico = diagnostico.textoONulo() ?: it.diagnostico,
                notas = notas.textoONulo() ?: it.notas
            )
        }
    }

    /**
     * Cambia el diagnóstico y las notas de una visita sin cambiar su estado.
     * Se usa en visitas ya terminadas (por ejemplo, referidas). Si [notas] llega vacío, las notas se borran.
     */
    suspend fun guardarDiagnostico(visitaId: String, diagnostico: String, notas: String?) {
        actualizarYPublicar(visitaId) {
            it.copy(
                diagnostico = diagnostico.textoONulo() ?: it.diagnostico,
                notas = notas.textoONulo()
            )
        }
    }

    suspend fun guardarReceta(
        visitaId: String,
        medicamento: String,
        dosis: String,
        frecuencia: String,
        duracion: String,
        indicaciones: String? = null
    ) {
        val receta = RecetaEntity(
            id = UUID.randomUUID().toString(),
            visitaId = visitaId,
            medicamento = medicamento.trim(),
            dosis = dosis.trim(),
            frecuencia = frecuencia.trim(),
            duracion = duracion.trim(),
            indicaciones = indicaciones.textoONulo()
        )
        recetaDao.insertar(receta)
        publicar { sincronizador.publicarReceta(receta) }
    }

    suspend fun guardarReferencia(visitaId: String, especialidad: String, destino: String, motivo: String) {
        val referencia = ReferenciaEntity(
            id = UUID.randomUUID().toString(),
            visitaId = visitaId,
            especialidad = especialidad.trim(),
            destino = destino.trim(),
            motivo = motivo.trim()
        )
        val visita = database.withWriteTransaction<VisitaEntity?> {
            referenciaDao.insertar(referencia)
            actualizarVisita(visitaId) {
                it.copy(estado = EstadoVisita.REFERIDO, horaFin = LocalDateTime.now())
            }
        }
        publicar { sincronizador.publicarReferencia(referencia) }
        if (visita != null) {
            publicar { sincronizador.publicarVisita(visita) }
        }
    }

    /**
     * Registra el cobro de una visita. Los datos del seguro solo se guardan con [TipoPago.SEGURO];
     * en ese caso, si llegan [copago] y [montoAseguradora], el monto es la suma de los dos.
     */
    suspend fun guardarCobro(
        visitaId: String,
        tipoPago: TipoPago,
        monto: Double,
        aseguradora: String? = null,
        numeroAutorizacion: String? = null,
        copago: Double? = null,
        montoAseguradora: Double? = null
    ) {
        val esSeguro = tipoPago == TipoPago.SEGURO
        val cobro = CobroEntity(
            id = UUID.randomUUID().toString(),
            visitaId = visitaId,
            tipoPago = tipoPago,
            monto = if (esSeguro && copago != null && montoAseguradora != null) copago + montoAseguradora else monto,
            aseguradora = if (esSeguro) aseguradora.textoONulo() else null,
            numeroAutorizacion = if (esSeguro) numeroAutorizacion.textoONulo() else null,
            copago = if (esSeguro) copago else null,
            montoAseguradora = if (esSeguro) montoAseguradora else null
        )
        cobroDao.insertar(cobro)
        publicar { sincronizador.publicarCobro(cobro) }
    }

    private suspend fun actualizarYPublicar(visitaId: String, cambio: (VisitaEntity) -> VisitaEntity) {
        val visita = actualizarVisita(visitaId, cambio) ?: return
        publicar { sincronizador.publicarVisita(visita) }
    }

    private suspend fun actualizarVisita(visitaId: String, cambio: (VisitaEntity) -> VisitaEntity): VisitaEntity? {
        val visita = visitaDao.obtenerPorId(visitaId) ?: return null
        val actualizada = cambio(visita)
        visitaDao.actualizar(actualizada)
        return actualizada
    }

    private fun String?.textoONulo(): String? = this?.trim()?.ifBlank { null }

    private fun prioridadEnFila(visita: Visita): Int = when {
        visita.estado == EstadoVisita.ESPERANDO && visita.esUrgente -> 0
        visita.estado == EstadoVisita.ESPERANDO -> 1
        visita.estado == EstadoVisita.EN_CONSULTA -> 2
        visita.estado == EstadoVisita.REFERIDO -> 3
        else -> 4
    }
}
