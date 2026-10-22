package plat.clinimedik.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import plat.clinimedik.app.data.local.entity.CitaEntity

@Dao
interface CitaDao {

    @Query("SELECT * FROM cita WHERE medicoId = :medicoId AND fechaHora >= :inicio AND fechaHora < :fin ORDER BY fechaHora")
    fun observarDelDia(medicoId: String, inicio: LocalDateTime, fin: LocalDateTime): Flow<List<CitaEntity>>

    @Query("SELECT * FROM cita WHERE medicoId = :medicoId AND fechaHora >= :inicio AND fechaHora < :fin ORDER BY fechaHora")
    suspend fun obtenerDelDia(medicoId: String, inicio: LocalDateTime, fin: LocalDateTime): List<CitaEntity>

    @Insert
    suspend fun insertar(cita: CitaEntity)

    @Insert
    suspend fun insertarTodos(citas: List<CitaEntity>)

    @Query("DELETE FROM cita")
    suspend fun borrarTodos()
}
