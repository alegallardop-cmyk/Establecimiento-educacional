package cl.duoc.bitacora_escolar.fakes

import cl.duoc.bitacora_escolar.data.remote.RegistroApi
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.model.RetroalimentacionRequest
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException

// "Microservicio falso" de registros (lista en memoria)
class FakeRegistroApi(iniciales: List<Registro>) : RegistroApi {

    val registros = iniciales.toMutableList()

    var sinConexion = false     // simula servidor apagado
    var rechazar = false        // simula respuesta 409

    private fun revisarConexion() {
        if (sinConexion) throw IOException("No se pudo conectar con el servidor")
    }

    private fun <T> respuestaError(): Response<T> {
        return Response.error(409, "{\"error\":\"Regla de negocio\"}".toResponseBody(null))
    }

    private fun posicion(id: Int): Int {
        return registros.indexOfFirst { it.id == id }
    }

    // Igual que el backend: filtra por bitácora, o por estudiante (del más nuevo al más antiguo), o todos
    override suspend fun listar(bitacoraId: Int?, estudianteId: Int?): List<Registro> {
        revisarConexion()
        if (bitacoraId != null) return registros.filter { it.bitacoraId == bitacoraId }
        if (estudianteId != null) return registros.filter { it.estudianteId == estudianteId }.sortedByDescending { it.id }
        return registros.toList()
    }

    override suspend fun buscar(id: Int): Registro {
        revisarConexion()
        return registros.first { it.id == id }
    }

    override suspend fun crear(registro: Registro): Response<Registro> {
        revisarConexion()
        if (rechazar) return respuestaError()
        val nuevo = registro.copy(
            id = (registros.maxOfOrNull { it.id } ?: 0) + 1,
            estadoRevision = EstadoRevision.PENDIENTE,
            fechaEnvio = if (registro.estadoEnvio == EstadoEnvio.ENVIADO) "19/11/2026" else ""
        )
        registros.add(nuevo)
        return Response.success(nuevo)
    }

    override suspend fun actualizar(id: Int, registro: Registro): Response<Registro> {
        revisarConexion()
        if (rechazar) return respuestaError()
        val i = posicion(id)
        registros[i] = registro.copy(id = id)
        return Response.success(registros[i])
    }

    override suspend fun retroalimentar(id: Int, datos: RetroalimentacionRequest): Response<Registro> {
        revisarConexion()
        if (rechazar) return respuestaError()
        val i = posicion(id)
        registros[i] = registros[i].copy(
            comentarioDocente = datos.comentario,
            docenteRevisorId = datos.docenteRevisorId,
            estadoRevision = datos.estadoRevision,
            fechaRetroalimentacion = "19/11/2026"
        )
        return Response.success(registros[i])
    }

    // Igual que el backend: un registro ENVIADO no se puede eliminar (409)
    override suspend fun eliminar(id: Int): Response<Void> {
        revisarConexion()
        val i = posicion(id)
        if (registros[i].estadoEnvio == EstadoEnvio.ENVIADO) return respuestaError()
        registros.removeAt(i)
        return Response.success<Void>(null)
    }
}
