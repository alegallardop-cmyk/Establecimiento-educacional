package cl.duoc.bitacora_escolar.model

import androidx.room3.Embedded

// Estas clases NO son tablas. Juntan los datos con los NOMBRES que están en otras tablas.

// Resultado de un JOIN en Room: asignación + nombre del curso + nombre de la asignatura
data class AsignacionConNombres(
    @Embedded val asignacion: Asignacion,
    val curso: String,
    val asignatura: String
)

// Bitácora (viene del microservicio) + nombres de su asignación (vienen de Room).
// La arma BitacoraRepository.
data class BitacoraConNombres(
    val bitacora: Bitacora,
    val docenteId: Int,
    val cursoId: Int,
    val curso: String,
    val asignatura: String
)

// Registro (viene del microservicio) + nombre del estudiante (viene de Room).
// La arma RegistroRepository.
data class RegistroConEstudiante(
    val registro: Registro,
    val estudiante: String
)
