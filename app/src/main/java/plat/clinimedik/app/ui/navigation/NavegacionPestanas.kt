package plat.clinimedik.app.ui.navigation

import androidx.navigation.NavController

fun NavController.irAPestana(pestana: Any) {
    navigate(pestana) {
        popUpTo(graph.id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/** Borra el estado guardado de las pestañas para que no vuelva a aparecer otro rol o médico. */
fun NavController.borrarHistorialDePestanas() {
    clearBackStack<Inicio>()
    clearBackStack<Fila>()
    clearBackStack<AgendaRecepcion>()
    clearBackStack<Buscar>()
    clearBackStack<FilaMedico>()
    clearBackStack<AgendaMedico>()
    clearBackStack<PacientesMedico>()
}

/** Después de cerrar sesión: Login queda como única pantalla y Atrás cierra la app. */
fun NavController.irALoginSinHistorial() {
    borrarHistorialDePestanas()
    navigate(Login) {
        popUpTo(graph.id) { inclusive = true }
    }
}