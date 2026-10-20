package cl.duoc.bitacora_escolar.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "cursos")
data class Curso(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String           // ej: "7° Básico A"
)
