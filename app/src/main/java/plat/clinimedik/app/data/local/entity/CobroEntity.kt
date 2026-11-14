package plat.clinimedik.app.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import plat.clinimedik.app.data.model.Cobro
import plat.clinimedik.app.data.model.TipoPago

@Entity(
    tableName = "cobro",
    indices = [Index("visitaId")]
)
data class CobroEntity(
    @PrimaryKey val id: String,
    val visitaId: String,
    val tipoPago: TipoPago,
    val monto: Double,
    val aseguradora: String?,
    val numeroAutorizacion: String?,
    val copago: Double?,
    val montoAseguradora: Double?
)

fun CobroEntity.aModelo(): Cobro = Cobro(
    id = id,
    visitaId = visitaId,
    tipoPago = tipoPago,
    monto = monto,
    aseguradora = aseguradora,
    numeroAutorizacion = numeroAutorizacion,
    copago = copago,
    montoAseguradora = montoAseguradora
)

fun Cobro.aEntity(): CobroEntity = CobroEntity(
    id = id,
    visitaId = visitaId,
    tipoPago = tipoPago,
    monto = monto,
    aseguradora = aseguradora,
    numeroAutorizacion = numeroAutorizacion,
    copago = copago,
    montoAseguradora = montoAseguradora
)
