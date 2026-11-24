package cl.duoc.bitacora_escolar.model

// Bitácora mensual de una asignación (un docente + un curso + una asignatura).
// Ya NO es una tabla de Room: se guarda en el MICROSERVICIO (Spring Boot + MySQL)
// y llega a la app como JSON por Retrofit (Gson lo convierte en este data class).
// asignacionId apunta a la tabla asignaciones que sigue en Room.
data class Bitacora(
    val id: Int = 0,               // 0 = todavía no existe en el servidor
    val asignacionId: Int,
    val mes: Int,                  // 1 a 12
    val anio: Int,
    val titulo: String,
    val proposito: String,
    val instrucciones: String,
    val preguntas: String,         // preguntas orientadoras, una por línea
    val estado: String = EstadoBitacora.BORRADOR,
    val fechaPublicacion: String = "",
    val fechaCierre: String = ""
)
