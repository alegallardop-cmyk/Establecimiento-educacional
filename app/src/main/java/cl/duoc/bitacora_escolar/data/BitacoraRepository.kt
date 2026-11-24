package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.data.remote.BitacoraApi
import cl.duoc.bitacora_escolar.data.remote.comprobarRespuesta
import cl.duoc.bitacora_escolar.model.AsignacionConNombres
import cl.duoc.bitacora_escolar.model.Bitacora
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.EstadoBitacora

// Bitácoras: se leen y se guardan en el MICROSERVICIO (api).
// Los nombres del curso y la asignatura se buscan en ROOM (cursoDao).
class BitacoraRepository(
    private val api: BitacoraApi,
    private val cursoDao: CursoDao
) {

    // ---------- Leer ----------

    // Docente: solo las bitácoras de SUS asignaciones
    suspend fun obtenerDeDocente(docenteId: Int): List<BitacoraConNombres> {
        val asignaciones = cursoDao.obtenerAsignacionesDeDocente(docenteId)
        return unirConNombres(api.listar(), asignaciones)
    }

    // Estudiante: solo las publicadas o cerradas de SU curso
    suspend fun obtenerVisiblesDeCurso(cursoId: Int): List<BitacoraConNombres> {
        val asignaciones = cursoDao.obtenerAsignacionesDeCurso(cursoId)
        val visibles = api.listar().filter { it.estado != EstadoBitacora.BORRADOR }
        return unirConNombres(visibles, asignaciones)
    }

    // null = la asignación de la bitácora no existe en este teléfono
    suspend fun obtenerPorId(id: Int): BitacoraConNombres? {
        val bitacora = api.buscar(id)
        val asignacion = cursoDao.obtenerAsignacion(bitacora.asignacionId) ?: return null
        return conNombres(bitacora, asignacion)
    }

    // Para no repetir la misma asignación en el mismo mes (al editar no se cuenta a sí misma)
    suspend fun existe(asignacionId: Int, mes: Int, anio: Int, idExcluir: Int = 0): Boolean {
        return api.listar().any {
            it.asignacionId == asignacionId && it.mes == mes && it.anio == anio && it.id != idExcluir
        }
    }

    // ---------- Guardar (POST / PUT / DELETE) ----------

    // id = 0 -> POST (crear). Otro id -> PUT (actualizar).
    suspend fun guardar(bitacora: Bitacora) {
        val respuesta = if (bitacora.id == 0) {
            api.crear(bitacora)
        } else {
            api.actualizar(bitacora.id, bitacora)
        }
        comprobarRespuesta(respuesta)
    }

    suspend fun publicar(id: Int) {
        comprobarRespuesta(api.publicar(id))
    }

    suspend fun cerrar(id: Int) {
        comprobarRespuesta(api.cerrar(id))
    }

    suspend fun eliminar(id: Int) {
        comprobarRespuesta(api.eliminar(id))
    }

    // ---------- Ayudantes ----------

    private fun conNombres(bitacora: Bitacora, asignacion: AsignacionConNombres): BitacoraConNombres {
        return BitacoraConNombres(
            bitacora = bitacora,
            docenteId = asignacion.asignacion.docenteId,
            cursoId = asignacion.asignacion.cursoId,
            curso = asignacion.curso,
            asignatura = asignacion.asignatura
        )
    }

    // Deja solo las bitácoras cuya asignación está en la lista y les agrega los nombres
    private fun unirConNombres(
        bitacoras: List<Bitacora>,
        asignaciones: List<AsignacionConNombres>
    ): List<BitacoraConNombres> {
        val resultado = mutableListOf<BitacoraConNombres>()
        for (bitacora in bitacoras) {
            val asignacion = asignaciones.find { it.asignacion.id == bitacora.asignacionId }
            if (asignacion != null) {
                resultado.add(conNombres(bitacora, asignacion))
            }
        }
        return resultado
    }
}
