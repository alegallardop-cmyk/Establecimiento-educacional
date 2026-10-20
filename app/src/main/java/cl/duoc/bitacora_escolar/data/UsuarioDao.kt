package cl.duoc.bitacora_escolar.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.bitacora_escolar.model.Usuario

// Configuramos el QUÉ VAMOS A HACER
@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE perfil = 'ESTUDIANTE' AND cursoId = :cursoId ORDER BY nombre")
    suspend fun obtenerEstudiantesDeCurso(cursoId: Int): List<Usuario>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}
