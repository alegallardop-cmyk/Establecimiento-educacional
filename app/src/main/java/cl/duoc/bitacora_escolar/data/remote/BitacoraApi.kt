package cl.duoc.bitacora_escolar.data.remote

import cl.duoc.bitacora_escolar.model.Bitacora
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Endpoints del microservicio de bitácoras (http://10.0.2.2:8080/bitacoras)
interface BitacoraApi {

    @GET("bitacoras")
    suspend fun listar(): List<Bitacora>

    @GET("bitacoras/{id}")
    suspend fun buscar(@Path("id") id: Int): Bitacora

    @POST("bitacoras")
    suspend fun crear(@Body bitacora: Bitacora): Response<Bitacora>

    @PUT("bitacoras/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body bitacora: Bitacora): Response<Bitacora>

    @PUT("bitacoras/{id}/publicar")
    suspend fun publicar(@Path("id") id: Int): Response<Bitacora>

    @PUT("bitacoras/{id}/cerrar")
    suspend fun cerrar(@Path("id") id: Int): Response<Bitacora>

    @DELETE("bitacoras/{id}")
    suspend fun eliminar(@Path("id") id: Int): Response<Void>
}
