package cl.duoc.bitacora_escolar.ui.estudiante.inicio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.bitacora_escolar.ui.componentes.*
import cl.duoc.bitacora_escolar.ui.theme.ColorAdvertencia

// Dashboard del estudiante
@Composable
fun EstudianteInicioScreen(
    onAbrirBitacora: () -> Unit,
    onMisAportes: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: EstudianteInicioViewModel = viewModel()
) {
    val nombreCurso by viewModel.nombreCurso.collectAsStateWithLifecycle()
    val asignaciones by viewModel.asignaciones.collectAsStateWithLifecycle()
    val bitacoras by viewModel.bitacoras.collectAsStateWithLifecycle()
    val misRegistros by viewModel.misRegistros.collectAsStateWithLifecycle()

    val borradores = viewModel.cantidadBorradores(misRegistros)
    val comentarios = viewModel.cantidadComentarios(misRegistros)
    val activas = viewModel.activas(bitacoras)

    Scaffold(
        topBar = { BarraSuperior(titulo = "Hola, ${viewModel.nombreEstudiante}") }
    ) { innerPadding ->
        ContenidoPantalla(innerPadding) {
            // ----- Indicadores (dashboard) -----
            item { Titulo("Resumen") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TarjetaIndicador(activas.size, "Activas", Modifier.weight(1f))
                    TarjetaIndicador(viewModel.cantidadEnviados(misRegistros), "Enviados", Modifier.weight(1f))
                    TarjetaIndicador(viewModel.cantidadRevisados(misRegistros), "Revisados", Modifier.weight(1f))
                    TarjetaIndicador(borradores, "Borradores", Modifier.weight(1f))
                }
            }
            item {
                Button(onClick = { onMisAportes() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Mis aportes (historial y seguimiento)")
                }
            }

            // ----- Avisos (aparecen con una animación suave) -----
            item {
                AnimatedVisibility(
                    visible = borradores > 0 || comentarios > 0,
                    enter = fadeIn(animationSpec = tween(500)) + expandVertically(animationSpec = tween(500))
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Titulo("Avisos")
                            if (borradores > 0) {
                                Text("Tienes $borradores borrador(es) sin enviar.", color = ColorAdvertencia)
                            }
                            if (comentarios > 0) Text("Tienes $comentarios comentario(s) de tus docentes.")
                        }
                    }
                }
            }

            // ----- Curso y asignaturas -----
            item { Titulo("Mi curso: $nombreCurso") }
            items(asignaciones) { asignacion ->
                Text(asignacion.asignatura)
            }

            // ----- Bitácoras -----
            item { Titulo("Bitácoras activas") }
            if (activas.isEmpty()) {
                item { Text("No hay bitácoras activas por ahora.") }
            }
            items(activas) { bitacora ->
                TarjetaBitacora(
                    bitacora = bitacora,
                    onClick = {
                        viewModel.seleccionarBitacora(bitacora.bitacora.id)
                        onAbrirBitacora()
                    }
                )
            }
            // Historial: bitácoras cerradas, con sus registros y comentarios
            item { Titulo("Bitácoras anteriores") }
            items(viewModel.cerradas(bitacoras)) { bitacora ->
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
