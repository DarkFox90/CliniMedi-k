package plat.clinimedik.app

import android.app.Application
import kotlinx.coroutines.launch
import plat.clinimedik.app.dependencyinjection.AppContainer

class CliniMedikApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.aplicacionScope.launch {
            container.cargaDiaria.asegurar()
        }
    }
}
