package cl.duoc.bitacora_escolar.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

// Registro (aporte) que hace un estudiante después de una clase.
// También guarda la retroalimentación del docente.
// CLAVES FORÁNEAS: bitacoraId → bitacoras, estudianteId y docenteRevisorId → usuarios.
// El nombre del estudiante NO se guarda aquí: se obtiene con un JOIN (3FN).
@Entity(
    tableName = "registros",
    foreignKeys = [
        ForeignKey(entity = Bitacora::class, parentColumns = ["id"], childColumns = ["bitacoraId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["estudianteId"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["docenteRevisorId"])
    ],
    indices = [Index("bitacoraId"), Index("estudianteId"), Index("docenteRevisorId")]
)
data class Registro(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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
    val docenteRevisorId: Int? = null   // null = todavía nadie lo revisa
)
