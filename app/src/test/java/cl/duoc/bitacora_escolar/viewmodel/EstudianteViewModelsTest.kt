package cl.duoc.bitacora_escolar.viewmodel

import cl.duoc.bitacora_escolar.MainDispatcherRule
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.fakes.Escenario
import cl.duoc.bitacora_escolar.ui.estudiante.bitacora.BitacoraEstudianteViewModel
import cl.duoc.bitacora_escolar.model.FiltroSeguimiento
import cl.duoc.bitacora_escolar.ui.estudiante.historial.HistorialEstudianteViewModel
import cl.duoc.bitacora_escolar.ui.estudiante.inicio.EstudianteInicioViewModel
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Pruebas de las 3 pantallas de lista del estudiante: inicio, bitácora e historial
class EstudianteViewModelsTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val escenario = Escenario()

    @Before
    fun preparar() {
        Sesion.usuario = escenario.usuario(Escenario.MARTINA)
    }

    // ---------- Inicio ----------
    private fun inicio() = EstudianteInicioViewModel(
        escenario.cursoRepository, escenario.bitacoraRepository, escenario.registroRepository
    )

    @Test
    fun inicio_carga_curso_bitacoras_y_mis_registros() {
        val viewModel = inicio()
        viewModel.cargar()

        assertEquals("Martina Soto", viewModel.nombreEstudiante)
        assertEquals("7° Básico A", viewModel.uiState.value.nombreCurso)
        assertEquals(3, viewModel.uiState.value.asignaciones.size)
        assertEquals(3, viewModel.uiState.value.bitacoras.size)         // sin el borrador
        assertEquals(3, viewModel.uiState.value.misRegistros.size)
    }

    @Test
    fun inicio_contadores() {
        val viewModel = inicio()
        viewModel.cargar()
        val registros = viewModel.uiState.value.misRegistros
        val bitacoras = viewModel.uiState.value.bitacoras

        assertEquals(2, viewModel.activas(bitacoras).size)
        assertEquals(1, viewModel.cerradas(bitacoras).size)
        assertEquals(1, viewModel.cantidadBorradores(registros))
        assertEquals(2, viewModel.cantidadEnviados(registros))
        assertEquals(2, viewModel.cantidadRevisados(registros))
        assertEquals(2, viewModel.cantidadComentarios(registros))
    }

    @Test
    fun inicio_sin_servidor_seleccionar_y_cerrar_sesion() {
        escenario.apagarServidor()
        val viewModel = inicio()
        viewModel.cargar()
        assertEquals(ERROR_CONEXION, viewModel.uiState.value.error)

        viewModel.seleccionarBitacora(2)
        assertEquals(2, Sesion.bitacoraSeleccionada)
        viewModel.cerrarSesion()
        assertNull(Sesion.usuario)
    }

    // ---------- Bitácora ----------
    private fun bitacora(id: Int): BitacoraEstudianteViewModel {
        Sesion.bitacoraSeleccionada = id
        return BitacoraEstudianteViewModel(escenario.bitacoraRepository, escenario.registroRepository)
    }

    @Test
    fun bitacora_muestra_solo_mis_registros_ordenados() {
        val viewModel = bitacora(2)
        viewModel.cargar()

        assertFalse(viewModel.uiState.value.sinAcceso)
        assertEquals(listOf(2, 4), viewModel.uiState.value.misRegistros.map { it.id })
        assertEquals("02/10/2026", viewModel.ordenados(viewModel.uiState.value.misRegistros)[0].fechaClase)
        assertTrue(viewModel.puedeRegistrar(viewModel.uiState.value.bitacora!!.bitacora))
    }

    @Test
    fun bitacora_cerrada_no_permite_registrar() {
        val viewModel = bitacora(1)
        viewModel.cargar()
        assertFalse(viewModel.puedeRegistrar(viewModel.uiState.value.bitacora!!.bitacora))
    }

    @Test
    fun bitacora_borrador_o_de_otro_curso_no_tiene_acceso() {
        val borrador = bitacora(4)
        borrador.cargar()
        assertTrue(borrador.uiState.value.sinAcceso)

        Sesion.usuario = escenario.usuario(Escenario.AGUSTIN)    // 8° Básico A
        val otroCurso = bitacora(2)
        otroCurso.cargar()
        assertTrue(otroCurso.uiState.value.sinAcceso)
    }

    @Test
    fun bitacora_sin_servidor_y_navegacion() {
        escenario.apagarServidor()
        val viewModel = bitacora(2)
        viewModel.cargar()
        assertEquals(ERROR_CONEXION, viewModel.uiState.value.error)

        viewModel.abrirRegistro(4)
        assertEquals(4, Sesion.registroSeleccionado)
        viewModel.nuevoRegistro()
        assertEquals(0, Sesion.registroSeleccionado)
    }

    // ---------- Historial ----------
    private fun historial() = HistorialEstudianteViewModel(escenario.bitacoraRepository, escenario.registroRepository)

    @Test
    fun historial_filtros_de_seguimiento() {
        val viewModel = historial()
        viewModel.cargar()
        val registros = viewModel.uiState.value.misRegistros

        assertEquals(3, viewModel.filtrados(registros, FiltroSeguimiento.TODOS).size)
        assertEquals(1, viewModel.filtrados(registros, FiltroSeguimiento.BORRADORES).size)
        assertEquals(0, viewModel.filtrados(registros, FiltroSeguimiento.PENDIENTES).size)
        assertEquals(2, viewModel.filtrados(registros, FiltroSeguimiento.REVISADOS).size)

        viewModel.cambiarFiltro(FiltroSeguimiento.REVISADOS)
        assertEquals(FiltroSeguimiento.REVISADOS, viewModel.uiState.value.filtro)
    }

    @Test
    fun historial_nombre_de_la_bitacora_y_abrir() {
        val viewModel = historial()
        viewModel.cargar()
        val registro = viewModel.uiState.value.misRegistros.first { it.id == 2 }

        assertEquals("Lenguaje: Bitácora de Lenguaje - Octubre", viewModel.nombreBitacora(registro, viewModel.uiState.value.bitacoras))
        assertEquals("Bitácora", viewModel.nombreBitacora(registro, emptyList()))

        viewModel.abrirRegistro(registro)
        assertEquals(2, Sesion.bitacoraSeleccionada)
        assertEquals(2, Sesion.registroSeleccionado)
    }

    @Test
    fun historial_sin_servidor() {
        escenario.apagarServidor()
        val viewModel = historial()
        viewModel.cargar()
        assertEquals(ERROR_CONEXION, viewModel.uiState.value.error)
    }
}
