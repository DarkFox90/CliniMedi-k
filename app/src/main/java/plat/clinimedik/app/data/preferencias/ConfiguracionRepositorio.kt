package plat.clinimedik.app.data.preferencias

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import plat.clinimedik.app.data.model.RolDispositivo

class ConfiguracionRepositorio(private val dataStore: DataStore<Preferences>) {

    val configuracion: Flow<ConfiguracionDispositivo?> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferencias ->
            val rol = preferencias[CLAVE_ROL]?.let { nombre ->
                RolDispositivo.entries.firstOrNull { it.name == nombre }
            }
            val medicoId = preferencias[CLAVE_MEDICO_ID]
            if (rol != null && medicoId != null) ConfiguracionDispositivo(rol, medicoId) else null
        }
        .distinctUntilChanged()

    suspend fun guardar(rol: RolDispositivo, medicoId: String) {
        dataStore.edit { preferencias ->
            preferencias[CLAVE_ROL] = rol.name
            preferencias[CLAVE_MEDICO_ID] = medicoId
        }
    }

    private companion object {
        val CLAVE_ROL = stringPreferencesKey("rol")
        val CLAVE_MEDICO_ID = stringPreferencesKey("medico_id")
    }
}
