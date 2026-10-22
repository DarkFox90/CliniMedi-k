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

class VisitaRepositorio(
    private val database: CliniMedikDatabase,
    private val visitaDao: VisitaDao,
    private val pacienteDao: PacienteDao,
    private val recetaDao: RecetaDao,
    private val referenciaDao: ReferenciaDao,
    private val cobroDao: CobroDao
) {

    fun observarFilaDelDia(medicoId: String): Flow<List<VisitaConPaciente>> {
        val hoy = LocalDate.now()
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

    fun observarCobrosDelDia(medicoId: String): Flow<List<Cobro>> {
        val hoy = LocalDate.now()
        return cobroDao.observarDelDia(medicoId, inicioDelDia(hoy), finDelDia(hoy))
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
        )
        visitaDao.insertar(visita.aEntity())
        return visita.id
    }

    suspend fun marcarUrgente(visitaId: String, motivo: String) {
        actualizarVisita(visitaId) { it.copy(esUrgente = true, motivoPrioridad = motivo.trim()) }
    }

    suspend fun pasarAConsulta(visitaId: String) {
        actualizarVisita(visitaId) {
            it.copy(estado = EstadoVisita.EN_CONSULTA, horaInicioConsulta = LocalDateTime.now())
        }
    }

    suspend fun finalizarConsulta(visitaId: String) {
        actualizarVisita(visitaId) {
            it.copy(estado = EstadoVisita.ATENDIDO, horaFin = LocalDateTime.now())
        }
    }

    suspend fun guardarReceta(
        visitaId: String,
        medicamento: String,
        dosis: String,
        frecuencia: String,
        duracion: String
    ) {
        recetaDao.insertar(
            RecetaEntity(
                id = UUID.randomUUID().toString(),
                visitaId = visitaId,
                medicamento = medicamento.trim(),
                dosis = dosis.trim(),
                frecuencia = frecuencia.trim(),
                duracion = duracion.trim()
            )
        )
    }

    suspend fun guardarReferencia(visitaId: String, especialidad: String, destino: String, motivo: String) {
        database.withWriteTransaction {
            referenciaDao.insertar(
                ReferenciaEntity(
                    id = UUID.randomUUID().toString(),
                    visitaId = visitaId,
                    especialidad = especialidad.trim(),
                    destino = destino.trim(),
                    motivo = motivo.trim()
                )
            )
            actualizarVisita(visitaId) {
                it.copy(estado = EstadoVisita.REFERIDO, horaFin = LocalDateTime.now())
            }
        }
    }

    suspend fun guardarCobro(visitaId: String, tipoPago: TipoPago, monto: Double) {
        cobroDao.insertar(
            CobroEntity(
                id = UUID.randomUUID().toString(),
                visitaId = visitaId,
                tipoPago = tipoPago,
                monto = monto
            )
        )
    }

    private suspend fun actualizarVisita(visitaId: String, cambio: (VisitaEntity) -> VisitaEntity) {
        val visita = visitaDao.obtenerPorId(visitaId) ?: return
        visitaDao.actualizar(cambio(visita))
    }

    private fun prioridadEnFila(visita: Visita): Int = when {
        visita.estado == EstadoVisita.ESPERANDO && visita.esUrgente -> 0
        visita.estado == EstadoVisita.ESPERANDO -> 1
        visita.estado == EstadoVisita.EN_CONSULTA -> 2
        visita.estado == EstadoVisita.REFERIDO -> 3
        else -> 4
    }
}
