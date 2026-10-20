package cl.duoc.bitacora_escolar.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

// Bitácora mensual de una asignación (un docente + un curso + una asignatura).
// asignacionId es CLAVE FORÁNEA: el curso, la asignatura y el docente se obtienen
// desde la tabla asignaciones (no se repiten aquí → 3FN).
// El índice único impide dos bitácoras de la misma asignación en el mismo mes.
@Entity(
    tableName = "bitacoras",
    foreignKeys = [
        ForeignKey(entity = Asignacion::class, parentColumns = ["id"], childColumns = ["asignacionId"])
    ],
    indices = [Index(value = ["asignacionId", "mes", "anio"], unique = true)]
)
data class Bitacora(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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
