package cl.duoc.bitacora_escolar.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.bitacora_escolar.model.Recordatorio
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {

    @Insert
    suspend fun insertar(recordatorio: Recordatorio)

    @Query("SELECT * FROM recordatorios WHERE docenteId = :docenteId AND estado = 'PENDIENTE'")
    fun observarPendientesDeDocente(docenteId: Int): Flow<List<Recordatorio>>

    @Query("UPDATE recordatorios SET estado = :estado WHERE id = :id")
    suspend fun cambiarEstado(id: Int, estado: String)
}
