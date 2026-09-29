package plat.clinimedik.app.ui.screens.setup.rol

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import plat.clinimedik.app.data.model.RolDispositivo
import plat.clinimedik.app.ui.components.etiqueta

private fun RolDispositivo.aOpcionRol(): OpcionRol = OpcionRol(
    rol = this,
    titulo = etiqueta(),
    descripcion = when (this) {
        RolDispositivo.RECEPCION -> "Registra pacientes y administra la fila, las citas y los cobros"
        RolDispositivo.MEDICO -> "Consulta la fila, la agenda y el historial de tus pacientes, en modo de solo lectura"
    }
)

fun rolUiStateDePrueba(rolSeleccionado: RolDispositivo? = null): RolUiState = RolUiState(
    opciones = RolDispositivo.entries.map { it.aOpcionRol() },
    rolSeleccionado = rolSeleccionado
)

@Composable
fun RolRoute() {
    val state = remember { rolUiStateDePrueba() }
    RolScreen(state = state, onIntent = {})
}
