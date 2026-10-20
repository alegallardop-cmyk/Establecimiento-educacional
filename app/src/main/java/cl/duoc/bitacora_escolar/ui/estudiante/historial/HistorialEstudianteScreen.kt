package cl.duoc.bitacora_escolar.ui.estudiante.historial

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.bitacora_escolar.ui.componentes.*

@Composable
fun HistorialEstudianteScreen(
    onVolver: () -> Unit,
    onAbrirRegistro: () -> Unit,
    viewModel: HistorialEstudianteViewModel = viewModel()
) {
    val misRegistros by viewModel.misRegistros.collectAsStateWithLifecycle()
    val bitacoras by viewModel.bitacoras.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()

    val filtrados = viewModel.filtrados(misRegistros, filtro)

    Scaffold(
        topBar = { BarraSuperior(titulo = "Mis aportes", onVolver = onVolver) }
    ) { innerPadding ->
        ContenidoPantalla(innerPadding) {
            item { Titulo("Seguimiento de mis aportes") }
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
