package cl.duoc.bitacora_escolar.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.ui.componentes.SinAcceso
import cl.duoc.bitacora_escolar.ui.docente.detalle.DetalleBitacoraScreen
import cl.duoc.bitacora_escolar.ui.docente.formbitacora.FormBitacoraScreen
import cl.duoc.bitacora_escolar.ui.docente.historial.HistorialDocenteScreen
import cl.duoc.bitacora_escolar.ui.docente.inicio.DocenteInicioScreen
import cl.duoc.bitacora_escolar.ui.docente.revision.RevisionScreen
import cl.duoc.bitacora_escolar.ui.estudiante.bitacora.BitacoraEstudianteScreen
import cl.duoc.bitacora_escolar.ui.estudiante.historial.HistorialEstudianteScreen
import cl.duoc.bitacora_escolar.ui.estudiante.inicio.EstudianteInicioScreen
import cl.duoc.bitacora_escolar.ui.estudiante.registro.RegistroScreen
import cl.duoc.bitacora_escolar.ui.login.LoginScreen

@Composable
fun Navegacion() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                onIngresoDocente = { navController.navigate(Rutas.DOCENTE_INICIO) },
                onIngresoEstudiante = { navController.navigate(Rutas.ESTUDIANTE_INICIO) }
            )
        }

        // ==================== MÓDULO DOCENTE ====================
        // Control de acceso por perfil: si no es docente, ve "No tienes acceso"
        composable(Rutas.DOCENTE_INICIO) {
            // remember: el perfil se revisa una sola vez al entrar.
            // Así, al cerrar sesión no aparece "No tienes acceso" mientras vuelve al login.
            val esDocente = remember { Sesion.esDocente() }
            if (esDocente) {
                DocenteInicioScreen(
                    onNuevaBitacora = { navController.navigate(Rutas.FORM_BITACORA) },
                    onAbrirBitacora = { navController.navigate(Rutas.DETALLE_BITACORA) },
                    onHistorial = { navController.navigate(Rutas.HISTORIAL_DOCENTE) },
                    onCerrarSesion = { navController.popBackStack() }   // vuelve al login
                )
            } else {
                SinAcceso()
            }
        }

        composable(Rutas.FORM_BITACORA) {
            if (Sesion.esDocente()) {
                FormBitacoraScreen(onVolver = { navController.popBackStack() })
            } else {
                SinAcceso()
            }
        }

        composable(Rutas.DETALLE_BITACORA) {
            if (Sesion.esDocente()) {
                DetalleBitacoraScreen(
                    onVolver = { navController.popBackStack() },
                    onEditar = { navController.navigate(Rutas.FORM_BITACORA) },
                    onRevisarRegistro = { navController.navigate(Rutas.REVISION) }
                )
            } else {
                SinAcceso()
            }
        }

        composable(Rutas.REVISION) {
            if (Sesion.esDocente()) {
                RevisionScreen(onVolver = { navController.popBackStack() })
            } else {
                SinAcceso()
            }
        }

        composable(Rutas.HISTORIAL_DOCENTE) {
            if (Sesion.esDocente()) {
                HistorialDocenteScreen(
                    onVolver = { navController.popBackStack() },
                    onAbrirBitacora = { navController.navigate(Rutas.DETALLE_BITACORA) }
                )
            } else {
                SinAcceso()
            }
        }

        // ==================== MÓDULO ESTUDIANTE ====================
        composable(Rutas.ESTUDIANTE_INICIO) {
            val esEstudiante = remember { Sesion.esEstudiante() }
            if (esEstudiante) {
                EstudianteInicioScreen(
                    onAbrirBitacora = { navController.navigate(Rutas.BITACORA_ESTUDIANTE) },
                    onMisAportes = { navController.navigate(Rutas.HISTORIAL_ESTUDIANTE) },
                    onCerrarSesion = { navController.popBackStack() }   // vuelve al login
                )
            } else {
                SinAcceso()
            }
        }

        composable(Rutas.BITACORA_ESTUDIANTE) {
            if (Sesion.esEstudiante()) {
                BitacoraEstudianteScreen(
                    onVolver = { navController.popBackStack() },
                    onAbrirRegistro = { navController.navigate(Rutas.REGISTRO) }
                )
            } else {
                SinAcceso()
            }
        }

        composable(Rutas.REGISTRO) {
            if (Sesion.esEstudiante()) {
                RegistroScreen(onVolver = { navController.popBackStack() })
            } else {
                SinAcceso()
            }
        }

        composable(Rutas.HISTORIAL_ESTUDIANTE) {
            if (Sesion.esEstudiante()) {
                HistorialEstudianteScreen(
                    onVolver = { navController.popBackStack() },
                    onAbrirRegistro = { navController.navigate(Rutas.REGISTRO) }
                )
            } else {
                SinAcceso()
            }
        }
    }
}
