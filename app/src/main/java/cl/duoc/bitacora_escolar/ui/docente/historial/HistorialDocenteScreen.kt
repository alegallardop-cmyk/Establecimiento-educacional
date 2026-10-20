package cl.duoc.bitacora_escolar.ui.docente.historial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    val bitacoras by viewModel.bitacoras.collectAsStateWithLifecycle()
    val filtroEstado by viewModel.filtroEstado.collectAsStateWithLifecycle()
    val filtroTexto by viewModel.filtroTexto.collectAsStateWithLifecycle()

    val filtradas = viewModel.filtradas(bitacoras, filtroEstado, filtroTexto)

    Scaffold(
        topBar = { BarraSuperior(titulo = "Historial de bitácoras", onVolver = onVolver) }
    ) { innerPadding ->
        ContenidoPantalla(innerPadding) {
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
