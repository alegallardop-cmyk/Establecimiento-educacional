package cl.duoc.bitacora_escolar.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.model.Evidencia
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.model.TipoEvidencia
import cl.duoc.bitacora_escolar.ui.theme.ColorBorrador
import cl.duoc.bitacora_escolar.ui.theme.ColorCerrada
import cl.duoc.bitacora_escolar.ui.theme.ColorExito
import cl.duoc.bitacora_escolar.ui.theme.ColorPendiente
import cl.duoc.bitacora_escolar.ui.theme.ColorPublicada
import cl.duoc.bitacora_escolar.ui.theme.ColorRevisado
import cl.duoc.bitacora_escolar.ui.theme.ColorTextoClaro
import cl.duoc.bitacora_escolar.util.nombreMes
import coil3.compose.AsyncImage

// Composables pequeños que reutilizamos en varias pantallas.

// Contenido con scroll de todas las pantallas.
// En un teléfono usa todo el ancho; en una tablet o en horizontal el contenido
// se limita a 600 dp y queda centrado (así se adapta a distintos tamaños de pantalla).
@Composable
fun ContenidoPantalla(
    innerPadding: PaddingValues,
    contenido: LazyListScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = contenido
        )
    }
}

@Composable
fun Titulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

// Ícono rojo que se pone al final de un campo con error (trailingIcon)
@Composable
fun IconoError() {
    Icon(
        imageVector = Icons.Filled.Error,
        contentDescription = "Error",
        tint = MaterialTheme.colorScheme.error
    )
}

// Texto de error con ícono (para errores que no son de un solo campo)
@Composable
fun TextoError(texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconoError()
        Text(texto, color = MaterialTheme.colorScheme.error)
    }
}

// Mensaje de confirmación (verde, con ✓) o de error de operación (rojo, con !)
@Composable
fun Mensaje(texto: String, esError: Boolean = false) {
    val color = if (esError) MaterialTheme.colorScheme.error else ColorExito
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (esError) Icons.Filled.Error else Icons.Filled.CheckCircle,
            contentDescription = if (esError) "Error" else "Correcto",
            tint = color
        )
        Text(text = texto, color = color, fontWeight = FontWeight.Bold)
    }
}

// Rectángulo de color con un texto para identificar estados
@Composable
fun Etiqueta(texto: String, color: Color) {
    Text(
        text = texto,
        color = ColorTextoClaro,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun EtiquetaBitacora(estado: String) {
    when (estado) {
        EstadoBitacora.PUBLICADA -> Etiqueta("Activa", ColorPublicada)
        EstadoBitacora.CERRADA -> Etiqueta("Cerrada", ColorCerrada)
        else -> Etiqueta("Borrador", ColorBorrador)
    }
}

// Diferencia: borrador / enviado y pendiente / revisado
@Composable
fun EtiquetaRegistro(registro: Registro) {
    when {
        registro.estadoEnvio == EstadoEnvio.BORRADOR -> Etiqueta("Borrador", ColorBorrador)
        registro.estadoRevision == EstadoRevision.REVISADO -> Etiqueta("Revisado", ColorRevisado)
        else -> Etiqueta("Enviado - Pendiente", ColorPendiente)
    }
}

// Botón para elegir una opción: relleno si está elegida, con borde si no
@Composable
fun BotonOpcion(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (seleccionado) {
        Button(onClick = onClick, modifier = modifier) { Text(texto) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(texto) }
    }
}

// Botones para filtrar por estado. opciones = lista de (texto, valor); "" = todos
@Composable
fun FiltroEstados(
    opciones: List<Pair<String, String>>,
    elegido: String,
    onElegir: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for ((texto, valor) in opciones) {
            // weight(1f): todos los botones del mismo ancho, caben en cualquier pantalla
            val modifier = Modifier.weight(1f)
            val relleno = PaddingValues(4.dp)
            if (elegido == valor) {
                Button(onClick = { onElegir(valor) }, modifier = modifier, contentPadding = relleno) {
                    Text(texto, style = MaterialTheme.typography.labelMedium)
                }
            } else {
                OutlinedButton(onClick = { onElegir(valor) }, modifier = modifier, contentPadding = relleno) {
                    Text(texto, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun TarjetaBitacora(bitacora: BitacoraConNombres, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            EtiquetaBitacora(bitacora.bitacora.estado)
            Text(bitacora.bitacora.titulo, style = MaterialTheme.typography.titleMedium)
            Text("${bitacora.curso} - ${bitacora.asignatura}")
            Text("${nombreMes(bitacora.bitacora.mes)} ${bitacora.bitacora.anio}")
        }
    }
}

@Composable
fun TarjetaRegistro(registro: Registro, titulo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            EtiquetaRegistro(registro)
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text("Clase del ${registro.fechaClase}")
            Text("Contenido: ${registro.contenido}")
            if (registro.comentarioDocente.isNotBlank()) {
                Text("Tiene comentario del docente", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// Recuadro con un número grande (indicadores del dashboard)
@Composable
fun TarjetaIndicador(valor: Int, etiqueta: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        }
    }
}

// Muestra una fotografía a partir de su URI, igual que en FotoMotion (Coil)
@Composable
fun FotoEvidencia(uri: String) {
    AsyncImage(
        model = uri,
        contentDescription = "Fotografía de evidencia",
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Fit
    )
}

// Muestra las evidencias de un registro: las fotos como imagen y los enlaces como texto
@Composable
fun ListaEvidencias(evidencias: List<Evidencia>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (evidencias.isEmpty()) {
            Text("Sin evidencias.")
        }
        for (evidencia in evidencias) {
            if (evidencia.tipo == TipoEvidencia.FOTO) {
                FotoEvidencia(evidencia.enlace)
            } else {
                Text("${evidencia.descripcion}: ${evidencia.enlace}")
            }
        }
    }
}

// Se muestra cuando alguien intenta ver algo que no le corresponde
@Composable
fun SinAcceso(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextoError("No tienes acceso a esta información.")
    }
}
