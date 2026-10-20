package cl.duoc.bitacora_escolar.ui.docente.inicio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.ui.componentes.*
import cl.duoc.bitacora_escolar.ui.theme.ColorAdvertencia

// Dashboard del docente
@Composable
fun DocenteInicioScreen(
    onNuevaBitacora: () -> Unit,
    onAbrirBitacora: () -> Unit,
    onHistorial: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: DocenteInicioViewModel = viewModel()
) {
    val asignaciones by viewModel.asignaciones.collectAsStateWithLifecycle()
    val bitacoras by viewModel.bitacoras.collectAsStateWithLifecycle()
    val pendientes by viewModel.pendientesRevision.collectAsStateWithLifecycle()
    val recordatorios by viewModel.recordatorios.collectAsStateWithLifecycle()

    val hayAvisos = pendientes > 0 || recordatorios.isNotEmpty()

    Scaffold(
        topBar = { BarraSuperior(titulo = "Hola, ${viewModel.nombreDocente}") }
    ) { innerPadding ->
        ContenidoPantalla(innerPadding) {
            // ----- Indicadores (dashboard) -----
            item { Titulo("Resumen") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TarjetaIndicador(viewModel.contar(bitacoras, EstadoBitacora.PUBLICADA), "Activas", Modifier.weight(1f))
                    TarjetaIndicador(viewModel.contar(bitacoras, EstadoBitacora.BORRADOR), "Borradores", Modifier.weight(1f))
                    TarjetaIndicador(viewModel.contar(bitacoras, EstadoBitacora.CERRADA), "Cerradas", Modifier.weight(1f))
                    TarjetaIndicador(pendientes, "Por revisar", Modifier.weight(1f))
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.nuevaBitacora()
                            onNuevaBitacora()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Nueva bitácora")
                    }
                    OutlinedButton(onClick = { onHistorial() }, modifier = Modifier.weight(1f)) {
                        Text("Historial")
                    }
                }
            }

            // ----- Avisos internos (aparecen y desaparecen con una animación suave) -----
            item {
                AnimatedVisibility(
                    visible = hayAvisos,
                    enter = fadeIn(animationSpec = tween(500)) + expandVertically(animationSpec = tween(500)),
                    exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Titulo("Avisos")
                            if (pendientes > 0) {
                                Text(
                                    "Tienes $pendientes aporte(s) pendiente(s) de revisión.",
                                    color = ColorAdvertencia
                                )
                            }
                            for (recordatorio in recordatorios) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${recordatorio.fechaProgramada}: ${recordatorio.descripcion}",
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { viewModel.completarRecordatorio(recordatorio) }) {
                                        Text("Listo")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ----- Cursos y asignaturas -----
            item { Titulo("Mis cursos y asignaturas") }
            items(asignaciones) { asignacion ->
                Text("${asignacion.curso} - ${asignacion.asignatura}")
            }

            // ----- Bitácoras activas y borradores (las cerradas están en el Historial) -----
            item { Titulo("Bitácoras activas y borradores") }
            val enCurso = viewModel.bitacorasEnCurso(bitacoras)
            if (enCurso.isEmpty()) {
                item { Text("No tienes bitácoras activas. Crea una con \"Nueva bitácora\".") }
            }
            items(enCurso) { bitacora ->
                TarjetaBitacora(
                    bitacora = bitacora,
                    onClick = {
                        viewModel.seleccionarBitacora(bitacora.bitacora.id)
                        onAbrirBitacora()
                    }
                )
            }

            item {
                OutlinedButton(
                    onClick = {
                        viewModel.cerrarSesion()
                        onCerrarSesion()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
    }
}
