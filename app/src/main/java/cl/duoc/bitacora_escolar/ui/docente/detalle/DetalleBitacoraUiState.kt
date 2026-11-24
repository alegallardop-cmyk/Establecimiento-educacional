package cl.duoc.bitacora_escolar.ui.docente.detalle

import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.RegistroConEstudiante
import cl.duoc.bitacora_escolar.model.Usuario

data class DetalleBitacoraUiState(
    val bitacora: BitacoraConNombres? = null,                // microservicio + nombres de Room
    val registros: List<RegistroConEstudiante> = emptyList(), // solo los ENVIADOS
    val estudiantes: List<Usuario> = emptyList(),   // estudiantes del curso (Room)

    // Filtros
    val filtroNombre: String = "",
    val filtroFecha: String = "",
    val filtroEstado: String = "",                  // "" = todos

    // Estado de la pantalla
    val sinAcceso: Boolean = false,
    val cargando: Boolean = false,
    val error: String = "",                         // no se pudo cargar desde el servidor
    val confirmarEliminar: Boolean = false,         // true = se muestra "¿Eliminar?" con Sí / No
    val mensaje: String = "",                       // confirmación o error de la operación
    val mensajeEsError: Boolean = false
)
