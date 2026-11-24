package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.fakes.Escenario
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.model.Registro
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException

class RegistroRepositoryTest {

    private val escenario = Escenario()
    private val repository = escenario.registroRepository

    @Test
    fun buscar_por_id_trae_el_nombre_desde_room() = runTest {
        val aporte = repository.obtenerPorId(3)
        assertEquals("Benjamín Lagos", aporte.estudiante)
        assertEquals("Comparación y metáfora", aporte.registro.contenido)
    }

    @Test
    fun si_el_estudiante_no_esta_en_room_se_muestra_un_nombre_generico() = runTest {
        escenario.registroApi.registros.add(Registro(9, 2, 99, "02/10/2026", "c", "a", "ap", estadoEnvio = EstadoEnvio.ENVIADO))
        assertEquals("Estudiante", repository.obtenerPorId(9).estudiante)
    }

    @Test
    fun el_docente_solo_ve_los_enviados() = runTest {
        val enviados = repository.obtenerEnviadosDeBitacora(2)
        assertEquals(listOf(2, 3), enviados.map { it.registro.id })    // el borrador 4 no aparece
        assertEquals("Martina Soto", enviados[0].estudiante)
    }

    @Test
    fun registros_del_estudiante() = runTest {
        // En una bitácora: solo los de Martina (no el de Benjamín)
        val enBitacora = repository.obtenerDeEstudianteEnBitacora(2, Escenario.MARTINA)
        assertEquals(listOf(2, 4), enBitacora.map { it.id })

        // Todos los suyos, del más nuevo al más antiguo
        val todos = repository.obtenerDeEstudiante(Escenario.MARTINA)
        assertEquals(listOf(4, 2, 1), todos.map { it.id })
    }

    @Test
    fun cuenta_los_pendientes_de_revision() = runTest {
        assertEquals(1, repository.contarPendientes(listOf(1, 2, 4)))
        assertEquals(0, repository.contarPendientes(listOf(3)))
        assertEquals(0, repository.contarPendientes(emptyList()))
    }

    @Test
    fun guardar_nuevo_hace_post_y_devuelve_el_id() = runTest {
        // Arrange
        val nuevo = Registro(bitacoraId = 2, estudianteId = Escenario.BENJAMIN, fechaClase = "09/10/2026",
            contenido = "Haiku", actividad = "Escribimos haikus", aprendizaje = "Métrica 5-7-5",
            estadoEnvio = EstadoEnvio.ENVIADO)

        // Act
        val guardado = repository.guardar(nuevo)

        // Assert
        assertEquals(5, guardado.id)
        assertEquals(EstadoRevision.PENDIENTE, guardado.estadoRevision)
        assertEquals(5, escenario.registroApi.registros.size)
    }

    @Test
    fun guardar_existente_hace_put() = runTest {
        val borrador = escenario.registroApi.registros[3].copy(actividad = "Dibujamos un caligrama")
        val guardado = repository.guardar(borrador)
        assertEquals(4, guardado.id)
        assertEquals("Dibujamos un caligrama", escenario.registroApi.registros[3].actividad)
    }

    @Test
    fun retroalimentacion_del_docente() = runTest {
        repository.guardarRetroalimentacion(3, "Buen trabajo", Escenario.ANA, EstadoRevision.REVISADO)
        val registro = escenario.registroApi.registros[2]
        assertEquals("Buen trabajo", registro.comentarioDocente)
        assertEquals(EstadoRevision.REVISADO, registro.estadoRevision)
        assertEquals(Escenario.ANA, registro.docenteRevisorId)
    }

    @Test
    fun eliminar_borrador_si_y_enviado_no() = runTest {
        repository.eliminar(4)
        assertEquals(3, escenario.registroApi.registros.size)

        var huboError = false
        try {
            repository.eliminar(3)          // enviado: el servidor responde 409
        } catch (e: HttpException) {
            huboError = true
        }
        assertTrue(huboError)
    }
}
