package plat.clinimedik.app.ui.screens.setup.login

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import plat.clinimedik.app.R
import plat.clinimedik.app.ui.navigation.Login
import plat.clinimedik.app.ui.navigation.Rol

@Composable
fun LoginRoute(
    onIrARol: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                LoginEfecto.AbrirSelectorGoogle -> {
                    val resultado = if (activity != null) {
                        pedirTokenDeGoogle(activity)
                    } else {
                        LoginEvent.ErrorGoogle(MENSAJE_ERROR_LOGIN)
                    }
                    viewModel.onEvent(resultado)
                }
                LoginEfecto.IrARol -> onIrARol()
            }
        }
    }
    LoginScreen(state = state, onEvent = viewModel::onEvent)
}

private suspend fun pedirTokenDeGoogle(activity: Activity): LoginEvent {
    val opcionGoogle = GetSignInWithGoogleOption.Builder(
        serverClientId = activity.getString(R.string.default_web_client_id)
    ).build()
    val solicitud = GetCredentialRequest.Builder()
        .addCredentialOption(opcionGoogle)
        .build()
    return try {
        val respuesta = CredentialManager.create(activity).getCredential(
            context = activity,
            request = solicitud
        )
        val credencial = respuesta.credential
        if (credencial is CustomCredential &&
            credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            LoginEvent.TokenRecibido(GoogleIdTokenCredential.createFrom(credencial.data).idToken)
        } else {
            LoginEvent.ErrorGoogle(MENSAJE_ERROR_LOGIN)
        }
    } catch (error: GetCredentialCancellationException) {
        LoginEvent.ErrorGoogle("Se canceló el inicio de sesión")
    } catch (error: GetCredentialException) {
        LoginEvent.ErrorGoogle(MENSAJE_ERROR_LOGIN)
    } catch (error: GoogleIdTokenParsingException) {
        LoginEvent.ErrorGoogle(MENSAJE_ERROR_LOGIN)
    }
}

fun NavGraphBuilder.loginDestino(navController: NavController) {
    composable<Login> {
        LoginRoute(
            onIrARol = {
                navController.navigate(Rol) {
                    popUpTo<Login> { inclusive = true }
                }
            }
        )
    }
}