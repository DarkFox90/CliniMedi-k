package plat.clinimedik.app.ui.screens.setup.login

sealed interface LoginIntent {
    data object ContinuarConGoogle : LoginIntent
}
