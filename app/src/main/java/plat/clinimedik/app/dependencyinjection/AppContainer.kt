package plat.clinimedik.app.dependencyinjection

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import plat.clinimedik.app.data.local.CargaInicial
import plat.clinimedik.app.data.local.CliniMedikDatabase
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio
import plat.clinimedik.app.data.sincronizacion.Sincronizador
import plat.clinimedik.app.data.sincronizacion.SincronizadorLocal

private val Context.configuracionDataStore: DataStore<Preferences> by preferencesDataStore(name = "configuracion")

class AppContainer(context: Context) {

    private val database = CliniMedikDatabase.crear(context)

    val aplicacionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val sincronizador: Sincronizador = SincronizadorLocal()

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
        sincronizador
    )

    val citaRepositorio = CitaRepositorio(
        database,
        database.citaDao(),
        database.visitaDao(),
        sincronizador
    )

    val configuracionRepositorio = ConfiguracionRepositorio(context.configuracionDataStore)

    val cargaInicial = CargaInicial(database)
}
