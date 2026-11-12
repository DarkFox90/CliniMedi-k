package plat.clinimedik.app

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
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
