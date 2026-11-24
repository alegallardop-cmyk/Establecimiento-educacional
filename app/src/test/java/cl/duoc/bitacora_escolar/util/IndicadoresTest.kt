package cl.duoc.bitacora_escolar.util

import cl.duoc.bitacora_escolar.fakes.Escenario
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.model.RegistroConEstudiante
import org.junit.Assert.assertEquals
import org.junit.Test

class IndicadoresTest {

    private val escenario = Escenario()

    // Estudiantes de 7° Básico A: Martina, Benjamín, Florencia y Vicente
    private val estudiantes = escenario.usuarioDao.usuarios.filter { it.cursoId == Escenario.SEPTIMO_A }

    // Registros de la bitácora de octubre (id 2): 2 enviados (1 revisado, 1 pendiente) y 1 borrador
    private val registros = Escenario.registrosIniciales().filter { it.bitacoraId == 2 }

    @Test
    fun calcula_los_indicadores_solo_con_los_enviados() {
        // Act
        val resultado = calcularIndicadores(estudiantes, registros)

        // Assert
        assertEquals(4, resultado.totalEstudiantes)
        assertEquals(2, resultado.participaron)
        assertEquals(2, resultado.sinParticipacion)
        assertEquals(2, resultado.totalAportes)
        assertEquals(1, resultado.revisados)
        assertEquals(1, resultado.pendientes)
    }

    @Test
    fun estudiantes_sin_participacion() {
        val sinParticipar = estudiantesSinParticipacion(estudiantes, registros)
        assertEquals(listOf("Florencia Ruiz", "Vicente Mora"), sinParticipar.map { it.nombre })
    }

    @Test
    fun filtra_por_nombre_fecha_y_estado() {
        val aportes = listOf(
            RegistroConEstudiante(registros[0], "Martina Soto"),
            RegistroConEstudiante(registros[1], "Benjamín Lagos")
        )
        assertEquals(2, filtrarRegistros(aportes, "", "", "").size)
        assertEquals(1, filtrarRegistros(aportes, "benj", "", "").size)
        assertEquals(2, filtrarRegistros(aportes, "", "02/10/2026", "").size)
        assertEquals(0, filtrarRegistros(aportes, "", "03/10/2026", "").size)
        assertEquals("Martina Soto", filtrarRegistros(aportes, "", "", EstadoRevision.REVISADO)[0].estudiante)
    }

    @Test
    fun ordena_por_fecha_de_la_clase() {
        val ordenados = ordenarPorFecha(Escenario.registrosIniciales().reversed())
        assertEquals(listOf("10/09/2026", "02/10/2026", "02/10/2026", "05/10/2026"), ordenados.map { it.fechaClase })
    }
}
