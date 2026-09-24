package plat.clinimedik.app.data.model

data class Cobro (
    val id: String,
    val visitaId: String,
    val tipoPago: TipoPago,
    val monto: Double
)