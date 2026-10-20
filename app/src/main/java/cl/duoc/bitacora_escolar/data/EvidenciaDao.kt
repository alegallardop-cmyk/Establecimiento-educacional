package cl.duoc.bitacora_escolar.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.bitacora_escolar.model.Evidencia

@Dao
interface EvidenciaDao {

    @Insert
    suspend fun insertar(evidencia: Evidencia)

    @Query("SELECT * FROM evidencias WHERE registroId = :registroId")
    suspend fun obtenerDeRegistro(registroId: Int): List<Evidencia>

    @Query("DELETE FROM evidencias WHERE registroId = :registroId")
    suspend fun eliminarDeRegistro(registroId: Int)
}
