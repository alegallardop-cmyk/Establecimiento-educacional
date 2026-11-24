package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.data.remote.RegistroApi
import cl.duoc.bitacora_escolar.data.remote.comprobarRespuesta
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.model.RegistroConEstudiante
import cl.duoc.bitacora_escolar.model.RetroalimentacionRequest

// Registros de los estudiantes y sus evidencias: están en el MICROSERVICIO (api).
// El nombre del estudiante se busca en ROOM (usuarioDao).
class RegistroRepository(
    private val api: RegistroApi,
    private val usuarioDao: UsuarioDao
) {

    // ---------- Leer ----------

    suspend fun obtenerPorId(id: Int): RegistroConEstudiante {
        val registro = api.buscar(id)
        return RegistroConEstudiante(registro, nombreEstudiante(registro.estudianteId))
    }

    // Docente: solo los ENVIADOS (los borradores son privados del estudiante)
    suspend fun obtenerEnviadosDeBitacora(bitacoraId: Int): List<RegistroConEstudiante> {
        val enviados = api.listar(bitacoraId, null).filter { it.estadoEnvio == EstadoEnvio.ENVIADO }
        return enviados.map { RegistroConEstudiante(it, nombreEstudiante(it.estudianteId)) }
    }

    // Estudiante: solo SUS registros de una bitácora
    suspend fun obtenerDeEstudianteEnBitacora(bitacoraId: Int, estudianteId: Int): List<Registro> {
        return api.listar(bitacoraId, null).filter { it.estudianteId == estudianteId }
    }

    // Estudiante: todos SUS registros (el servidor los manda del más nuevo al más antiguo)
    suspend fun obtenerDeEstudiante(estudianteId: Int): List<Registro> {
        return api.listar(null, estudianteId)
    }

    // Aviso del docente: aportes enviados y pendientes de revisión en sus bitácoras
    suspend fun contarPendientes(bitacoraIds: List<Int>): Int {
        if (bitacoraIds.isEmpty()) return 0
        return api.listar(null, null).count {
            it.bitacoraId in bitacoraIds &&
                    it.estadoEnvio == EstadoEnvio.ENVIADO &&
                    it.estadoRevision == EstadoRevision.PENDIENTE
        }
    }

    // ---------- Guardar (POST / PUT / DELETE) ----------

    // id = 0 -> POST (crear). Otro id -> PUT (actualizar el borrador o enviarlo).
    // Devuelve el registro como quedó en el servidor (con su id y sus fechas).
    suspend fun guardar(registro: Registro): Registro {
        val respuesta = if (registro.id == 0) {
            api.crear(registro)
        } else {
            api.actualizar(registro.id, registro)
        }
        comprobarRespuesta(respuesta)
        return respuesta.body() ?: registro
    }

    suspend fun guardarRetroalimentacion(id: Int, comentario: String, docenteId: Int, estadoRevision: String) {
        val datos = RetroalimentacionRequest(
            comentario = comentario,
            docenteRevisorId = docenteId,
            estadoRevision = estadoRevision
        )
        comprobarRespuesta(api.retroalimentar(id, datos))
    }

    // Solo se pueden eliminar borradores (el servidor también lo revisa)
    suspend fun eliminar(id: Int) {
        comprobarRespuesta(api.eliminar(id))
    }

    // ---------- Ayudantes ----------

    private suspend fun nombreEstudiante(estudianteId: Int): String {
        return usuarioDao.obtenerPorId(estudianteId)?.nombre ?: "Estudiante"
    }
}
