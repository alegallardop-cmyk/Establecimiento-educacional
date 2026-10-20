package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.model.Bitacora
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import kotlinx.coroutines.flow.Flow

class BitacoraRepository(private val bitacoraDao: BitacoraDao) {

    // Si la bitácora es nueva (id = 0) se inserta; si ya existía se actualiza.
    suspend fun guardar(bitacora: Bitacora) {
        if (bitacora.id == 0) {
            bitacoraDao.insertar(bitacora)
        } else {
            bitacoraDao.actualizar(
                id = bitacora.id,
                mes = bitacora.mes,
                anio = bitacora.anio,
                titulo = bitacora.titulo,
                proposito = bitacora.proposito,
                instrucciones = bitacora.instrucciones,
                preguntas = bitacora.preguntas
            )
        }
    }

    suspend fun obtenerPorId(id: Int): BitacoraConNombres? {
        return bitacoraDao.obtenerPorId(id)
    }

    fun observarPorId(id: Int): Flow<BitacoraConNombres?> {
        return bitacoraDao.observarPorId(id)
    }

    fun observarDeDocente(docenteId: Int): Flow<List<BitacoraConNombres>> {
        return bitacoraDao.observarDeDocente(docenteId)
    }

    fun observarVisiblesDeCurso(cursoId: Int): Flow<List<BitacoraConNombres>> {
        return bitacoraDao.observarVisiblesDeCurso(cursoId)
    }

    suspend fun existe(asignacionId: Int, mes: Int, anio: Int, idExcluir: Int = 0): Boolean {
        return bitacoraDao.contarIguales(asignacionId, mes, anio, idExcluir) > 0
    }

    suspend fun publicar(id: Int, fecha: String) {
        bitacoraDao.publicar(id, fecha)
    }

    suspend fun cerrar(id: Int, fecha: String) {
        bitacoraDao.cerrar(id, fecha)
    }
}
