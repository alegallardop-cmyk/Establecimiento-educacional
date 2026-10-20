package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.model.Usuario

// DEFINIMOS EL CÓMO LO VAMOS A HACER
class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun buscarPorCorreo(correo: String): Usuario? {
        return usuarioDao.buscarPorCorreo(correo.trim().lowercase())
    }

    suspend fun obtenerEstudiantesDeCurso(cursoId: Int): List<Usuario> {
        return usuarioDao.obtenerEstudiantesDeCurso(cursoId)
    }
}
