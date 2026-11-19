package plat.clinimedik.app.data.repository

import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import com.google.firebase.auth.FirebaseAuth
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.sincronizacion.Sincronizador

/**
 * Cierra la sesión del teléfono: sale de Firebase, olvida la cuenta elegida
 * y borra el rol y el médico. Los datos de la clínica (Room) se conservan.
 */
class SesionRepositorio(
    private val sincronizador: Sincronizador,
    private val firebaseAuth: FirebaseAuth,
    private val credentialManager: CredentialManager,
    private val configuracionRepositorio: ConfiguracionRepositorio
) {

    suspend fun cerrarSesion() {
        sincronizador.detener()
        firebaseAuth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (error: ClearCredentialException) {
            // Puede fallar en un emulador sin Google Play services; la sesión se cierra igual.
            Log.w(ETIQUETA, "No se pudo limpiar la cuenta elegida", error)
        }
        configuracionRepositorio.limpiar()
    }

    private companion object {
        const val ETIQUETA = "SesionRepositorio"
    }
}