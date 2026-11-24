package cl.duoc.bitacora_escolar.viewmodel

import cl.duoc.bitacora_escolar.MainDispatcherRule
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.fakes.Escenario
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.ui.docente.inicio.DocenteInicioViewModel
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DocenteInicioViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val escenario = Escenario()
    private lateinit var viewModel: DocenteInicioViewModel

    @Before
    fun preparar() {
        Sesion.usuario = escenario.usuario(Escenario.ANA)
        viewModel = DocenteInicioViewModel(
            escenario.cursoRepository,
            escenario.bitacoraRepository,
            escenario.registroRepository,
            escenario.recordatorioRepository
        )
    }

    @Test
    fun cargar_trae_datos_de_room_y_del_microservicio() {
        // Act
        viewModel.cargar()

        // Assert
        assertEquals("Ana Fuentes", viewModel.nombreDocente)
        assertEquals(3, viewModel.uiState.value.asignaciones.size)
        assertEquals(listOf(1, 2, 4), viewModel.uiState.value.bitacoras.map { it.bitacora.id })
        assertEquals(1, viewModel.uiState.value.pendientesRevision)
        assertEquals(2, viewModel.uiState.value.recordatorios.size)
        assertEquals("", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.cargando)          // terminó de cargar
    }

    @Test
    fun antes_de_cargar_no_hay_datos() {
        assertTrue(viewModel.uiState.value.bitacoras.isEmpty())
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun contadores_del_dashboard() {
        viewModel.cargar()
        val lista = viewModel.uiState.value.bitacoras
        assertEquals(2, viewModel.bitacorasEnCurso(lista).size)      // sin la cerrada
        assertEquals(1, viewModel.contar(lista, EstadoBitacora.PUBLICADA))
        assertEquals(1, viewModel.contar(lista, EstadoBitacora.BORRADOR))
        assertEquals(1, viewModel.contar(lista, EstadoBitacora.CERRADA))
    }

    @Test
    fun completar_un_recordatorio() {
        viewModel.cargar()
        viewModel.completarRecordatorio(viewModel.uiState.value.recordatorios[0])
        assertEquals(1, viewModel.uiState.value.recordatorios.size)
    }

    @Test
    fun sin_servidor_se_muestra_el_error_pero_los_datos_locales_si_se_ven() {
        escenario.apagarServidor()
        viewModel.cargar()
        assertEquals(ERROR_CONEXION, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.cargando)
        assertEquals(3, viewModel.uiState.value.asignaciones.size)     // Room sí funciona
    }

    @Test
    fun seleccionar_nueva_y_cerrar_sesion() {
        viewModel.seleccionarBitacora(2)
        assertEquals(2, Sesion.bitacoraSeleccionada)
        viewModel.nuevaBitacora()
        assertEquals(0, Sesion.bitacoraSeleccionada)
        viewModel.cerrarSesion()
        assertNull(Sesion.usuario)
    }
}
