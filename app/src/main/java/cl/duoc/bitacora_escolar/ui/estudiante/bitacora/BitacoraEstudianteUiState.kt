package cl.duoc.bitacora_escolar.ui.estudiante.bitacora

import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.Registro

data class BitacoraEstudianteUiState(
    val bitacora: BitacoraConNombres? = null,
    val misRegistros: List<Registro> = emptyList(),   // solo los de ESTE estudiante

    // Estado de la pantalla
    val sinAcceso: Boolean = false,
    val cargando: Boolean = false,
    val error: String = ""
)
