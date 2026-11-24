package cl.duoc.bitacora_escolar.ui.estudiante.historial

import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.FiltroSeguimiento
import cl.duoc.bitacora_escolar.model.Registro

data class HistorialEstudianteUiState(
    val misRegistros: List<Registro> = emptyList(),
    val bitacoras: List<BitacoraConNombres> = emptyList(),   // para mostrar a qué bitácora pertenece cada aporte
    val filtro: String = FiltroSeguimiento.TODOS,

    // Estado de la pantalla
    val cargando: Boolean = false,
    val error: String = ""
)
