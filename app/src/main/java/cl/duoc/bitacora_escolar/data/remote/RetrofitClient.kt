package cl.duoc.bitacora_escolar.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Igual que en clases: un "object" con Retrofit, la URL base y Gson para convertir JSON.
// Tenemos dos URL base: nuestro microservicio y la API externa de feriados.
object RetrofitClient {

    // 10.0.2.2 = el computador visto desde el emulador (donde corre Spring Boot)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // API externa (pública, sin clave): feriados de Chile
    private const val URL_FERIADOS = "https://date.nager.at/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val bitacoraApi: BitacoraApi by lazy {
        retrofit.create(BitacoraApi::class.java)
    }

    val registroApi: RegistroApi by lazy {
        retrofit.create(RegistroApi::class.java)
    }

    val feriadosApi: FeriadosApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_FERIADOS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FeriadosApi::class.java)
    }
}
