package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.model.AsignacionConNombres

class CursoRepository(private val cursoDao: CursoDao) {

    // Cursos y asignaturas que tiene asignados un docente
    suspend fun asignacionesDeDocente(docenteId: Int): List<AsignacionConNombres> {
        return cursoDao.obtenerAsignacionesDeDocente(docenteId)
    }

    // Asignaturas del curso de un estudiante
    suspend fun asignacionesDeCurso(cursoId: Int): List<AsignacionConNombres> {
        return cursoDao.obtenerAsignacionesDeCurso(cursoId)
    }

    suspend fun nombreCurso(cursoId: Int): String {
        return cursoDao.obtenerCurso(cursoId)?.nombre ?: ""
    }
}
