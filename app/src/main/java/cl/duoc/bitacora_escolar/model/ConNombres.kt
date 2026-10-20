package cl.duoc.bitacora_escolar.model

import androidx.room3.Embedded

// Estas clases NO son tablas. Son el resultado de una consulta con JOIN:
// los datos de la tabla (@Embedded) + los nombres que vienen de otras tablas.
// Así los nombres se guardan UNA sola vez (en cursos, asignaturas y usuarios).

data class AsignacionConNombres(
    @Embedded val asignacion: Asignacion,
    val curso: String,
    val asignatura: String
)

data class BitacoraConNombres(
    @Embedded val bitacora: Bitacora,
    val docenteId: Int,
    val cursoId: Int,
    val curso: String,
    val asignatura: String
)

data class RegistroConEstudiante(
    @Embedded val registro: Registro,
    val estudiante: String
)
