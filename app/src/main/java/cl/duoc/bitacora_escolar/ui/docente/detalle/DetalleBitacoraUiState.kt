package cl.duoc.bitacora_escolar.ui.docente.detalle

import cl.duoc.bitacora_escolar.model.Usuario

data class DetalleBitacoraUiState(
    val estudiantes: List<Usuario> = emptyList(),   // estudiantes del curso

    // Filtros
    val filtroNombre: String = "",
    val filtroFecha: String = "",
    val filtroEstado: String = "",                  // "" = todos

    // Estado de la pantalla
    val sinAcceso: Boolean = false,
    val mensaje: String = "",                       // confirmación o error de la operación
    val mensajeEsError: Boolean = false
)
