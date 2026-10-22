package plat.clinimedik.app.data.local

import androidx.room3.withWriteTransaction
import java.time.LocalDate
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.data.local.entity.MetadatoEntity
import plat.clinimedik.app.data.local.entity.aEntity

class CargaDiaria(private val database: CliniMedikDatabase) {

    suspend fun asegurar() {
        database.withWriteTransaction {
            val ultimaCarga = database.metadatoDao().obtener()?.fechaCarga
            if (ultimaCarga != LocalDate.now()) {
                recargar()
            }
        }
    }

    private suspend fun recargar() {
        database.recetaDao().borrarTodos()
        database.referenciaDao().borrarTodos()
        database.cobroDao().borrarTodos()
        database.documentoDao().borrarTodos()
        database.citaDao().borrarTodos()
        database.visitaDao().borrarTodos()
        database.pacienteDao().borrarTodos()

        database.medicoDao().guardarTodos(FakeDataSource.medicos.map { it.aEntity() })
        database.pacienteDao().insertarTodos(FakeDataSource.pacientes.map { it.aEntity() })
        database.visitaDao().insertarTodos(FakeDataSource.visitas.map { it.aEntity() })
        database.recetaDao().insertarTodos(FakeDataSource.recetas.map { it.aEntity() })
        database.referenciaDao().insertarTodos(FakeDataSource.referencias.map { it.aEntity() })
        database.cobroDao().insertarTodos(FakeDataSource.cobros.map { it.aEntity() })
        database.documentoDao().insertarTodos(FakeDataSource.documentos.map { it.aEntity() })
        database.citaDao().insertarTodos(FakeDataSource.citas.map { it.aEntity() })

        database.metadatoDao().guardar(MetadatoEntity(fechaCarga = FakeDataSource.hoy))
    }
}
