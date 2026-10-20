package cl.duoc.bitacora_escolar.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "asignaturas")
data class Asignatura(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String           // ej: "Lenguaje"
)
