package cl.duoc.bitacora_escolar.viewmodel

import cl.duoc.bitacora_escolar.MainDispatcherRule
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.fakes.Escenario
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.model.TipoRecordatorio
import cl.duoc.bitacora_escolar.ui.docente.detalle.DetalleBitacoraViewModel
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DetalleBitacoraViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val escenario = Escenario()

    @Before
    fun preparar() {
        Sesion.usuario = escenario.usuario(Escenario.ANA)
    }

    private fun crearViewModel(bitacoraId: Int): DetalleBitacoraViewModel {
        Sesion.bitacoraSeleccionada = bitacoraId
        return DetalleBitacoraViewModel(
            escenario.bitacoraRepository,
            escenario.registroRepository,
            escenario.usuarioRepository,
            escenario.recordatorioRepository
        )
    }

    @Test
    fun cargar_bitacora_registros_e_indicadores() {
        // Arrange
        val viewModel = crearViewModel(2)

        // Act
        viewModel.cargar()

        // Assert
        assertEquals("Bitácora de Lenguaje - Octubre", viewModel.uiState.value.bitacora?.bitacora?.titulo)
        assertEquals(2, viewModel.uiState.value.registros.size)          // solo enviados
        assertEquals(4, viewModel.uiState.value.estudiantes.size)
        assertFalse(viewModel.uiState.value.cargando)

        val indicadores = viewModel.indicadores(viewModel.uiState.value.registros)
        assertEquals(2, indicadores.participaron)
        assertEquals(1, indicadores.pendientes)
        assertEquals(2, viewModel.sinParticipacion(viewModel.uiState.value.registros).size)
    }

    @Test
    fun filtros_de_aportes() {
        val viewModel = crearViewModel(2)
        viewModel.cargar()
        val registros = viewModel.uiState.value.registros

        viewModel.cambiarFiltroNombre("benjamín")
        assertEquals(1, viewModel.registrosFiltrados(registros).size)

        viewModel.cambiarFiltroNombre("")
        viewModel.cambiarFiltroFecha("02/10/2026")
        viewModel.cambiarFiltroEstado(EstadoRevision.REVISADO)
        assertEquals("Martina Soto", viewModel.registrosFiltrados(registros)[0].estudiante)
    }

    @Test
    fun otro_docente_no_tiene_acceso() {
        Sesion.usuario = escenario.usuario(Escenario.PEDRO)
        val viewModel = crearViewModel(2)
        viewModel.cargar()
        assertTrue(viewModel.uiState.value.sinAcceso)
    }

    @Test
    fun publicar_un_borrador() {
        val viewModel = crearViewModel(4)
        viewModel.cargar()
        viewModel.publicar()
        assertEquals(EstadoBitacora.PUBLICADA, viewModel.uiState.value.bitacora?.bitacora?.estado)
        assertFalse(viewModel.uiState.value.mensajeEsError)
    }

    @Test
    fun cerrar_agrega_un_recordatorio() {
        val viewModel = crearViewModel(2)
        viewModel.cargar()
        viewModel.cerrar()
        assertEquals(EstadoBitacora.CERRADA, viewModel.uiState.value.bitacora?.bitacora?.estado)
        assertEquals(TipoRecordatorio.NUEVA_BITACORA, escenario.recordatorioDao.recordatorios.last().tipo)
    }

    @Test
    fun eliminar_borrador_con_confirmacion() {
        // Arrange
        val viewModel = crearViewModel(4)
        viewModel.cargar()
        var volvio = false
        assertTrue(viewModel.puedeEliminar())

        // Act
        viewModel.pedirEliminar()
        assertTrue(viewModel.uiState.value.confirmarEliminar)
        viewModel.eliminar(onEliminada = { volvio = true })

        // Assert
        assertTrue(volvio)
        assertEquals(3, escenario.bitacoraApi.bitacoras.size)
    }

    @Test
    fun una_bitacora_publicada_no_se_puede_eliminar() {
        val viewModel = crearViewModel(2)
        viewModel.cargar()
        var volvio = false
        assertFalse(viewModel.puedeEliminar())
        viewModel.eliminar(onEliminada = { volvio = true })
        assertFalse(volvio)
        assertEquals(4, escenario.bitacoraApi.bitacoras.size)
    }

    @Test
    fun cancelar_eliminar() {
        val viewModel = crearViewModel(4)
        viewModel.pedirEliminar()
        viewModel.cancelarEliminar()
        assertFalse(viewModel.uiState.value.confirmarEliminar)
    }

    @Test
    fun sin_servidor_las_acciones_muestran_error() {
        val viewModel = crearViewModel(4)
        viewModel.cargar()
        escenario.apagarServidor()

        viewModel.cargar()
        assertEquals(ERROR_CONEXION, viewModel.uiState.value.error)

        viewModel.publicar()
        assertEquals("No se pudo publicar la bitácora.", viewModel.uiState.value.mensaje)

        viewModel.cerrar()
        assertEquals("No se pudo cerrar la bitácora.", viewModel.uiState.value.mensaje)

        viewModel.eliminar(onEliminada = {})
        assertEquals("No se pudo eliminar la bitácora.", viewModel.uiState.value.mensaje)
        assertTrue(viewModel.uiState.value.mensajeEsError)
    }

    @Test
    fun cerrar_sin_bitacora_cargada_no_hace_nada() {
        val viewModel = crearViewModel(2)
        viewModel.cerrar()
        assertEquals("", viewModel.uiState.value.mensaje)
    }

    @Test
    fun seleccionar_registro() {
        val viewModel = crearViewModel(2)
        viewModel.seleccionarRegistro(3)
        assertEquals(3, Sesion.registroSeleccionado)
    }
}
