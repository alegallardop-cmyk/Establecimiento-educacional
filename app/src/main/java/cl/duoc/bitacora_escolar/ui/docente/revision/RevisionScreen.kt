package cl.duoc.bitacora_escolar.ui.docente.revision

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.ui.componentes.*

@Composable
fun RevisionScreen(
    onVolver: () -> Unit,
    viewModel: RevisionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(titulo = "Revisar aporte", onVolver = onVolver) }
    ) { innerPadding ->
        val registro = uiState.registro
        if (uiState.sinAcceso) {
            SinAcceso(Modifier.padding(innerPadding))
        } else if (registro != null) {
            ContenidoPantalla(innerPadding) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            EtiquetaRegistro(registro)
                            Text(uiState.estudiante, style = MaterialTheme.typography.titleLarge)
                            Text("Fecha de la clase: ${registro.fechaClase}")
                            Text("Contenido trabajado: ${registro.contenido}")
                            Text("Actividad realizada: ${registro.actividad}")
                            Text("Qué aprendió: ${registro.aprendizaje}")
                            Text("Dudas: ${registro.dudas.ifBlank { "ninguna" }}")
                            Text("Enviado el: ${registro.fechaEnvio}")
                        }
                    }
                }

                // ----- Evidencias: fotografías y enlaces -----
                item { Titulo("Evidencias") }
                item { ListaEvidencias(uiState.evidencias) }

                // ----- Retroalimentación -----
                item { Titulo("Retroalimentación") }
                item {
                    OutlinedTextField(
                        value = uiState.comentario,
                        onValueChange = { viewModel.cambiarComentario(it) },
                        label = { Text("Comentario para el estudiante") },
                        isError = uiState.errorComentario,
                        supportingText = {
                            if (uiState.errorComentario) Text("Mínimo 5 caracteres")
                        },
                        trailingIcon = { if (uiState.errorComentario) IconoError() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedButton(onClick = { viewModel.guardarComentario() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Guardar retroalimentación")
                    }
                }
                if (registro.estadoRevision != EstadoRevision.REVISADO) {
                    item {
                        Button(onClick = { viewModel.marcarRevisado() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Marcar como revisado")
                        }
                    }
                }
                item {
                    // Mensaje de confirmación o de error que aparece suavemente
                    AnimatedVisibility(
                        visible = uiState.mensaje.isNotBlank(),
                        enter = fadeIn(animationSpec = tween(500)) + expandVertically(animationSpec = tween(500))
                    ) {
                        Mensaje(uiState.mensaje, esError = uiState.mensajeEsError)
                    }
                }
            }
        }
    }
}
