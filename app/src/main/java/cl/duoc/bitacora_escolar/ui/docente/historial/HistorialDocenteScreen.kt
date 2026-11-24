package cl.duoc.bitacora_escolar.ui.docente.historial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.ui.componentes.*

@Composable
fun HistorialDocenteScreen(
    onVolver: () -> Unit,
    onAbrirBitacora: () -> Unit,
    viewModel: HistorialDocenteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bitacoras = uiState.bitacoras
    val filtroEstado = uiState.filtroEstado
    val filtroTexto = uiState.filtroTexto
    val error = uiState.error

    // Cada vez que la pantalla se muestra (también al volver desde otra) se piden los datos al servidor
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargar()
    }

    val filtradas = viewModel.filtradas(bitacoras, filtroEstado, filtroTexto)

    Scaffold(
        topBar = { BarraSuperior(titulo = "Historial de bitácoras", onVolver = onVolver) }
    ) { innerPadding ->
        ContenidoPantalla(innerPadding) {
            if (uiState.cargando) {
                item { Cargando() }
            }
            if (error.isNotBlank()) {
                item { ErrorConexion(error) { viewModel.cargar() } }
            }
            item {
                OutlinedTextField(
                    value = filtroTexto,
                    onValueChange = { viewModel.cambiarFiltroTexto(it) },
                    label = { Text("Buscar por curso, asignatura o título") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                FiltroEstados(
                    opciones = listOf(
                        "Todas" to "",
                        "Borrador" to EstadoBitacora.BORRADOR,
                        "Activas" to EstadoBitacora.PUBLICADA,
                        "Cerradas" to EstadoBitacora.CERRADA
                    ),
                    elegido = filtroEstado,
                    onElegir = { viewModel.cambiarFiltroEstado(it) }
                )
            }
            item { Titulo("${filtradas.size} bitácora(s)") }
            if (filtradas.isEmpty()) {
                item { Text("No hay bitácoras con esos filtros.") }
            }
            items(filtradas) { bitacora ->
                TarjetaBitacora(
                    bitacora = bitacora,
                    onClick = {
                        viewModel.seleccionarBitacora(bitacora.bitacora.id)
                        onAbrirBitacora()
                    }
                )
            }
        }
    }
}
