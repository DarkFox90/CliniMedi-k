package plat.clinimedik.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import plat.clinimedik.app.data.local.entity.RecetaEntity

@Dao
interface RecetaDao {

    @Query("SELECT * FROM receta WHERE visitaId IN (:visitaIds)")
    fun observarDeVisitas(visitaIds: List<String>): Flow<List<RecetaEntity>>

    @Query("SELECT receta.* FROM receta INNER JOIN visita ON receta.visitaId = visita.id WHERE visita.pacienteId = :pacienteId ORDER BY visita.horaLlegada DESC")
    fun observarDePaciente(pacienteId: String): Flow<List<RecetaEntity>>

    @Query("SELECT * FROM receta WHERE visitaId = :visitaId")
    fun observarDeVisita(visitaId: String): Flow<List<RecetaEntity>>

    @Query("SELECT * FROM receta")
    suspend fun obtenerTodos(): List<RecetaEntity>

    @Insert
    suspend fun insertar(receta: RecetaEntity)

    @Upsert
    suspend fun guardar(receta: RecetaEntity)

    @Upsert
    suspend fun guardarTodos(recetas: List<RecetaEntity>)
}
