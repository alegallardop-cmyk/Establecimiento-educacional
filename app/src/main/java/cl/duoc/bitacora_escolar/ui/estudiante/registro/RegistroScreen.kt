package cl.duoc.bitacora_escolar.ui.estudiante.registro

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.bitacora_escolar.model.TipoEvidencia
import cl.duoc.bitacora_escolar.ui.componentes.*
import cl.duoc.bitacora_escolar.ui.theme.ColorDeshabilitado
import cl.duoc.bitacora_escolar.ui.theme.ColorExito
import cl.duoc.bitacora_escolar.util.crearUriFotografia

@Composable
fun RegistroScreen(
    onVolver: () -> Unit,
    viewModel: RegistroViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ===== RECURSOS NATIVOS (igual que en FotoMotion) =====
    val context = LocalContext.current

    // 1. Photo Picker: elegir una foto de la galería, sin pedir permisos
    val selectorFotos = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // Pedimos que el permiso de lectura sea PERSISTENTE: así la evidencia
            // se sigue viendo aunque se cierre la app (si no, el URI deja de funcionar)
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            viewModel.agregarFoto(uri)
        }
    }

    // 2. Cámara: la app de cámara guarda la foto en el URI que le damos
    //    (un archivo dentro de la app: se conserva hasta desinstalarla)
    var uriPendiente by rememberSaveable { mutableStateOf<Uri?>(null) }
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = TakePicture()
    ) { fotoTomada ->
        val uri = uriPendiente
        if (fotoTomada && uri != null) viewModel.agregarFoto(uri)
        uriPendiente = null
    }

    // ===== ANIMACIONES =====
    val fotos = uiState.evidencias.filter { it.tipo == TipoEvidencia.FOTO }
    val enlaces = uiState.evidencias.filter { it.tipo == TipoEvidencia.ENLACE }
    val hayFoto = fotos.isNotEmpty()

    // El botón "Enviar" cambia de gris a verde cuando el estudiante confirma
    val colorEnviar by animateColorAsState(
        targetValue = if (uiState.confirmaEnvio) ColorExito else ColorDeshabilitado,
        animationSpec = tween(durationMillis = 600),
        label = "Color del botón enviar"
    )

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = if (uiState.soloLectura) "Mi registro" else "Registro de clase",
                onVolver = onVolver
            )
        }
    ) { innerPadding ->
        val bitacora = uiState.bitacora
        if (uiState.sinAcceso) {
            SinAcceso(Modifier.padding(innerPadding))
        } else if (bitacora != null) {
            ContenidoPantalla(innerPadding) {
                item { Titulo(bitacora.titulo) }

                if (uiState.soloLectura) {
                    // ===== MODO SOLO LECTURA: registro enviado o bitácora cerrada =====
                    val registro = uiState.registro
                    // Confirmación al enviar (la pantalla pasa a solo lectura)
                    item {
                        AnimatedVisibility(
                            visible = uiState.mensaje.isNotBlank(),
                            enter = fadeIn(animationSpec = tween(500)) + expandVertically(animationSpec = tween(500))
                        ) {
                            Mensaje(uiState.mensaje, esError = uiState.mensajeEsError)
                        }
                    }
                    if (registro == null) {
                        item { Text("Esta bitácora está cerrada y ya no recibe registros.") }
                    } else {
                        item { EtiquetaRegistro(registro) }
                        item { Text("Fecha de la clase: ${registro.fechaClase}") }
                        item { Text("Contenido trabajado: ${registro.contenido}") }
                        item { Text("Actividad realizada: ${registro.actividad}") }
                        item { Text("Qué aprendí: ${registro.aprendizaje}") }
                        item { Text("Dudas: ${registro.dudas.ifBlank { "ninguna" }}") }
                        item { Titulo("Evidencias") }
                        item { ListaEvidencias(uiState.evidencias) }
                        item { Titulo("Retroalimentación del docente") }
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    if (registro.comentarioDocente.isBlank()) {
                                        Text("Tu docente aún no deja comentarios.")
                                    } else {
                                        Text(registro.comentarioDocente)
                                        Text(
                                            "Fecha: ${registro.fechaRetroalimentacion}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ===== MODO EDICIÓN: registro nuevo o borrador =====
                    item {
                        Column {
                            Text("Preguntas para guiarte:")
                            Text(bitacora.preguntas)
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.fechaClase,
                            onValueChange = { viewModel.cambiarFecha(it) },
                            label = { Text("Fecha de la clase (dd/mm/aaaa)") },
                            isError = uiState.errorFecha != "",
                            supportingText = {
                                if (uiState.errorFecha != "") Text(uiState.errorFecha)
                            },
                            trailingIcon = { if (uiState.errorFecha != "") IconoError() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.contenido,
                            onValueChange = { viewModel.cambiarContenido(it) },
                            label = { Text("¿Qué contenido trabajamos?") },
                            isError = uiState.errorContenido,
                            supportingText = { if (uiState.errorContenido) Text("Obligatorio para enviar") },
                            trailingIcon = { if (uiState.errorContenido) IconoError() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.actividad,
                            onValueChange = { viewModel.cambiarActividad(it) },
                            label = { Text("¿Qué actividad hicimos?") },
                            isError = uiState.errorActividad,
                            supportingText = { if (uiState.errorActividad) Text("Obligatorio para enviar") },
                            trailingIcon = { if (uiState.errorActividad) IconoError() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.aprendizaje,
                            onValueChange = { viewModel.cambiarAprendizaje(it) },
                            label = { Text("¿Qué aprendí?") },
                            isError = uiState.errorAprendizaje,
                            supportingText = { if (uiState.errorAprendizaje) Text("Obligatorio para enviar") },
                            trailingIcon = { if (uiState.errorAprendizaje) IconoError() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.dudas,
                            onValueChange = { viewModel.cambiarDudas(it) },
                            label = { Text("Dudas o dificultades (opcional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ----- Evidencias: fotografías -----
                    item { Titulo("Evidencias (máximo 5)") }
                    item {
                        // Crossfade: cambia un mensaje por otro con un desvanecimiento
                        Crossfade(
                            targetState = hayFoto,
                            animationSpec = tween(500),
                            label = "Estado de fotografía"
                        ) { tieneFoto ->
                            Text(
                                text = if (tieneFoto) "¡Fotografía agregada!"
                                else "Puedes agregar una foto de tu cuaderno o de tu trabajo"
                            )
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Botón de galería (Photo Picker)
                            OutlinedButton(
                                onClick = {
                                    selectorFotos.launch(
                                        PickVisualMediaRequest(
                                            PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Galería")
                            }
                            // Botón de cámara
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val nuevaUri = crearUriFotografia(context)
                                        uriPendiente = nuevaUri
                                        camaraLauncher.launch(nuevaUri)
                                    } catch (e: Exception) {
                                        uriPendiente = null
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cámara")
                            }
                        }
                    }
                    item {
                        // La primera foto aparece de forma progresiva
                        AnimatedVisibility(
                            visible = hayFoto,
                            enter = fadeIn(animationSpec = tween(600)) + expandVertically(animationSpec = tween(600)),
                            exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (foto in fotos) {
                                    FotoEvidencia(foto.enlace)
                                    TextButton(onClick = { viewModel.quitarEvidencia(foto) }) {
                                        Text("Quitar foto")
                                    }
                                }
                            }
                        }
                    }

                    // ----- Evidencias: enlaces -----
                    items(enlaces) { evidencia ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${evidencia.descripcion}: ${evidencia.enlace}", modifier = Modifier.weight(1f))
                            TextButton(onClick = { viewModel.quitarEvidencia(evidencia) }) {
                                Text("Quitar")
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.descripcionEvidencia,
                            onValueChange = { viewModel.cambiarDescripcionEvidencia(it) },
                            label = { Text("Descripción del enlace (opcional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.enlace,
                            onValueChange = { viewModel.cambiarEnlace(it) },
                            label = { Text("Enlace (https://...)") },
                            isError = uiState.errorEnlace,
                            supportingText = { if (uiState.errorEnlace) Text("Enlace no válido") },
                            trailingIcon = { if (uiState.errorEnlace) IconoError() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedButton(onClick = { viewModel.agregarEvidencia() }) {
                            Text("Agregar enlace")
                        }
                    }
                    if (uiState.errorMaximoEvidencias) {
                        item { TextoError("Máximo 5 evidencias por registro") }
                    }

                    // ----- Guardar o enviar -----
                    item {
                        OutlinedButton(onClick = { viewModel.guardarBorrador() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Guardar como borrador")
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = uiState.confirmaEnvio,
                                onCheckedChange = { viewModel.cambiarConfirmacion(it) }
                            )
                            Text("Revisé mi registro. Después de enviarlo no podré cambiarlo.")
                        }
                    }
                    if (uiState.errorConfirmacion) {
                        item { TextoError("Debe marcar la casilla para enviar") }
                    }
                    item {
                        Button(
                            onClick = { viewModel.enviar() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorEnviar
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enviar al docente")
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
}
