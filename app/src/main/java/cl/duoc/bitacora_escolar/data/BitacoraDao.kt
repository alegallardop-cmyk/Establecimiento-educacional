package cl.duoc.bitacora_escolar.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.bitacora_escolar.model.Bitacora
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import kotlinx.coroutines.flow.Flow

@Dao
interface BitacoraDao {

    @Insert
    suspend fun insertar(bitacora: Bitacora): Long

    // Las lecturas usan JOIN: bitacoras + asignaciones + cursos + asignaturas,
    // para traer el nombre del curso y de la asignatura (están en sus propias tablas)
    @Query(
        "SELECT bitacoras.*, asignaciones.docenteId AS docenteId, asignaciones.cursoId AS cursoId, " +
        "cursos.nombre AS curso, asignaturas.nombre AS asignatura " +
        "FROM bitacoras " +
        "INNER JOIN asignaciones ON asignaciones.id = bitacoras.asignacionId " +
        "INNER JOIN cursos ON cursos.id = asignaciones.cursoId " +
        "INNER JOIN asignaturas ON asignaturas.id = asignaciones.asignaturaId " +
        "WHERE bitacoras.id = :id"
    )
    suspend fun obtenerPorId(id: Int): BitacoraConNombres?

    @Query(
        "SELECT bitacoras.*, asignaciones.docenteId AS docenteId, asignaciones.cursoId AS cursoId, " +
        "cursos.nombre AS curso, asignaturas.nombre AS asignatura " +
        "FROM bitacoras " +
        "INNER JOIN asignaciones ON asignaciones.id = bitacoras.asignacionId " +
        "INNER JOIN cursos ON cursos.id = asignaciones.cursoId " +
        "INNER JOIN asignaturas ON asignaturas.id = asignaciones.asignaturaId " +
        "WHERE bitacoras.id = :id"
    )
    fun observarPorId(id: Int): Flow<BitacoraConNombres?>

    // Docente: solo SUS bitácoras
    @Query(
        "SELECT bitacoras.*, asignaciones.docenteId AS docenteId, asignaciones.cursoId AS cursoId, " +
        "cursos.nombre AS curso, asignaturas.nombre AS asignatura " +
        "FROM bitacoras " +
        "INNER JOIN asignaciones ON asignaciones.id = bitacoras.asignacionId " +
        "INNER JOIN cursos ON cursos.id = asignaciones.cursoId " +
        "INNER JOIN asignaturas ON asignaturas.id = asignaciones.asignaturaId " +
        "WHERE asignaciones.docenteId = :docenteId ORDER BY bitacoras.anio DESC, bitacoras.mes DESC"
    )
    fun observarDeDocente(docenteId: Int): Flow<List<BitacoraConNombres>>

    // Estudiante: solo bitácoras publicadas o cerradas de SU curso
    @Query(
        "SELECT bitacoras.*, asignaciones.docenteId AS docenteId, asignaciones.cursoId AS cursoId, " +
        "cursos.nombre AS curso, asignaturas.nombre AS asignatura " +
        "FROM bitacoras " +
        "INNER JOIN asignaciones ON asignaciones.id = bitacoras.asignacionId " +
        "INNER JOIN cursos ON cursos.id = asignaciones.cursoId " +
        "INNER JOIN asignaturas ON asignaturas.id = asignaciones.asignaturaId " +
        "WHERE asignaciones.cursoId = :cursoId AND bitacoras.estado != 'BORRADOR' ORDER BY bitacoras.anio DESC, bitacoras.mes DESC"
    )
    fun observarVisiblesDeCurso(cursoId: Int): Flow<List<BitacoraConNombres>>

    // Para no repetir la misma asignación en el mismo mes.
    // idExcluir: al EDITAR no se cuenta la propia bitácora (al crear se envía 0).
    @Query("SELECT COUNT(*) FROM bitacoras WHERE asignacionId = :asignacionId AND mes = :mes AND anio = :anio AND id != :idExcluir")
    suspend fun contarIguales(asignacionId: Int, mes: Int, anio: Int, idExcluir: Int): Int

    // Editar una bitácora (no cambia la asignación ni el estado)
    @Query("UPDATE bitacoras SET mes = :mes, anio = :anio, titulo = :titulo, proposito = :proposito, instrucciones = :instrucciones, preguntas = :preguntas WHERE id = :id")
    suspend fun actualizar(id: Int, mes: Int, anio: Int, titulo: String, proposito: String, instrucciones: String, preguntas: String)

    @Query("UPDATE bitacoras SET estado = 'PUBLICADA', fechaPublicacion = :fecha WHERE id = :id")
    suspend fun publicar(id: Int, fecha: String)

    @Query("UPDATE bitacoras SET estado = 'CERRADA', fechaCierre = :fecha WHERE id = :id")
    suspend fun cerrar(id: Int, fecha: String)
}
