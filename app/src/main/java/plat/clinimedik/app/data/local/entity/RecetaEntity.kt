package plat.clinimedik.app.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import plat.clinimedik.app.data.model.Receta

@Entity(
    tableName = "receta",
    indices = [Index("visitaId")]
)
data class RecetaEntity(
    @PrimaryKey val id: String,
    val visitaId: String,
    val medicamento: String,
    val dosis: String,
    val frecuencia: String,
    val duracion: String
)

fun RecetaEntity.aModelo(): Receta = Receta(
    id = id,
    visitaId = visitaId,
    medicamento = medicamento,
    dosis = dosis,
    frecuencia = frecuencia,
    duracion = duracion
)

fun Receta.aEntity(): RecetaEntity = RecetaEntity(
    id = id,
    visitaId = visitaId,
    medicamento = medicamento,
    dosis = dosis,
    frecuencia = frecuencia,
    duracion = duracion
)
