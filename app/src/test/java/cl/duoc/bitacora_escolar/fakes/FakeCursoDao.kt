package cl.duoc.bitacora_escolar.fakes

import cl.duoc.bitacora_escolar.data.CursoDao
import cl.duoc.bitacora_escolar.model.Asignacion
import cl.duoc.bitacora_escolar.model.AsignacionConNombres
import cl.duoc.bitacora_escolar.model.Asignatura
import cl.duoc.bitacora_escolar.model.Curso

// "Room falso" para cursos, asignaturas y asignaciones.
// Hace a mano lo mismo que el JOIN de la consulta real.
class FakeCursoDao : CursoDao {

    val cursos = mutableListOf<Curso>()
    val asignaturas = mutableListOf<Asignatura>()
    val asignaciones = mutableListOf<Asignacion>()

    override suspend fun insertarCurso(curso: Curso): Long {
        val nuevo = curso.copy(id = cursos.size + 1)
        cursos.add(nuevo)
        return nuevo.id.toLong()
    }

    override suspend fun insertarAsignatura(asignatura: Asignatura): Long {
        val nueva = asignatura.copy(id = asignaturas.size + 1)
        asignaturas.add(nueva)
        return nueva.id.toLong()
    }

    override suspend fun insertarAsignacion(asignacion: Asignacion): Long {
        val nueva = asignacion.copy(id = asignaciones.size + 1)
        asignaciones.add(nueva)
        return nueva.id.toLong()
    }

    override suspend fun obtenerCurso(id: Int): Curso? {
        return cursos.find { it.id == id }
    }

    override suspend fun obtenerAsignacionesDeDocente(docenteId: Int): List<AsignacionConNombres> {
        return asignaciones.filter { it.docenteId == docenteId }.map { conNombres(it) }.sortedBy { it.curso }
    }

    override suspend fun obtenerAsignacionesDeCurso(cursoId: Int): List<AsignacionConNombres> {
        return asignaciones.filter { it.cursoId == cursoId }.map { conNombres(it) }.sortedBy { it.asignatura }
    }

    override suspend fun obtenerAsignacion(id: Int): AsignacionConNombres? {
        val asignacion = asignaciones.find { it.id == id } ?: return null
        return conNombres(asignacion)
    }

    // El "JOIN": busca los nombres del curso y de la asignatura
    private fun conNombres(asignacion: Asignacion): AsignacionConNombres {
        val curso = cursos.first { it.id == asignacion.cursoId }.nombre
        val asignatura = asignaturas.first { it.id == asignacion.asignaturaId }.nombre
        return AsignacionConNombres(asignacion, curso, asignatura)
    }
}
