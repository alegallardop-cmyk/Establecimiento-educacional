package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.fakes.FakeFeriadosApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

// API externa: se prueba con una API FALSA (no se llama a internet)
class FeriadosRepositoryTest {

    private val api = FakeFeriadosApi()
    private val repository = FeriadosRepository(api)

    @Test
    fun deja_solo_los_feriados_del_mes() = runTest {
        // Act
        val septiembre = repository.feriadosDelMes(9, 2026)

        // Assert
        assertEquals(2026, api.anioPedido)
        assertEquals(listOf("2026-09-18", "2026-09-19"), septiembre.map { it.date })
        assertEquals(1, repository.feriadosDelMes(11, 2026).size)
        assertTrue(repository.feriadosDelMes(12, 2026).isEmpty())
    }

    @Test
    fun sin_internet_se_lanza_la_excepcion() = runTest {
        api.sinConexion = true
        var huboError = false
        try {
            repository.feriadosDelMes(9, 2026)
        } catch (e: IOException) {
            huboError = true
        }
        assertTrue(huboError)
    }
}
