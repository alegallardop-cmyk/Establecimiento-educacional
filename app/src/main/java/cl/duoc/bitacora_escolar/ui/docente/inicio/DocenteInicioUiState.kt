package cl.duoc.bitacora_escolar.ui.docente.inicio

import cl.duoc.bitacora_escolar.model.AsignacionConNombres
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.Recordatorio

data class DocenteInicioUiState(
    // Room
    val asignaciones: List<AsignacionConNombres> = emptyList(),
    val recordatorios: List<Recordatorio> = emptyList(),

    // Microservicio
    val bitacoras: List<BitacoraConNombres> = emptyList(),
    val pendientesRevision: Int = 0,

    // Estado de la pantalla
    val cargando: Boolean = false,
    val error: String = ""              // "" = sin error
)
