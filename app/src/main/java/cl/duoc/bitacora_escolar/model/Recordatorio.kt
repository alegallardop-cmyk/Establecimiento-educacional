package cl.duoc.bitacora_escolar.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

// Recordatorio interno del docente (se muestra dentro de la app).
// docenteId es CLAVE FORÁNEA a usuarios.
@Entity(
    tableName = "recordatorios",
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["docenteId"])
    ],
    indices = [Index("docenteId")]
)
data class Recordatorio(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val docenteId: Int,
    val tipo: String,              // TipoRecordatorio
    val descripcion: String,
    val fechaProgramada: String,   // dd/mm/aaaa
    val estado: String = EstadoRecordatorio.PENDIENTE
)
