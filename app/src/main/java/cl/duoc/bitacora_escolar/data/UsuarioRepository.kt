package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.model.Usuario

// DEFINIMOS EL CÓMO LO VAMOS A HACER
class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val cursoDao: CursoDao,
    private val recordatorioDao: RecordatorioDao
) {

    // La primera vez se cargan los usuarios, cursos y recordatorios ficticios en Room
    suspend fun prepararDatos() {
        DatosDePrueba.cargarSiEstaVacia(usuarioDao, cursoDao, recordatorioDao)
    }

    suspend fun buscarPorCorreo(correo: String): Usuario? {
        return usuarioDao.buscarPorCorreo(correo.trim().lowercase())
    }

    suspend fun obtenerEstudiantesDeCurso(cursoId: Int): List<Usuario> {
        return usuarioDao.obtenerEstudiantesDeCurso(cursoId)
    }
}
