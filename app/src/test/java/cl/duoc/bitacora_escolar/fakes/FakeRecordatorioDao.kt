package cl.duoc.bitacora_escolar.fakes

import cl.duoc.bitacora_escolar.data.RecordatorioDao
import cl.duoc.bitacora_escolar.model.EstadoRecordatorio
import cl.duoc.bitacora_escolar.model.Recordatorio

class FakeRecordatorioDao : RecordatorioDao {

    val recordatorios = mutableListOf<Recordatorio>()

    override suspend fun insertar(recordatorio: Recordatorio) {
        recordatorios.add(recordatorio.copy(id = recordatorios.size + 1))
    }

    override suspend fun obtenerPendientesDeDocente(docenteId: Int): List<Recordatorio> {
        return recordatorios.filter { it.docenteId == docenteId && it.estado == EstadoRecordatorio.PENDIENTE }
    }

    override suspend fun cambiarEstado(id: Int, estado: String) {
        val posicion = recordatorios.indexOfFirst { it.id == id }
        if (posicion >= 0) {
            recordatorios[posicion] = recordatorios[posicion].copy(estado = estado)
        }
    }
}
