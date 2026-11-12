package plat.clinimedik.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import plat.clinimedik.app.data.local.entity.CitaEntity

@Dao
interface CitaDao {

    @Query("SELECT * FROM cita WHERE medicoId = :medicoId AND fechaHora >= :inicio AND fechaHora < :fin ORDER BY fechaHora")
    fun observarDelDia(medicoId: String, inicio: LocalDateTime, fin: LocalDateTime): Flow<List<CitaEntity>>

    @Query("SELECT * FROM cita WHERE medicoId = :medicoId AND fechaHora >= :inicio AND fechaHora < :fin ORDER BY fechaHora")
    suspend fun obtenerDelDia(medicoId: String, inicio: LocalDateTime, fin: LocalDateTime): List<CitaEntity>

    @Query("SELECT * FROM cita WHERE id = :id")
    fun observarPorId(id: String): Flow<CitaEntity?>

    @Query("SELECT * FROM cita WHERE id = :id")
    suspend fun obtenerPorId(id: String): CitaEntity?

    @Query("SELECT * FROM cita WHERE pacienteId = :pacienteId AND estado = 'PROGRAMADA' AND fechaHora >= :desde ORDER BY fechaHora LIMIT 1")
    fun observarProximaDePaciente(pacienteId: String, desde: LocalDateTime): Flow<CitaEntity?>

    @Query("SELECT * FROM cita WHERE medicoId = :medicoId AND fechaHora >= :inicio AND fechaHora < :fin AND estado = 'PROGRAMADA' AND estadoConfirmacion != 'CONFIRMADA' ORDER BY fechaHora")
    fun observarSinConfirmar(medicoId: String, inicio: LocalDateTime, fin: LocalDateTime): Flow<List<CitaEntity>>

    @Query("SELECT * FROM cita")
    suspend fun obtenerTodos(): List<CitaEntity>

    @Insert
    suspend fun insertar(cita: CitaEntity)

    @Update
    suspend fun actualizar(cita: CitaEntity)

    @Upsert
    suspend fun guardar(cita: CitaEntity)

    @Upsert
    suspend fun guardarTodos(citas: List<CitaEntity>)
}
