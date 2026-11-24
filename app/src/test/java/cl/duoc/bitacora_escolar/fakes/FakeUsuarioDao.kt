package cl.duoc.bitacora_escolar.fakes

import cl.duoc.bitacora_escolar.data.UsuarioDao
import cl.duoc.bitacora_escolar.model.Perfil
import cl.duoc.bitacora_escolar.model.Usuario

// "Room falso": guarda los usuarios en una lista en memoria (no necesita un teléfono)
class FakeUsuarioDao : UsuarioDao {

    val usuarios = mutableListOf<Usuario>()
    var fallar = false          // true = simula que la base de datos falla

    override suspend fun insertar(usuario: Usuario): Long {
        val nuevo = usuario.copy(id = usuarios.size + 1)
        usuarios.add(nuevo)
        return nuevo.id.toLong()
    }

    override suspend fun buscarPorCorreo(correo: String): Usuario? {
        if (fallar) throw IllegalStateException("Base de datos no disponible")
        return usuarios.find { it.correo == correo }
    }

    override suspend fun obtenerPorId(id: Int): Usuario? {
        return usuarios.find { it.id == id }
    }

    override suspend fun obtenerEstudiantesDeCurso(cursoId: Int): List<Usuario> {
        return usuarios.filter { it.perfil == Perfil.ESTUDIANTE && it.cursoId == cursoId }.sortedBy { it.nombre }
    }

    override suspend fun contar(): Int {
        return usuarios.size
    }
}
