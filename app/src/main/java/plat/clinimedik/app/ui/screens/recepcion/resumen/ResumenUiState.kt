package plat.clinimedik.app.ui.screens.recepcion.resumen

data class CobroPorTipo(
    val tipo: String,
    val monto: String
)

data class BarraAfluencia(
    val hora: String,
    val llegadas: Int,
    val proporcion: Float
)

data class ResumenUiState(
    val subtitulo: String = "",
    val atendidos: Int = 0,
    val tiempoPromedio: String = "",
    val nuevos: Int = 0,
    val recurrentes: Int = 0,
    val cobrosPorTipo: List<CobroPorTipo> = emptyList(),
    val totalDelDia: String = "",
    val pendientesDeCobro: List<String> = emptyList(),
    val afluencia: List<BarraAfluencia> = emptyList()
) {
    val textoAfluencia: String
        get() {
            val pico = afluencia.maxByOrNull { it.llegadas }
            return if (pico == null || pico.llegadas == 0) {
                "Sin llegadas registradas hoy"
            } else {
                val llegadas = if (pico.llegadas == 1) "1 llegada" else "${pico.llegadas} llegadas"
                "Mayor afluencia: ${pico.hora} · $llegadas"
            }
        }
}
