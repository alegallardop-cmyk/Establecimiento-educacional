package cl.duoc.bitacora_escolar.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.bitacora_escolar.model.Asignacion
import cl.duoc.bitacora_escolar.model.AsignacionConNombres
import cl.duoc.bitacora_escolar.model.Asignatura
import cl.duoc.bitacora_escolar.model.Curso

// Cursos, asignaturas y la relación docente - curso - asignatura
@Dao
interface CursoDao {

    @Insert
    suspend fun insertarCurso(curso: Curso): Long

    @Insert
    suspend fun insertarAsignatura(asignatura: Asignatura): Long

    @Insert
    suspend fun insertarAsignacion(asignacion: Asignacion): Long

    @Query("SELECT * FROM cursos WHERE id = :id")
    suspend fun obtenerCurso(id: Int): Curso?

    // JOIN: une cada asignación con el nombre de su curso y de su asignatura
    @Query(
        "SELECT asignaciones.*, cursos.nombre AS curso, asignaturas.nombre AS asignatura " +
        "FROM asignaciones " +
        "INNER JOIN cursos ON cursos.id = asignaciones.cursoId " +
        "INNER JOIN asignaturas ON asignaturas.id = asignaciones.asignaturaId " +
        "WHERE asignaciones.docenteId = :docenteId ORDER BY cursos.nombre"
    )
    suspend fun obtenerAsignacionesDeDocente(docenteId: Int): List<AsignacionConNombres>

    @Query(
        "SELECT asignaciones.*, cursos.nombre AS curso, asignaturas.nombre AS asignatura " +
        "FROM asignaciones " +
        "INNER JOIN cursos ON cursos.id = asignaciones.cursoId " +
        "INNER JOIN asignaturas ON asignaturas.id = asignaciones.asignaturaId " +
        "WHERE asignaciones.cursoId = :cursoId ORDER BY asignaturas.nombre"
    )
    suspend fun obtenerAsignacionesDeCurso(cursoId: Int): List<AsignacionConNombres>
}
