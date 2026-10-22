package plat.clinimedik.app.ui.screens.medico.agenda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.AgendaMedico

@Composable
fun AgendaMedicoRoute() {
    val state = remember {
        AgendaMedicoUiState(
            citas = listOf(
                Cita("1", "Lucía Ajú", "08:30 a. m.", "Control de rutina"),
                Cita("2", "Álvaro Chacón", "09:00 a. m.", "Lectura de exámenes"),
                Cita("3", "María Fernanda Pérez", "10:00 a. m.", "Consulta general")
            )
        )
    }
    AgendaMedicoScreen(state = state, onEvent = {})
}

fun NavGraphBuilder.agendaMedicoDestino(navController: NavController) {
    composable<AgendaMedico> {
        AgendaMedicoRoute()
    }
}
