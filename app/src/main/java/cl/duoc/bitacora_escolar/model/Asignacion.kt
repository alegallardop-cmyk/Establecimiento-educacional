package cl.duoc.bitacora_escolar.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

// Relación docente - curso - asignatura.
// Ej: "Ana Fuentes hace Lenguaje en 7° Básico A".
// Tiene 3 CLAVES FORÁNEAS: a usuarios, a cursos y a asignaturas.
@Entity(
    tableName = "asignaciones",
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["docenteId"]),
        ForeignKey(entity = Curso::class, parentColumns = ["id"], childColumns = ["cursoId"]),
        ForeignKey(entity = Asignatura::class, parentColumns = ["id"], childColumns = ["asignaturaId"])
    ],
    indices = [Index("docenteId"), Index("cursoId"), Index("asignaturaId")]
)
data class Asignacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val docenteId: Int,
    val cursoId: Int,
    val asignaturaId: Int
)
