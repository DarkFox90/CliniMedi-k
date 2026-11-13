package plat.clinimedik.app.dependencyinjection

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import plat.clinimedik.app.BuildConfig
import plat.clinimedik.app.data.local.CargaInicial
import plat.clinimedik.app.data.local.CliniMedikDatabase
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.remote.ServicioCorreos
import plat.clinimedik.app.data.remote.ServicioCorreosDesactivado
import plat.clinimedik.app.data.remote.ServicioCorreosHttp
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.data.repository.NotificacionRepositorio
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio
import plat.clinimedik.app.data.sincronizacion.Sincronizador
import plat.clinimedik.app.data.sincronizacion.SincronizadorLocal

private val Context.configuracionDataStore: DataStore<Preferences> by preferencesDataStore(name = "configuracion")

class AppContainer(context: Context) {

    private val database = CliniMedikDatabase.crear(context)

    val aplicacionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val sincronizador: Sincronizador = SincronizadorLocal()

    /** Sin `CORREOS_URL` o `CORREOS_CLAVE` en `local.properties`, la app funciona igual pero no envía correos. */
    val servicioCorreos: ServicioCorreos =
        if (BuildConfig.CORREOS_URL.isBlank() || BuildConfig.CORREOS_CLAVE.isBlank()) {
            ServicioCorreosDesactivado
        } else {
            ServicioCorreosHttp(BuildConfig.CORREOS_URL, BuildConfig.CORREOS_CLAVE)
        }

    val notificacionRepositorio = NotificacionRepositorio(context.applicationContext, servicioCorreos)

    val medicoRepositorio = MedicoRepositorio(database.medicoDao(), sincronizador)

    val pacienteRepositorio = PacienteRepositorio(
        database,
        database.pacienteDao(),
        database.visitaDao(),
        database.documentoDao(),
        sincronizador
    )

    val visitaRepositorio = VisitaRepositorio(
        database,
        database.visitaDao(),
        database.pacienteDao(),
        database.recetaDao(),
        database.referenciaDao(),
        database.cobroDao(),
        sincronizador,
        notificacionRepositorio
    )

    val citaRepositorio = CitaRepositorio(
        database,
        database.citaDao(),
        database.visitaDao(),
        sincronizador,
        notificacionRepositorio
    )

    val configuracionRepositorio = ConfiguracionRepositorio(context.configuracionDataStore)

    val cargaInicial = CargaInicial(database)
}
