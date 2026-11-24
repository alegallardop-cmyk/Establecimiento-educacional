package cl.duoc.bitacora_escolar.ui.estudiante.historial

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.bitacora_escolar.model.FiltroSeguimiento
import cl.duoc.bitacora_escolar.ui.componentes.*

@Composable
fun HistorialEstudianteScreen(
    onVolver: () -> Unit,
    onAbrirRegistro: () -> Unit,
    viewModel: HistorialEstudianteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val misRegistros = uiState.misRegistros
    val bitacoras = uiState.bitacoras
    val filtro = uiState.filtro
    val error = uiState.error

    // Cada vez que la pantalla se muestra (también al volver desde otra) se piden los datos al servidor
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargar()
    }

    val filtrados = viewModel.filtrados(misRegistros, filtro)

    Scaffold(
        topBar = { BarraSuperior(titulo = "Mis aportes", onVolver = onVolver) }
    ) { innerPadding ->
        ContenidoPantalla(innerPadding) {
            item { Titulo("Seguimiento de mis aportes") }
            if (uiState.cargando) {
                item { Cargando() }
            }
            if (error.isNotBlank()) {
                item { ErrorConexion(error) { viewModel.cargar() } }
            }
            item {
                FiltroEstados(
                    opciones = listOf(
                        "Todos" to FiltroSeguimiento.TODOS,
                        "Borrador" to FiltroSeguimiento.BORRADORES,
                        "Pendiente" to FiltroSeguimiento.PENDIENTES,
                        "Revisado" to FiltroSeguimiento.REVISADOS
                    ),
                    elegido = filtro,
                    onElegir = { viewModel.cambiarFiltro(it) }
                )
            }
            item { Text("${filtrados.size} aporte(s)") }
            if (filtrados.isEmpty()) {
                item { Text("No tienes aportes con ese estado.") }
            }
            items(filtrados) { registro ->
                TarjetaRegistro(
                    registro = registro,
                    titulo = viewModel.nombreBitacora(registro, bitacoras),
                    onClick = {
                        viewModel.abrirRegistro(registro)
                        onAbrirRegistro()
                    }
                )
            }
        }
    }
}
