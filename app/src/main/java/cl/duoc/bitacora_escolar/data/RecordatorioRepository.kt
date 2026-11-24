package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.model.Recordatorio

// Recordatorios internos del docente (siguen en Room)
class RecordatorioRepository(private val recordatorioDao: RecordatorioDao) {

    suspend fun guardar(recordatorio: Recordatorio) {
        recordatorioDao.insertar(recordatorio)
    }

    suspend fun pendientesDeDocente(docenteId: Int): List<Recordatorio> {
        return recordatorioDao.obtenerPendientesDeDocente(docenteId)
    }

    suspend fun cambiarEstado(id: Int, estado: String) {
        recordatorioDao.cambiarEstado(id, estado)
    }
}
