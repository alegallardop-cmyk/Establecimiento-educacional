package cl.duoc.bitacora_escolar.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.bitacora_escolar.ui.login.LoginScreen

// ETAPA 1: por ahora la app solo tiene el Login.
// Las pantallas del docente y del estudiante se agregan en la siguiente etapa.
@Composable
fun Navegacion() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                onIngresoDocente = { },      // pendiente: ir al inicio del docente
                onIngresoEstudiante = { }    // pendiente: ir al inicio del estudiante
            )
        }
    }
}
