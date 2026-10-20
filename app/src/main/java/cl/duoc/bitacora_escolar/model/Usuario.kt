package cl.duoc.bitacora_escolar.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

// Usuario ficticio: docente o estudiante. NUNCA datos reales.
// cursoId es CLAVE FORÁNEA a la tabla cursos (solo la usan los estudiantes; docentes = null).
@Entity(
    tableName = "usuarios",
    foreignKeys = [
        ForeignKey(entity = Curso::class, parentColumns = ["id"], childColumns = ["cursoId"])
    ],
    indices = [Index(value = ["correo"], unique = true), Index("cursoId")]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val password: String,
    val perfil: String,          // Perfil.DOCENTE o Perfil.ESTUDIANTE
    val cursoId: Int? = null     // solo estudiantes: su curso
)
