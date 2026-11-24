package cl.duoc.bitacora_escolar.model

// Evidencia de un registro: una fotografía (galería o cámara) o un enlace.
// Viaja dentro del JSON del registro (lista "evidencias").
data class Evidencia(
    val id: Int = 0,
    val tipo: String,              // TipoEvidencia.FOTO o TipoEvidencia.ENLACE
    val descripcion: String,       // ej: "Foto de mi cuaderno"
    val enlace: String             // FOTO: el URI de la imagen. ENLACE: "https://..."
)
