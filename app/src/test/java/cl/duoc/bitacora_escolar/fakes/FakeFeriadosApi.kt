package cl.duoc.bitacora_escolar.fakes

import cl.duoc.bitacora_escolar.data.remote.FeriadosApi
import cl.duoc.bitacora_escolar.model.Feriado
import java.io.IOException

// "API externa falsa": algunos feriados de Chile 2026 con el mismo formato que Nager.Date
class FakeFeriadosApi : FeriadosApi {

    var sinConexion = false
    var anioPedido = 0          // para comprobar qué año se pidió

    override suspend fun feriadosDeChile(anio: Int): List<Feriado> {
        if (sinConexion) throw IOException("Sin internet")
        anioPedido = anio
        return listOf(
            Feriado("2026-01-01", "Año Nuevo"),
            Feriado("2026-09-18", "Día de la Independencia"),
            Feriado("2026-09-19", "Día de las Glorias del Ejército"),
            Feriado("2026-10-12", "Encuentro de Dos Mundos"),
            Feriado("2026-11-01", "Día de Todos los Santos"),
            Feriado("fecha-mala", "Dato con formato incorrecto")
        )
    }
}
