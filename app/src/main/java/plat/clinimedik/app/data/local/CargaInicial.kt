package plat.clinimedik.app.data.local

import androidx.room3.withWriteTransaction
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.data.local.entity.MetadatoEntity
import plat.clinimedik.app.data.local.entity.aEntity

/**
 * Inserta los datos de prueba una sola vez, cuando la base está recién creada.
 * Después no borra ni reemplaza nada: lo que registra el usuario se conserva.
 * La fila de [MetadatoEntity] marca que la carga ya se hizo.
 */
class CargaInicial(private val database: CliniMedikDatabase) {

    suspend fun asegurar() {
        if (database.metadatoDao().obtener() != null) return
        database.withWriteTransaction {
            if (database.metadatoDao().obtener() == null) {
                cargar()
            }
        }
    }

    private suspend fun cargar() {
        database.medicoDao().guardarTodos(FakeDataSource.medicos.map { it.aEntity() })
        database.pacienteDao().guardarTodos(FakeDataSource.pacientes.map { it.aEntity() })
        database.visitaDao().guardarTodos(FakeDataSource.visitas.map { it.aEntity() })
        database.recetaDao().guardarTodos(FakeDataSource.recetas.map { it.aEntity() })
        database.referenciaDao().guardarTodos(FakeDataSource.referencias.map { it.aEntity() })
        database.cobroDao().guardarTodos(FakeDataSource.cobros.map { it.aEntity() })
        database.documentoDao().guardarTodos(FakeDataSource.documentos.map { it.aEntity() })
        database.citaDao().guardarTodos(FakeDataSource.citas.map { it.aEntity() })

        database.metadatoDao().guardar(MetadatoEntity(fechaCarga = FakeDataSource.hoy))
    }
}
