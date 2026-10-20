package cl.duoc.bitacora_escolar.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

// Evidencia de un registro: una fotografía (galería o cámara) o un enlace.
// registroId es CLAVE FORÁNEA: si se borra el registro, se borran sus evidencias (CASCADE).
@Entity(
    tableName = "evidencias",
    foreignKeys = [
        ForeignKey(entity = Registro::class, parentColumns = ["id"], childColumns = ["registroId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("registroId")]
)
data class Evidencia(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val registroId: Int = 0,
    val tipo: String,              // TipoEvidencia.FOTO o TipoEvidencia.ENLACE
    val descripcion: String,       // ej: "Foto de mi cuaderno"
    val enlace: String             // FOTO: el URI de la imagen. ENLACE: "https://..."
)
