package plat.clinimedik.app.data.repository

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plat.clinimedik.app.data.local.dao.CitaDao
import plat.clinimedik.app.data.local.entity.aEntity
import plat.clinimedik.app.data.local.entity.aModelo
import plat.clinimedik.app.data.model.Cita

class CitaRepositorio(private val citaDao: CitaDao) {

    fun observarCitasDelDia(medicoId: String, fecha: LocalDate = LocalDate.now()): Flow<List<Cita>> =
        citaDao.observarDelDia(medicoId, inicioDelDia(fecha), finDelDia(fecha))
            .map { lista -> lista.map { it.aModelo() } }

    suspend fun buscarConflicto(medicoId: String, fechaHora: LocalDateTime, duracionMinutos: Int = 30): Cita? {
        val fecha = fechaHora.toLocalDate()
        val finNueva = fechaHora.plusMinutes(duracionMinutos.toLong())
        return citaDao.obtenerDelDia(medicoId, inicioDelDia(fecha), finDelDia(fecha))
            .map { it.aModelo() }
            .firstOrNull { cita ->
                val finExistente = cita.fechaHora.plusMinutes(cita.duracionMinutos.toLong())
                cita.fechaHora < finNueva && fechaHora < finExistente
            }
    }

    suspend fun crear(cita: Cita): String {
        val id = UUID.randomUUID().toString()
        citaDao.insertar(cita.copy(id = id).aEntity())
        return id
    }
}
