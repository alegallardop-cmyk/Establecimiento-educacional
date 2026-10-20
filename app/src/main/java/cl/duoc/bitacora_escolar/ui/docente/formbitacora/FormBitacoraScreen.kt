package cl.duoc.bitacora_escolar.ui.docente.formbitacora

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.bitacora_escolar.ui.componentes.*

@Composable
fun FormBitacoraScreen(
    onVolver: () -> Unit,
    viewModel: FormBitacoraViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val esEdicion = viewModel.esEdicion()

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = if (esEdicion) "Editar bitácora" else "Nueva bitácora",
                onVolver = onVolver
            )
        }
    ) { innerPadding ->
        if (uiState.sinAcceso) {
            SinAcceso(Modifier.padding(innerPadding))
        } else {
            ContenidoPantalla(innerPadding) {
                // ----- Curso y asignatura -----
                item { Titulo("1. Curso y asignatura") }
                if (esEdicion) {
                    // Al editar no se cambia el curso ni la asignatura
                    val elegida = uiState.asignacionElegida
                    if (elegida != null) {
                        item { Text("${elegida.curso} - ${elegida.asignatura} (no se puede cambiar)") }
                    }
                } else {
                    items(uiState.asignaciones) { asignacion ->
                        BotonOpcion(
                            texto = "${asignacion.curso} - ${asignacion.asignatura}",
                            seleccionado = uiState.asignacionElegida == asignacion,
                            onClick = { viewModel.elegirAsignacion(asignacion) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (uiState.errorAsignacion) {
                        item { TextoError("Debe elegir un curso y asignatura") }
                    }
                }

                // ----- Mes y año -----
                item { Titulo("2. Mes y año") }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.mes,
                            onValueChange = { viewModel.cambiarMes(it) },
                            label = { Text("Mes (1 a 12)") },
                            isError = uiState.errorMes,
                            supportingText = { if (uiState.errorMes) Text("Mes inválido") },
                            trailingIcon = { if (uiState.errorMes) IconoError() },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = uiState.anio,
                            onValueChange = { viewModel.cambiarAnio(it) },
                            label = { Text("Año") },
                            isError = uiState.errorAnio,
                            supportingText = { if (uiState.errorAnio) Text("Año inválido") },
                            trailingIcon = { if (uiState.errorAnio) IconoError() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (uiState.errorRepetida) {
                    item { TextoError("Ya existe una bitácora para ese curso, asignatura y mes") }
                }

                // ----- Contenido -----
                item { Titulo("3. Contenido") }
                item {
                    OutlinedTextField(
                        value = uiState.titulo,
                        onValueChange = { viewModel.cambiarTitulo(it) },
                        label = { Text("Título") },
                        isError = uiState.errorTitulo,
                        supportingText = { if (uiState.errorTitulo) Text("Entre 5 y 80 caracteres") },
                        trailingIcon = { if (uiState.errorTitulo) IconoError() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = uiState.proposito,
                        onValueChange = { viewModel.cambiarProposito(it) },
                        label = { Text("Propósito") },
                        isError = uiState.errorProposito,
                        supportingText = { if (uiState.errorProposito) Text("El propósito es obligatorio") },
                        trailingIcon = { if (uiState.errorProposito) IconoError() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = uiState.instrucciones,
                        onValueChange = { viewModel.cambiarInstrucciones(it) },
                        label = { Text("Instrucciones") },
                        isError = uiState.errorInstrucciones,
                        supportingText = { if (uiState.errorInstrucciones) Text("Las instrucciones son obligatorias") },
                        trailingIcon = { if (uiState.errorInstrucciones) IconoError() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ----- Preguntas orientadoras -----
                item { Titulo("4. Preguntas orientadoras") }
                items(uiState.preguntas) { pregunta ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(pregunta, modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.quitarPregunta(pregunta) }) {
                            Text("Quitar")
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = uiState.nuevaPregunta,
                        onValueChange = { viewModel.cambiarNuevaPregunta(it) },
                        label = { Text("Nueva pregunta") },
                        isError = uiState.errorNuevaPregunta,
                        supportingText = { if (uiState.errorNuevaPregunta) Text("Mínimo 5 caracteres") },
                        trailingIcon = { if (uiState.errorNuevaPregunta) IconoError() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedButton(onClick = { viewModel.agregarPregunta() }) {
                        Text("Agregar pregunta")
                    }
                }
                if (uiState.errorPreguntas) {
                    item { TextoError("Agregue al menos una pregunta") }
                }

                // ----- Guardar -----
                if (!uiState.guardada) {
                    if (esEdicion) {
                        item {
                            Button(onClick = { viewModel.guardar(publicar = false) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Guardar cambios")
                            }
                        }
                    } else {
                        item {
                            OutlinedButton(
                                onClick = { viewModel.guardar(publicar = false) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Guardar como borrador")
                            }
                        }
                        item {
                            Button(onClick = { viewModel.guardar(publicar = true) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Guardar y publicar")
                            }
                        }
                    }
                }

                // Confirmación (verde) o error de la operación (rojo): aparece suavemente
                item {
                    AnimatedVisibility(
                        visible = uiState.mensaje.isNotBlank(),
                        enter = fadeIn(animationSpec = tween(500)) + expandVertically(animationSpec = tween(500))
                    ) {
                        Mensaje(uiState.mensaje, esError = uiState.mensajeEsError)
                    }
                }
                if (uiState.guardada) {
                    item {
                        Button(onClick = { onVolver() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Volver")
                        }
                    }
                }
            }
        }
    }
}
