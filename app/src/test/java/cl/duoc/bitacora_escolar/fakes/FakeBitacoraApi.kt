package cl.duoc.bitacora_escolar.fakes

import cl.duoc.bitacora_escolar.data.remote.BitacoraApi
import cl.duoc.bitacora_escolar.model.Bitacora
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException

// "Microservicio falso": responde como el backend, pero con una lista en memoria.
// Así las pruebas no necesitan Spring Boot ni internet.
class FakeBitacoraApi(iniciales: List<Bitacora>) : BitacoraApi {

    val bitacoras = iniciales.toMutableList()

    var sinConexion = false     // true = simula que el servidor está apagado (IOException)
    var rechazar = false        // true = simula que el servidor responde 409 (regla de negocio)

    private fun revisarConexion() {
        if (sinConexion) throw IOException("No se pudo conectar con el servidor")
    }

    private fun <T> respuestaError(): Response<T> {
        return Response.error(409, "{\"error\":\"Regla de negocio\"}".toResponseBody(null))
    }

    private fun posicion(id: Int): Int {
        return bitacoras.indexOfFirst { it.id == id }
    }

    override suspend fun listar(): List<Bitacora> {
        revisarConexion()
        return bitacoras.toList()
    }

    override suspend fun buscar(id: Int): Bitacora {
        revisarConexion()
        return bitacoras.first { it.id == id }
    }

    override suspend fun crear(bitacora: Bitacora): Response<Bitacora> {
        revisarConexion()
        if (rechazar) return respuestaError()
        val publicar = bitacora.estado == EstadoBitacora.PUBLICADA
        val nueva = bitacora.copy(
            id = (bitacoras.maxOfOrNull { it.id } ?: 0) + 1,
            fechaPublicacion = if (publicar) "19/11/2026" else ""
        )
        bitacoras.add(nueva)
        return Response.success(nueva)
    }

    override suspend fun actualizar(id: Int, bitacora: Bitacora): Response<Bitacora> {
        revisarConexion()
        if (rechazar) return respuestaError()
        val i = posicion(id)
        // Igual que el backend: no cambia la asignación ni el estado
        bitacoras[i] = bitacoras[i].copy(
            mes = bitacora.mes,
            anio = bitacora.anio,
            titulo = bitacora.titulo,
            proposito = bitacora.proposito,
            instrucciones = bitacora.instrucciones,
            preguntas = bitacora.preguntas
        )
        return Response.success(bitacoras[i])
    }

    override suspend fun publicar(id: Int): Response<Bitacora> {
        revisarConexion()
        val i = posicion(id)
        bitacoras[i] = bitacoras[i].copy(estado = EstadoBitacora.PUBLICADA, fechaPublicacion = "19/11/2026")
        return Response.success(bitacoras[i])
    }

    override suspend fun cerrar(id: Int): Response<Bitacora> {
        revisarConexion()
        val i = posicion(id)
        bitacoras[i] = bitacoras[i].copy(estado = EstadoBitacora.CERRADA, fechaCierre = "19/11/2026")
        return Response.success(bitacoras[i])
    }

    override suspend fun eliminar(id: Int): Response<Void> {
        revisarConexion()
        bitacoras.removeAt(posicion(id))
        return Response.success<Void>(null)
    }
}
