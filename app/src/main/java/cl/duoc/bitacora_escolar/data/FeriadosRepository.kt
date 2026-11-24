package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.data.remote.FeriadosApi
import cl.duoc.bitacora_escolar.data.remote.RetrofitClient
import cl.duoc.bitacora_escolar.model.Feriado

// API EXTERNA: feriados de Chile (Nager.Date).
// Solo se muestran en pantalla: NO se guardan ni se mezclan con nuestros datos.
class FeriadosRepository(
    private val api: FeriadosApi = RetrofitClient.feriadosApi
) {

    // La API entrega todos los feriados del año; dejamos solo los del mes pedido.
    // La fecha viene como "2026-09-18": la parte del medio es el mes.
    suspend fun feriadosDelMes(mes: Int, anio: Int): List<Feriado> {
        return api.feriadosDeChile(anio).filter { feriado ->
            val partes = feriado.date.split("-")
            partes.size == 3 && partes[1].toIntOrNull() == mes
        }
    }
}
