package plat.clinimedik.app.ui.screens.setup.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.R
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun LoginScreen(
    state: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text(
                text = "CliniMedi-k",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Inicia sesión con la cuenta de Google de la clínica",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            if (state.mensajeError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.mensajeError,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            BotonGoogle(
                cargando = state.cargando,
                onClick = { onIntent(LoginIntent.ContinuarConGoogle) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "La configuración se realiza una sola vez por dispositivo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BotonGoogle(
    cargando: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !cargando,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier
    ) {
        if (cargando) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = if (cargando) "Iniciando sesión…" else "Continuar con Google")
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginNormalPreview() {
    CliniMedikTheme {
        LoginScreen(state = LoginUiState(), onIntent = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginCargandoPreview() {
    CliniMedikTheme {
        LoginScreen(state = LoginUiState(cargando = true), onIntent = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginErrorPreview() {
    CliniMedikTheme {
        LoginScreen(
            state = LoginUiState(
                mensajeError = "No se pudo iniciar sesión con Google. Revisa tu conexión e intenta de nuevo."
            ),
            onIntent = {}
        )
    }
}