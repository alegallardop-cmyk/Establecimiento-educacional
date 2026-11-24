package cl.duoc.bitacora_escolar.ui.docente.historial

import cl.duoc.bitacora_escolar.model.BitacoraConNombres

data class HistorialDocenteUiState(
    val bitacoras: List<BitacoraConNombres> = emptyList(),

    // Filtros: estado ("" = todos) y texto (curso, asignatura o título)
    val filtroEstado: String = "",
    val filtroTexto: String = "",

    // Estado de la pantalla
    val cargando: Boolean = false,
    val error: String = ""
)
