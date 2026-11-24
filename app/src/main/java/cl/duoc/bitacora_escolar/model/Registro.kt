package cl.duoc.bitacora_escolar.model

// Registro (aporte) que hace un estudiante después de una clase.
// También guarda la retroalimentación del docente.
// Se guarda en el MICROSERVICIO (JSON). Las evidencias vienen dentro del mismo registro.
// El nombre del estudiante NO viene del servidor: se busca en Room con estudianteId.
data class Registro(
    val id: Int = 0,               // 0 = registro nuevo
    val bitacoraId: Int,
    val estudianteId: Int,
    val fechaClase: String,        // formato dd/mm/aaaa
    val contenido: String,         // contenido trabajado
    val actividad: String,         // actividad realizada
    val aprendizaje: String,       // qué aprendió
    val dudas: String = "",        // dudas o dificultades (opcional)
    val estadoEnvio: String = EstadoEnvio.BORRADOR,
    val estadoRevision: String = EstadoRevision.PENDIENTE,
    val fechaCreacion: String = "",
    val fechaEnvio: String = "",

    // Retroalimentación del docente
    val comentarioDocente: String = "",
    val fechaRetroalimentacion: String = "",
    val docenteRevisorId: Int? = null,  // null = todavía nadie lo revisa

    // Fotos y enlaces (máximo 5)
    val evidencias: List<Evidencia> = emptyList()
)
