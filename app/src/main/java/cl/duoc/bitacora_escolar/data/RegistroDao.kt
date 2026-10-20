package cl.duoc.bitacora_escolar.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.model.RegistroConEstudiante
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {

    @Insert
    suspend fun insertar(registro: Registro): Long

    // JOIN con usuarios: trae el nombre del estudiante junto al registro
    @Query(
        "SELECT registros.*, usuarios.nombre AS estudiante FROM registros " +
        "INNER JOIN usuarios ON usuarios.id = registros.estudianteId " +
        "WHERE registros.id = :id"
    )
    suspend fun obtenerPorId(id: Int): RegistroConEstudiante?

    // Docente: solo ve los ENVIADOS (los borradores son privados del estudiante)
    @Query(
        "SELECT registros.*, usuarios.nombre AS estudiante FROM registros " +
        "INNER JOIN usuarios ON usuarios.id = registros.estudianteId " +
        "WHERE registros.bitacoraId = :bitacoraId AND registros.estadoEnvio = 'ENVIADO'"
    )
    fun observarEnviadosDeBitacora(bitacoraId: Int): Flow<List<RegistroConEstudiante>>

    @Query("SELECT * FROM registros WHERE bitacoraId = :bitacoraId AND estadoEnvio = 'ENVIADO'")
    suspend fun obtenerEnviadosDeBitacora(bitacoraId: Int): List<Registro>

    // Estudiante: solo SUS registros
    @Query("SELECT * FROM registros WHERE bitacoraId = :bitacoraId AND estudianteId = :estudianteId")
    fun observarDeEstudianteEnBitacora(bitacoraId: Int, estudianteId: Int): Flow<List<Registro>>

    @Query("SELECT * FROM registros WHERE estudianteId = :estudianteId ORDER BY id DESC")
    fun observarDeEstudiante(estudianteId: Int): Flow<List<Registro>>

    // Aviso para el docente: aportes pendientes de revisión en sus bitácoras
    @Query(
        "SELECT COUNT(*) FROM registros " +
        "INNER JOIN bitacoras ON bitacoras.id = registros.bitacoraId " +
        "INNER JOIN asignaciones ON asignaciones.id = bitacoras.asignacionId " +
        "WHERE registros.estadoEnvio = 'ENVIADO' AND registros.estadoRevision = 'PENDIENTE' AND asignaciones.docenteId = :docenteId"
    )
    fun observarPendientesDeDocente(docenteId: Int): Flow<Int>

    // Actualizar un borrador (o enviarlo)
    @Query("UPDATE registros SET fechaClase = :fechaClase, contenido = :contenido, actividad = :actividad, aprendizaje = :aprendizaje, dudas = :dudas, estadoEnvio = :estadoEnvio, fechaEnvio = :fechaEnvio WHERE id = :id")
    suspend fun actualizarContenido(
        id: Int,
        fechaClase: String,
        contenido: String,
        actividad: String,
        aprendizaje: String,
        dudas: String,
        estadoEnvio: String,
        fechaEnvio: String
    )

    // Retroalimentación del docente
    @Query("UPDATE registros SET comentarioDocente = :comentario, fechaRetroalimentacion = :fecha, docenteRevisorId = :docenteId, estadoRevision = :estadoRevision WHERE id = :id")
    suspend fun guardarRetroalimentacion(id: Int, comentario: String, fecha: String, docenteId: Int, estadoRevision: String)
}
