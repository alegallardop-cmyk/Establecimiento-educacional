package cl.duoc.bitacora_escolar.ui.docente.detalle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.ui.componentes.*
import cl.duoc.bitacora_escolar.ui.feriados.SeccionFeriados
import cl.duoc.bitacora_escolar.util.nombreMes

@Composable
fun DetalleBitacoraScreen(
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onRevisarRegistro: () -> Unit,
    viewModel: DetalleBitacoraViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bitacora = uiState.bitacora
    val registros = uiState.registros

    // Cada vez que la pantalla se muestra (también al volver desde otra) se piden los datos al servidor
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargar()
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Bitácora", onVolver = onVolver) }
    ) { innerPadding ->
        val conNombres = bitacora
        if (uiState.sinAcceso) {
            SinAcceso(Modifier.padding(innerPadding))
        } else if (conNombres != null) {
            val datos = conNombres.bitacora
            val indicadores = viewModel.indicadores(registros)

            ContenidoPantalla(innerPadding) {
                if (uiState.error.isNotBlank()) {
                    item { ErrorConexion(uiState.error) { viewModel.cargar() } }
                }

                // ----- Información -----
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Crossfade: al publicar o cerrar, la etiqueta cambia con un desvanecimiento
                            Crossfade(
                                targetState = datos.estado,
                                animationSpec = tween(500),
                                label = "Estado de la bitácora"
                            ) { estado ->
                                EtiquetaBitacora(estado)
                            }
                            Text(datos.titulo, style = MaterialTheme.typography.titleLarge)
                            Text("${conNombres.curso} - ${conNombres.asignatura} - ${nombreMes(datos.mes)} ${datos.anio}")
                            Text("Propósito: ${datos.proposito}")
                            Text("Instrucciones: ${datos.instrucciones}")
                            Text("Preguntas orientadoras:")
                            Text(datos.preguntas)
                            Text("Publicada: ${datos.fechaPublicacion.ifBlank { "no" }}")
                            Text("Cerrada: ${datos.fechaCierre.ifBlank { "no" }}")
                        }
                    }
                }

                // ----- Feriados del mes (API externa) -----
                item { SeccionFeriados(mes = datos.mes, anio = datos.anio) }

                // ----- Acciones según el estado -----
                if (datos.estado != EstadoBitacora.CERRADA) {
                    item {
                        OutlinedButton(onClick = { onEditar() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Editar bitácora")
                        }
                    }
                }
                if (datos.estado == EstadoBitacora.BORRADOR) {
                    item {
                        Button(onClick = { viewModel.publicar() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Publicar bitácora")
                        }
                    }
                }
                if (datos.estado == EstadoBitacora.PUBLICADA) {
                    item {
                        Button(onClick = { viewModel.cerrar() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Cerrar bitácora")
                        }
                    }
                }
                // Eliminar (solo borradores): primero pide confirmar
                if (viewModel.puedeEliminar()) {
                    item {
                        if (!uiState.confirmarEliminar) {
                            OutlinedButton(onClick = { viewModel.pedirEliminar() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Eliminar bitácora")
                            }
                        } else {
                            ConfirmarEliminar(
                                pregunta = "¿Eliminar esta bitácora? No se puede deshacer.",
                                onConfirmar = { viewModel.eliminar(onEliminada = onVolver) },
                                onCancelar = { viewModel.cancelarEliminar() }
                            )
                        }
                    }
                }
                // Mensaje de confirmación o de error: aparece suavemente
                item {
                    AnimatedVisibility(
                        visible = uiState.mensaje.isNotBlank(),
                        enter = fadeIn(animationSpec = tween(500)) + expandVertically(animationSpec = tween(500))
                    ) {
                        Mensaje(uiState.mensaje, esError = uiState.mensajeEsError)
                    }
                }

                // ----- Informe mensual: indicadores, participación y resumen de aportes -----
                item { Titulo("Informe mensual: indicadores") }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TarjetaIndicador(indicadores.totalEstudiantes, "Estudiantes", Modifier.weight(1f))
                        TarjetaIndicador(indicadores.participaron, "Participaron", Modifier.weight(1f))
                        TarjetaIndicador(indicadores.sinParticipacion, "Sin participar", Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TarjetaIndicador(indicadores.totalAportes, "Aportes", Modifier.weight(1f))
                        TarjetaIndicador(indicadores.revisados, "Revisados", Modifier.weight(1f))
                        TarjetaIndicador(indicadores.pendientes, "Pendientes", Modifier.weight(1f))
                    }
                }

                // ----- Sin participación -----
                item { Titulo("Estudiantes sin participación") }
                items(viewModel.sinParticipacion(registros)) { estudiante ->
                    Text(estudiante.nombre)
                }

                // ----- Filtros -----
                item { Titulo("Resumen de aportes (temas y actividades)") }
                item {
                    OutlinedTextField(
                        value = uiState.filtroNombre,
                        onValueChange = { viewModel.cambiarFiltroNombre(it) },
                        label = { Text("Filtrar por estudiante") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = uiState.filtroFecha,
                        onValueChange = { viewModel.cambiarFiltroFecha(it) },
                        label = { Text("Filtrar por fecha (dd/mm/aaaa)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    FiltroEstados(
                        opciones = listOf(
                            "Todos" to "",
                            "Pendientes" to EstadoRevision.PENDIENTE,
                            "Revisados" to EstadoRevision.REVISADO
                        ),
                        elegido = uiState.filtroEstado,
                        onElegir = { viewModel.cambiarFiltroEstado(it) }
                    )
                }

                // ----- Lista de aportes -----
                val filtrados = viewModel.registrosFiltrados(registros)
                if (filtrados.isEmpty()) {
                    item { Text("No hay aportes con esos filtros.") }
                }
                items(filtrados) { aporte ->
                    TarjetaRegistro(
                        registro = aporte.registro,
                        titulo = aporte.estudiante,
                        onClick = {
                            viewModel.seleccionarRegistro(aporte.registro.id)
                            onRevisarRegistro()
                        }
                    )
                }
            }
        } else if (uiState.cargando) {
            Cargando(Modifier.padding(innerPadding))
        } else if (uiState.error.isNotBlank()) {
            // No se pudo cargar la bitácora desde el servidor
            Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                ErrorConexion(uiState.error) { viewModel.cargar() }
            }
        }
    }
}
