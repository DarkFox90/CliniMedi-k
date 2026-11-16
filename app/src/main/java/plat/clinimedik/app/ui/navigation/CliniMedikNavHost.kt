
package plat.clinimedik.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import plat.clinimedik.app.ui.components.BarraNavegacionMedico
import plat.clinimedik.app.ui.components.BarraNavegacionRecepcion
import plat.clinimedik.app.ui.components.PestanaMedico
import plat.clinimedik.app.ui.components.PestanaRecepcion
import plat.clinimedik.app.ui.screens.medico.agenda.agendaMedicoDestino
import plat.clinimedik.app.ui.screens.medico.fila.filaMedicoDestino
import plat.clinimedik.app.ui.screens.medico.historial.historialMedicoDestino
import plat.clinimedik.app.ui.screens.medico.pacientes.pacientesMedicoDestino
import plat.clinimedik.app.ui.screens.recepcion.agenda.agendaRecepcionDestino
import plat.clinimedik.app.ui.screens.recepcion.buscar.buscarDestino
import plat.clinimedik.app.ui.screens.recepcion.cuestionario.cuestionarioDestino
import plat.clinimedik.app.ui.screens.recepcion.detalle.detalleDestino
import plat.clinimedik.app.ui.screens.recepcion.escaneo.escaneoDestino
import plat.clinimedik.app.ui.screens.recepcion.fila.filaRecepcionDestino
import plat.clinimedik.app.ui.screens.recepcion.inicio.inicioRecepcionDestino
import plat.clinimedik.app.ui.screens.recepcion.nuevacita.nuevaCitaDestino
import plat.clinimedik.app.ui.screens.recepcion.qr.codigoQrDestino
import plat.clinimedik.app.ui.screens.recepcion.registro.registroDestino
import plat.clinimedik.app.ui.screens.recepcion.resultado.resultadoDestino
import plat.clinimedik.app.ui.screens.recepcion.resumen.resumenDestino
import plat.clinimedik.app.ui.screens.setup.arranque.arranqueDestino
import plat.clinimedik.app.ui.screens.setup.login.loginDestino
import plat.clinimedik.app.ui.screens.setup.rol.rolDestino
import plat.clinimedik.app.ui.screens.setup.seleccionmedico.seleccionMedicoDestino
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import plat.clinimedik.app.ui.components.LocalMensajes
import plat.clinimedik.app.ui.components.MensajesSnackbar
import androidx.compose.foundation.layout.navigationBarsPadding
import plat.clinimedik.app.ui.screens.recepcion.visor.visorDocumentoDestino

@Composable
fun CliniMedikNavHost() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mensajes = remember(snackbarHostState, scope) {
        MensajesSnackbar(snackbarHostState, scope)
    }

    CompositionLocalProvider(LocalMensajes provides mensajes) {
        ContenidoNavegacion(snackbarHostState)
    }
}
@Composable
private fun ContenidoNavegacion(snackbarHostState: SnackbarHostState) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val destino = entradaActual?.destination

    val pestanaRecepcion = when {
        destino?.hasRoute<Inicio>() == true -> PestanaRecepcion.INICIO
        destino?.hasRoute<Fila>() == true -> PestanaRecepcion.FILA
        destino?.hasRoute<AgendaRecepcion>() == true -> PestanaRecepcion.AGENDA
        destino?.hasRoute<Buscar>() == true -> PestanaRecepcion.BUSCAR
        else -> null
    }

    val pestanaMedico = when {
        destino?.hasRoute<FilaMedico>() == true -> PestanaMedico.FILA
        destino?.hasRoute<AgendaMedico>() == true -> PestanaMedico.AGENDA
        destino?.hasRoute<PacientesMedico>() == true -> PestanaMedico.PACIENTES
        else -> null
    }

    val hayBarra = pestanaRecepcion != null || pestanaMedico != null

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = if (hayBarra) Modifier else Modifier.navigationBarsPadding()
            )
        },
        bottomBar = {
            when {
                pestanaRecepcion != null -> {
                    BarraNavegacionRecepcion(
                        seleccion = pestanaRecepcion,
                        onSeleccionar = { pestana ->
                            val ruta = when (pestana) {
                                PestanaRecepcion.INICIO -> Inicio
                                PestanaRecepcion.FILA -> Fila
                                PestanaRecepcion.AGENDA -> AgendaRecepcion
                                PestanaRecepcion.BUSCAR -> Buscar
                            }
                            navController.irAPestana(ruta)
                        }
                    )
                }

                pestanaMedico != null -> {
                    BarraNavegacionMedico(
                        seleccion = pestanaMedico,
                        onSeleccionar = { pestana ->
                            val ruta = when (pestana) {
                                PestanaMedico.FILA -> FilaMedico
                                PestanaMedico.AGENDA -> AgendaMedico
                                PestanaMedico.PACIENTES -> PacientesMedico
                            }
                            navController.irAPestana(ruta)
                        }
                    )
                }
            }
        },
        contentWindowInsets = if (hayBarra) {
            ScaffoldDefaults.contentWindowInsets
        } else {
            WindowInsets(0)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Arranque,
            modifier = Modifier.padding(innerPadding),
            // Sin animaciones: al navegar o regresar, la pantalla cambia al instante.
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
            predictivePopEnterTransition = { EnterTransition.None },
            predictivePopExitTransition = { ExitTransition.None }
        ) {
            arranqueDestino(navController)

            navigation<SetupGraph>(startDestination = Login) {
                loginDestino(navController)
                rolDestino(navController)
                seleccionMedicoDestino(navController)
            }

            navigation<RecepcionGraph>(startDestination = Inicio) {
                inicioRecepcionDestino(navController)
                filaRecepcionDestino(navController)
                agendaRecepcionDestino(navController)
                buscarDestino(navController)
                detalleDestino(navController)
                nuevaCitaDestino(navController)
                registroDestino(navController)
                cuestionarioDestino(navController)
                codigoQrDestino(navController)
                escaneoDestino(navController)
                resultadoDestino(navController)
                resumenDestino(navController)
                visorDocumentoDestino(navController)
            }

            navigation<MedicoGraph>(startDestination = FilaMedico) {
                filaMedicoDestino(navController)
                agendaMedicoDestino(navController)
                pacientesMedicoDestino(navController)
                historialMedicoDestino(navController)
            }
        }
    }
}

