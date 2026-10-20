package cl.duoc.bitacora_escolar.ui.docente.formbitacora

import cl.duoc.bitacora_escolar.model.AsignacionConNombres

data class FormBitacoraUiState(
    // 0 = bitácora nueva; otro número = id de la bitácora que se está editando
    val bitacoraId: Int = 0,

    // Opciones para elegir (cursos y asignaturas del docente)
    val asignaciones: List<AsignacionConNombres> = emptyList(),

    // Variables
    val asignacionElegida: AsignacionConNombres? = null,
    val mes: String = "",
    val anio: String = "",
    val titulo: String = "",
    val proposito: String = "",
    val instrucciones: String = "",
    val nuevaPregunta: String = "",
    val preguntas: List<String> = emptyList(),

    // Estados de error
    val errorAsignacion: Boolean = false,
    val errorMes: Boolean = false,
    val errorAnio: Boolean = false,
    val errorTitulo: Boolean = false,
    val errorProposito: Boolean = false,
    val errorInstrucciones: Boolean = false,
    val errorPreguntas: Boolean = false,
    val errorNuevaPregunta: Boolean = false,
    val errorRepetida: Boolean = false,

    // Estado de la pantalla
    val sinAcceso: Boolean = false,
    val guardada: Boolean = false,      // true = se guardó bien (se muestra la confirmación)
    val mensaje: String = "",
    val mensajeEsError: Boolean = false
)
