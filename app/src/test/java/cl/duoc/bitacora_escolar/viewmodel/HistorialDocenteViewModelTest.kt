package cl.duoc.bitacora_escolar.viewmodel

import cl.duoc.bitacora_escolar.MainDispatcherRule
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.fakes.Escenario
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.ui.docente.historial.HistorialDocenteViewModel
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HistorialDocenteViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val escenario = Escenario()
    private lateinit var viewModel: HistorialDocenteViewModel

    @Before
    fun preparar() {
        Sesion.usuario = escenario.usuario(Escenario.ANA)
        viewModel = HistorialDocenteViewModel(escenario.bitacoraRepository)
    }

    @Test
    fun cargar_y_filtrar() {
        viewModel.cargar()
        val lista = viewModel.uiState.value.bitacoras
        assertEquals(3, lista.size)

        viewModel.cambiarFiltroEstado(EstadoBitacora.CERRADA)
        viewModel.cambiarFiltroTexto("")
        assertEquals(1, viewModel.filtradas(lista, viewModel.uiState.value.filtroEstado, viewModel.uiState.value.filtroTexto).size)

        assertEquals(3, viewModel.filtradas(lista, "", "lenguaje").size)      // por asignatura
        assertEquals(3, viewModel.filtradas(lista, "", "7°").size)            // por curso
        assertEquals(1, viewModel.filtradas(lista, "", "noviembre").size)     // por título
        assertEquals(0, viewModel.filtradas(lista, "", "química").size)
    }

    @Test
    fun error_de_conexion_y_seleccionar() {
        escenario.apagarServidor()
        viewModel.cargar()
        assertEquals(ERROR_CONEXION, viewModel.uiState.value.error)

        viewModel.seleccionarBitacora(1)
        assertEquals(1, Sesion.bitacoraSeleccionada)
    }
}
