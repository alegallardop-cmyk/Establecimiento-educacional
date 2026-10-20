package cl.duoc.bitacora_escolar.ui.estudiante.registro

import cl.duoc.bitacora_escolar.model.Bitacora
import cl.duoc.bitacora_escolar.model.Evidencia
import cl.duoc.bitacora_escolar.model.Registro

data class RegistroUiState(
    val bitacora: Bitacora? = null,
    val registro: Registro? = null,          // null = registro nuevo

    // Variables del formulario
    val fechaClase: String = "",
    val contenido: String = "",
    val actividad: String = "",
    val aprendizaje: String = "",
    val dudas: String = "",
    val descripcionEvidencia: String = "",
    val enlace: String = "",
    val evidencias: List<Evidencia> = emptyList(),
    val confirmaEnvio: Boolean = false,      // checkbox "revisé mi registro"

    // Estados de error
    val errorFecha: String = "",             // "" = sin error; si no, el mensaje
    val errorContenido: Boolean = false,
    val errorActividad: Boolean = false,
    val errorAprendizaje: Boolean = false,
    val errorEnlace: Boolean = false,
    val errorMaximoEvidencias: Boolean = false,
    val errorConfirmacion: Boolean = false,

    // Estado de la pantalla
    val soloLectura: Boolean = false,        // ya enviado o bitácora cerrada
    val sinAcceso: Boolean = false,
    val mensaje: String = "",
    val mensajeEsError: Boolean = false
)
