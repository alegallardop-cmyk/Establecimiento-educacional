package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.model.Recordatorio
import kotlinx.coroutines.flow.Flow

class RecordatorioRepository(private val recordatorioDao: RecordatorioDao) {

    suspend fun guardar(recordatorio: Recordatorio) {
        recordatorioDao.insertar(recordatorio)
    }

    fun observarPendientesDeDocente(docenteId: Int): Flow<List<Recordatorio>> {
        return recordatorioDao.observarPendientesDeDocente(docenteId)
    }

    suspend fun cambiarEstado(id: Int, estado: String) {
        recordatorioDao.cambiarEstado(id, estado)
    }
}
