package cl.duoc.bitacora_escolar.data.remote

import cl.duoc.bitacora_escolar.model.Feriado
import retrofit2.http.GET
import retrofit2.http.Path

// API EXTERNA Nager.Date: https://date.nager.at/api/v3/PublicHolidays/2026/CL
// Solo se LEE (GET). No guarda nada en nuestra base de datos.
interface FeriadosApi {

    @GET("api/v3/PublicHolidays/{anio}/CL")
    suspend fun feriadosDeChile(@Path("anio") anio: Int): List<Feriado>
}
