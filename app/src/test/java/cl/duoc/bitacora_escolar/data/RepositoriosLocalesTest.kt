package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.fakes.Escenario
import cl.duoc.bitacora_escolar.model.EstadoRecordatorio
import cl.duoc.bitacora_escolar.model.Recordatorio
import cl.duoc.bitacora_escolar.model.TipoRecordatorio
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// Repositorios que siguen en Room: usuarios, cursos y recordatorios
class RepositoriosLocalesTest {

    private val escenario = Escenario()

    @Test
    fun usuarios_buscar_por_correo_sin_importar_mayusculas_ni_espacios() = runTest {
        val ana = escenario.usuarioRepository.buscarPorCorreo("  ANA.FUENTES@escuela.cl ")
        assertEquals("Ana Fuentes", ana?.nombre)
        assertNull(escenario.usuarioRepository.buscarPorCorreo("nadie@escuela.cl"))
    }

    @Test
    fun los_datos_de_prueba_se_cargan_una_sola_vez() = runTest {
        escenario.usuarioRepository.prepararDatos()      // ya había datos: no hace nada
        assertEquals(8, escenario.usuarioDao.usuarios.size)
        assertEquals(5, escenario.cursoDao.asignaciones.size)
    }

    @Test
    fun estudiantes_de_un_curso() = runTest {
        val estudiantes = escenario.usuarioRepository.obtenerEstudiantesDeCurso(Escenario.SEPTIMO_A)
        assertEquals(4, estudiantes.size)
    }

    @Test
    fun cursos_y_asignaciones() = runTest {
        assertEquals("7° Básico A", escenario.cursoRepository.nombreCurso(Escenario.SEPTIMO_A))
        assertEquals("", escenario.cursoRepository.nombreCurso(99))
        assertEquals(3, escenario.cursoRepository.asignacionesDeDocente(Escenario.ANA).size)
        assertEquals(
            listOf("Historia", "Lenguaje", "Matemática"),
            escenario.cursoRepository.asignacionesDeCurso(Escenario.SEPTIMO_A).map { it.asignatura }
        )
    }

    @Test
    fun recordatorios_guardar_listar_y_completar() = runTest {
        val repository = escenario.recordatorioRepository
        assertEquals(2, repository.pendientesDeDocente(Escenario.ANA).size)

        repository.guardar(Recordatorio(docenteId = Escenario.PEDRO, tipo = TipoRecordatorio.CIERRE_MENSUAL, descripcion = "Cerrar", fechaProgramada = "30/11/2026"))
        val dePedro = repository.pendientesDeDocente(Escenario.PEDRO)
        assertEquals(1, dePedro.size)

        repository.cambiarEstado(dePedro[0].id, EstadoRecordatorio.COMPLETADO)
        assertEquals(0, repository.pendientesDeDocente(Escenario.PEDRO).size)
    }
}
