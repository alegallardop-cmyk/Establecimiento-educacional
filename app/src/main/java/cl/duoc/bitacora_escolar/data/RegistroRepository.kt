package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.model.Evidencia
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.model.RegistroConEstudiante
import kotlinx.coroutines.flow.Flow

// Registros de los estudiantes y sus evidencias (fotos y enlaces)
class RegistroRepository(
    private val registroDao: RegistroDao,
    private val evidenciaDao: EvidenciaDao
) {

    // Si el registro es nuevo (id = 0) se inserta; si ya existía se actualiza.
    // Luego se reemplazan sus evidencias. Devuelve el id del registro.
    suspend fun guardar(registro: Registro, evidencias: List<Evidencia>): Int {
        val id: Int
        if (registro.id == 0) {
            id = registroDao.insertar(registro).toInt()
        } else {
            id = registro.id
            registroDao.actualizarContenido(
                id = registro.id,
                fechaClase = registro.fechaClase,
                contenido = registro.contenido,
                actividad = registro.actividad,
                aprendizaje = registro.aprendizaje,
                dudas = registro.dudas,
                estadoEnvio = registro.estadoEnvio,
                fechaEnvio = registro.fechaEnvio
            )
        }
        evidenciaDao.eliminarDeRegistro(id)
        for (evidencia in evidencias) {
            evidenciaDao.insertar(evidencia.copy(id = 0, registroId = id))
        }
        return id
    }

    suspend fun obtenerPorId(id: Int): RegistroConEstudiante? {
        return registroDao.obtenerPorId(id)
    }

    suspend fun obtenerEvidencias(registroId: Int): List<Evidencia> {
        return evidenciaDao.obtenerDeRegistro(registroId)
    }

    fun observarEnviadosDeBitacora(bitacoraId: Int): Flow<List<RegistroConEstudiante>> {
        return registroDao.observarEnviadosDeBitacora(bitacoraId)
    }

    suspend fun obtenerEnviadosDeBitacora(bitacoraId: Int): List<Registro> {
        return registroDao.obtenerEnviadosDeBitacora(bitacoraId)
    }

    fun observarDeEstudianteEnBitacora(bitacoraId: Int, estudianteId: Int): Flow<List<Registro>> {
        return registroDao.observarDeEstudianteEnBitacora(bitacoraId, estudianteId)
    }

    fun observarDeEstudiante(estudianteId: Int): Flow<List<Registro>> {
        return registroDao.observarDeEstudiante(estudianteId)
    }

    fun observarPendientesDeDocente(docenteId: Int): Flow<Int> {
        return registroDao.observarPendientesDeDocente(docenteId)
    }

    suspend fun guardarRetroalimentacion(id: Int, comentario: String, fecha: String, docenteId: Int, estadoRevision: String) {
        registroDao.guardarRetroalimentacion(id, comentario, fecha, docenteId, estadoRevision)
    }
}
