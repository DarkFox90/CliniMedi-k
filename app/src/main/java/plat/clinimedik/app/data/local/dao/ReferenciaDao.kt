package plat.clinimedik.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import plat.clinimedik.app.data.local.entity.ReferenciaEntity

@Dao
interface ReferenciaDao {

    @Query("SELECT * FROM referencia WHERE visitaId IN (:visitaIds)")
    fun observarDeVisitas(visitaIds: List<String>): Flow<List<ReferenciaEntity>>

    @Query("SELECT referencia.* FROM referencia INNER JOIN visita ON referencia.visitaId = visita.id WHERE visita.pacienteId = :pacienteId ORDER BY visita.horaLlegada DESC")
    fun observarDePaciente(pacienteId: String): Flow<List<ReferenciaEntity>>

    @Query("SELECT * FROM referencia WHERE visitaId = :visitaId LIMIT 1")
    fun observarDeVisita(visitaId: String): Flow<ReferenciaEntity?>

    @Query("SELECT referencia.* FROM referencia INNER JOIN visita ON referencia.visitaId = visita.id WHERE visita.pacienteId = :pacienteId ORDER BY visita.horaLlegada DESC LIMIT 1")
    fun observarUltimaDePaciente(pacienteId: String): Flow<ReferenciaEntity?>

    @Query("SELECT * FROM referencia")
    suspend fun obtenerTodos(): List<ReferenciaEntity>

    @Insert
    suspend fun insertar(referencia: ReferenciaEntity)

    @Upsert
    suspend fun guardar(referencia: ReferenciaEntity)

    @Upsert
    suspend fun guardarTodos(referencias: List<ReferenciaEntity>)
}
