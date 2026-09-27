package plat.clinimedik.app.ui.screens.recepcion.resultado

sealed interface ResultadoIntent {
    data object VerHistorial : ResultadoIntent
    data object EscanearOtro : ResultadoIntent
    data object Regresar : ResultadoIntent
}