package cl.duoc.bitacora_escolar.model

// Lo que se envía en el body de PUT /registros/{id}/retroalimentacion
// (los nombres deben ser iguales a los del DTO del backend)
data class RetroalimentacionRequest(
    val comentario: String,
    val docenteRevisorId: Int,
    val estadoRevision: String
)
