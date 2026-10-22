package plat.clinimedik.app.ui.screens.setup.login

sealed interface LoginEvent {
    data object ContinuarConGoogle : LoginEvent
}
