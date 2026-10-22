package plat.clinimedik.app.dependencyinjection

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import plat.clinimedik.app.data.local.CargaDiaria
import plat.clinimedik.app.data.local.CliniMedikDatabase
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio

private val Context.configuracionDataStore: DataStore<Preferences> by preferencesDataStore(name = "configuracion")

class AppContainer(context: Context) {

    private val database = CliniMedikDatabase.crear(context)

    val aplicacionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val medicoRepositorio = MedicoRepositorio(database.medicoDao())

    val pacienteRepositorio = PacienteRepositorio(
        database,
        database.pacienteDao(),
        database.visitaDao(),
        database.documentoDao()
    )

    val visitaRepositorio = VisitaRepositorio(
        database,
        database.visitaDao(),
        database.pacienteDao(),
        database.recetaDao(),
        database.referenciaDao(),
        database.cobroDao()
    )

    val citaRepositorio = CitaRepositorio(database.citaDao())

    val configuracionRepositorio = ConfiguracionRepositorio(context.configuracionDataStore)

    val cargaDiaria = CargaDiaria(database)
}
