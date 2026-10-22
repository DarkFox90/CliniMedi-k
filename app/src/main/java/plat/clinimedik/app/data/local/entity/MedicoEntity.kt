package plat.clinimedik.app.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import plat.clinimedik.app.data.model.Medico

@Entity(
    tableName = "medico"
)
data class MedicoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val especialidad: String
)

fun MedicoEntity.aModelo(): Medico = Medico(
    id = id,
    nombre = nombre,
    especialidad = especialidad
)

fun Medico.aEntity(): MedicoEntity = MedicoEntity(
    id = id,
    nombre = nombre,
    especialidad = especialidad
)
