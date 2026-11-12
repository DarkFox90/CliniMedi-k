package plat.clinimedik.app.data.model

data class Cobro (
    val id: String,
    val visitaId: String,
    val tipoPago: TipoPago,
    val monto: Double,
    val aseguradora: String? = null,
    val numeroAutorizacion: String? = null,
    val copago: Double? = null,
    val montoAseguradora: Double? = null
)