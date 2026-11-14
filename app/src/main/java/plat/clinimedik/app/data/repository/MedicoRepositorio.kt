package plat.clinimedik.app.data.repository

import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plat.clinimedik.app.data.local.dao.MedicoDao
import plat.clinimedik.app.data.local.entity.MedicoEntity
import plat.clinimedik.app.data.local.entity.aModelo
import plat.clinimedik.app.data.model.Medico
import plat.clinimedik.app.data.sincronizacion.Sincronizador

class MedicoRepositorio(
    private val medicoDao: MedicoDao,
    private val sincronizador: Sincronizador
) {

    fun observarMedicos(): Flow<List<Medico>> =
        medicoDao.observarTodos().map { lista -> lista.map { it.aModelo() } }

    fun observarMedico(id: String): Flow<Medico?> =
        medicoDao.observarPorId(id).map { it?.aModelo() }

    suspend fun crear(nombre: String, especialidad: String): String {
        val medico = MedicoEntity(
            id = UUID.randomUUID().toString(),
            nombre = nombre.trim(),
            especialidad = especialidad.trim()
        )
        medicoDao.insertar(medico)
        publicar { sincronizador.publicarMedico(medico) }
        return medico.id
    }
}
