package cl.duoc.bitacora_escolar.ui.estudiante.inicio

import cl.duoc.bitacora_escolar.model.AsignacionConNombres
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.Registro

data class EstudianteInicioUiState(
    // Room
    val nombreCurso: String = "",
    val asignaciones: List<AsignacionConNombres> = emptyList(),

    // Microservicio: bitácoras publicadas o cerradas de su curso y SUS registros
    val bitacoras: List<BitacoraConNombres> = emptyList(),
    val misRegistros: List<Registro> = emptyList(),

    // Estado de la pantalla
    val cargando: Boolean = false,
    val error: String = ""
)
