package plat.clinimedik.app.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import plat.clinimedik.app.data.model.Referencia

@Entity(
    tableName = "referencia",
    indices = [Index("visitaId")]
)
data class ReferenciaEntity(
    @PrimaryKey val id: String,
    val visitaId: String,
    val especialidad: String,
    val destino: String,
    val motivo: String
)

fun ReferenciaEntity.aModelo(): Referencia = Referencia(
    id = id,
    visitaId = visitaId,
    especialidad = especialidad,
    destino = destino,
    motivo = motivo
)

fun Referencia.aEntity(): ReferenciaEntity = ReferenciaEntity(
    id = id,
    visitaId = visitaId,
    especialidad = especialidad,
    destino = destino,
    motivo = motivo
)
