package cl.duoc.bitacora_escolar.data.remote

import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.model.RetroalimentacionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints del microservicio de registros (http://10.0.2.2:8080/registros)
interface RegistroApi {

    // @Query agrega ?bitacoraId=2 o ?estudianteId=3 a la URL (si es null, no se agrega)
    @GET("registros")
    suspend fun listar(
        @Query("bitacoraId") bitacoraId: Int?,
        @Query("estudianteId") estudianteId: Int?
    ): List<Registro>

    @GET("registros/{id}")
    suspend fun buscar(@Path("id") id: Int): Registro

    @POST("registros")
    suspend fun crear(@Body registro: Registro): Response<Registro>

    @PUT("registros/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body registro: Registro): Response<Registro>

    @PUT("registros/{id}/retroalimentacion")
    suspend fun retroalimentar(@Path("id") id: Int, @Body datos: RetroalimentacionRequest): Response<Registro>

    @DELETE("registros/{id}")
    suspend fun eliminar(@Path("id") id: Int): Response<Void>
}
