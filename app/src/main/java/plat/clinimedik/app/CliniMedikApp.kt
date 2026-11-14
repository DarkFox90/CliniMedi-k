package plat.clinimedik.app

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import plat.clinimedik.app.data.model.RolDispositivo
import plat.clinimedik.app.dependencyinjection.AppContainer

class CliniMedikApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.aplicacionScope.launch {
            protegido("No se pudieron cargar los datos iniciales") {
                container.cargaInicial.asegurar()
            }
            protegido("No se pudo iniciar la sincronización") {
                container.sincronizador.iniciar()
            }
            protegido("No se pudieron programar los correos pendientes") {
                // Solo Recepción crea citas, así que solo su teléfono envía correos.
                // El teléfono del médico solo consulta las confirmaciones.
                if (container.configuracionRepositorio.configuracion.first()?.rol == RolDispositivo.RECEPCION) {
                    container.citaRepositorio.reenviarCorreosPendientes()
                }
                container.citaRepositorio.actualizarConfirmaciones()
            }
        }
    }

    private suspend fun protegido(mensaje: String, accion: suspend () -> Unit) {
        try {
            accion()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e("CliniMedikApp", mensaje, error)
        }
    }
}
