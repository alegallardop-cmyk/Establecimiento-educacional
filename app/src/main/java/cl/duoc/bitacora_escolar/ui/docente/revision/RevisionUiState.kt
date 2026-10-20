package cl.duoc.bitacora_escolar.ui.docente.revision

import cl.duoc.bitacora_escolar.model.Evidencia
import cl.duoc.bitacora_escolar.model.Registro

data class RevisionUiState(
    val registro: Registro? = null,
    val estudiante: String = "",             // nombre (viene del JOIN con usuarios)
    val evidencias: List<Evidencia> = emptyList(),

    // Variable
    val comentario: String = "",

    // Estado de error
    val errorComentario: Boolean = false,

    // Estado de la pantalla
    val sinAcceso: Boolean = false,
    val mensaje: String = "",
    val mensajeEsError: Boolean = false
)
