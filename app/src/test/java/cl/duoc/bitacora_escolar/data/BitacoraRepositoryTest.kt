package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.fakes.Escenario
import cl.duoc.bitacora_escolar.model.Bitacora
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException

// Se prueba el repositorio con un microservicio FALSO (FakeBitacoraApi) y un Room FALSO (FakeCursoDao)
class BitacoraRepositoryTest {

    private val escenario = Escenario()
    private val repository = escenario.bitacoraRepository

    @Test
    fun docente_solo_ve_sus_bitacoras_con_nombres() = runTest {
        // Act
        val deAna = repository.obtenerDeDocente(Escenario.ANA)

        // Assert: la 3 es de Pedro, no aparece
        assertEquals(listOf(1, 2, 4), deAna.map { it.bitacora.id })
        assertEquals("7° Básico A", deAna[0].curso)
        assertEquals("Lenguaje", deAna[0].asignatura)
        assertEquals(Escenario.ANA, deAna[0].docenteId)
    }

    @Test
    fun estudiante_no_ve_borradores() = runTest {
        val visibles = repository.obtenerVisiblesDeCurso(Escenario.SEPTIMO_A)
        assertEquals(listOf(1, 2, 3), visibles.map { it.bitacora.id })

        // 8° Básico A (curso 4) no tiene bitácoras
        assertTrue(repository.obtenerVisiblesDeCurso(4).isEmpty())
    }

    @Test
    fun buscar_por_id() = runTest {
        val bitacora = repository.obtenerPorId(3)
        assertEquals("Matemática", bitacora?.asignatura)
        assertEquals(Escenario.PEDRO, bitacora?.docenteId)
        assertEquals(Escenario.SEPTIMO_A, bitacora?.cursoId)
    }

    @Test
    fun bitacora_de_una_asignacion_que_no_existe_en_room_devuelve_null() = runTest {
        escenario.bitacoraApi.bitacoras.add(Bitacora(9, 99, 10, 2026, "Otra", "p", "i", "q"))
        assertNull(repository.obtenerPorId(9))
    }

    @Test
    fun existe_bitacora_repetida() = runTest {
        assertTrue(repository.existe(1, 10, 2026))
        assertFalse(repository.existe(1, 12, 2026))
        // Al editar la bitácora 2 no se cuenta a sí misma
        assertFalse(repository.existe(1, 10, 2026, idExcluir = 2))
    }

    @Test
    fun guardar_nueva_hace_post() = runTest {
        // Arrange
        val nueva = Bitacora(asignacionId = 3, mes = 11, anio = 2026, titulo = "Historia - Noviembre",
            proposito = "p", instrucciones = "i", preguntas = "q", estado = EstadoBitacora.PUBLICADA)

        // Act
        repository.guardar(nueva)

        // Assert
        val guardada = escenario.bitacoraApi.bitacoras.last()
        assertEquals(5, guardada.id)
        assertEquals("Historia - Noviembre", guardada.titulo)
    }

    @Test
    fun guardar_existente_hace_put() = runTest {
        val editada = escenario.bitacoraApi.bitacoras[3].copy(titulo = "Teatro en noviembre")
        repository.guardar(editada)
        assertEquals("Teatro en noviembre", escenario.bitacoraApi.bitacoras[3].titulo)
        assertEquals(4, escenario.bitacoraApi.bitacoras.size)
    }

    @Test
    fun si_el_servidor_rechaza_se_lanza_HttpException() = runTest {
        escenario.bitacoraApi.rechazar = true
        var huboError = false
        try {
            repository.guardar(Bitacora(asignacionId = 1, mes = 10, anio = 2026, titulo = "Repetida", proposito = "p", instrucciones = "i", preguntas = "q"))
        } catch (e: HttpException) {
            huboError = true
            assertEquals(409, e.code())
        }
        assertTrue(huboError)
    }

    @Test
    fun publicar_cerrar_y_eliminar() = runTest {
        repository.publicar(4)
        assertEquals(EstadoBitacora.PUBLICADA, escenario.bitacoraApi.bitacoras[3].estado)

        repository.cerrar(2)
        assertEquals(EstadoBitacora.CERRADA, escenario.bitacoraApi.bitacoras[1].estado)

        repository.eliminar(4)
        assertEquals(3, escenario.bitacoraApi.bitacoras.size)
    }

    @Test
    fun sin_conexion_se_lanza_IOException() = runTest {
        escenario.apagarServidor()
        var huboError = false
        try {
            repository.obtenerDeDocente(Escenario.ANA)
        } catch (e: IOException) {
            huboError = true
        }
        assertTrue(huboError)
    }
}
