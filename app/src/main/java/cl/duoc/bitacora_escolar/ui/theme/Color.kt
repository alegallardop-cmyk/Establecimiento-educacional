package cl.duoc.bitacora_escolar.ui.theme

import androidx.compose.ui.graphics.Color

// TODOS los colores de la app se definen aquí (en las pantallas no se escriben códigos hex).

// Paleta principal (azul escolar + amarillo)
val Azul40 = Color(0xFF1F5FAD)
val Azul80 = Color(0xFFA9C7FF)
val Amarillo40 = Color(0xFF8A6A00)
val Amarillo80 = Color(0xFFF2C94C)
val Verde40 = Color(0xFF2E7D32)
val Verde80 = Color(0xFF9CD69F)

// Colores semánticos: cada uno tiene un significado
val ColorExito = Color(0xFF15803D)        // verde: operación correcta
val ColorAdvertencia = Color(0xFFB45309)  // ámbar: algo pendiente
val ColorError = Color(0xFFB3261E)        // rojo: error
val ColorDeshabilitado = Color(0xFF6B7280) // gris: aún no disponible
val ColorTextoClaro = Color(0xFFFFFFFF)   // texto sobre las etiquetas de color

// Colores de los estados (reutilizan los semánticos)
val ColorBorrador = ColorDeshabilitado    // borrador
val ColorPendiente = ColorAdvertencia     // enviado, falta revisar
val ColorRevisado = ColorExito            // revisado
val ColorPublicada = Color(0xFF1D4ED8)    // azul: bitácora activa
val ColorCerrada = Color(0xFF7C3AED)      // morado: bitácora cerrada
