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
