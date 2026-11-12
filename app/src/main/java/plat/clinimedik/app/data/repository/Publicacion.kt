package plat.clinimedik.app.data.repository

import android.util.Log
import kotlinx.coroutines.CancellationException

/**
 * Ejecuta una llamada al sincronizador después de guardar en Room.
 * Si la publicación falla, el dato ya quedó guardado en el teléfono:
 * el error se registra y la pantalla no se entera.
 */
internal inline fun publicar(accion: () -> Unit) {
    try {
        accion()
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Log.w("Sincronizacion", "No se pudo publicar el cambio", error)
    }
}
