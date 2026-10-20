package cl.duoc.bitacora_escolar.ui.estudiante.bitacora

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.ui.componentes.*
import cl.duoc.bitacora_escolar.util.nombreMes

@Composable
fun BitacoraEstudianteScreen(
    onVolver: () -> Unit,
    onAbrirRegistro: () -> Unit,
    viewModel: BitacoraEstudianteViewModel = viewModel()
) {
    val sinAcceso by viewModel.sinAcceso.collectAsStateWithLifecycle()
    val bitacora by viewModel.bitacora.collectAsStateWithLifecycle()
    val misRegistros by viewModel.misRegistros.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(titulo = "Bitácora", onVolver = onVolver) }
    ) { innerPadding ->
        val conNombres = bitacora
        if (sinAcceso) {
            SinAcceso(Modifier.padding(innerPadding))
        } else if (conNombres != null) {
            val datos = conNombres.bitacora
            ContenidoPantalla(innerPadding) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            EtiquetaBitacora(datos.estado)
                            Text(datos.titulo, style = MaterialTheme.typography.titleLarge)
                            Text("${conNombres.asignatura} - ${nombreMes(datos.mes)} ${datos.anio}")
                            Text("Propósito: ${datos.proposito}")
                            Text("Instrucciones: ${datos.instrucciones}")
                            Text("Preguntas para guiarte:")
                            Text(datos.preguntas)
                        }
                    }
                }

                if (viewModel.puedeRegistrar(datos)) {
                    item {
                        Button(
                            onClick = {
                                viewModel.nuevoRegistro()
                                onAbrirRegistro()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Nuevo registro de clase")
                        }
                    }
                } else {
                    item { Text("Esta bitácora está cerrada: puedes ver tus aportes, pero no agregar nuevos.") }
                }

                item { Titulo("Mis registros") }
                if (misRegistros.isEmpty()) {
                    item { Text("Todavía no tienes registros en esta bitácora.") }
                }
                items(viewModel.ordenados(misRegistros)) { registro ->
                    TarjetaRegistro(
                        registro = registro,
                        titulo = if (registro.estadoEnvio == EstadoEnvio.BORRADOR) "Borrador (solo tú lo ves)" else "Registro enviado",
                        onClick = {
                            viewModel.abrirRegistro(registro.id)
                            onAbrirRegistro()
                        }
                    )
                }
            }
        }
    }
}
