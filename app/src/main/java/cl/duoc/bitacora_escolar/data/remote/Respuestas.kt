package cl.duoc.bitacora_escolar.data.remote

import retrofit2.HttpException
import retrofit2.Response

// Igual que en la guía de Retrofit con POST: si el servidor responde error (400, 404, 409...)
// se lanza una excepción y el ViewModel muestra el mensaje.
// La usan BitacoraRepository y RegistroRepository.
fun comprobarRespuesta(respuesta: Response<*>) {
    if (!respuesta.isSuccessful) {
        throw HttpException(respuesta)
    }
}
