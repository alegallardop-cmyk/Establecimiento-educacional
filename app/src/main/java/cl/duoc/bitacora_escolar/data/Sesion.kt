package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.model.Perfil
import cl.duoc.bitacora_escolar.model.Usuario

// Guarda en memoria quién inició sesión y qué eligió en la pantalla anterior.
// Es un "object": existe una sola Sesion para toda la app.
object Sesion {

    var usuario: Usuario? = null

    // Lo que el usuario tocó en una lista, para que la siguiente pantalla sepa qué mostrar
    var bitacoraSeleccionada: Int = 0
    var registroSeleccionado: Int = 0      // 0 = registro nuevo

    fun idUsuario(): Int = usuario?.id ?: 0

    fun esDocente(): Boolean = usuario?.perfil == Perfil.DOCENTE

    fun esEstudiante(): Boolean = usuario?.perfil == Perfil.ESTUDIANTE

    fun cerrar() {
        usuario = null
        bitacoraSeleccionada = 0
        registroSeleccionado = 0
    }
}
