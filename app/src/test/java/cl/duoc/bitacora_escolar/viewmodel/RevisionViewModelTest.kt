package cl.duoc.bitacora_escolar.viewmodel

import cl.duoc.bitacora_escolar.MainDispatcherRule
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.fakes.Escenario
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.ui.docente.revision.RevisionViewModel
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RevisionViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val escenario = Escenario()

    @Before
    fun preparar() {
        Sesion.usuario = escenario.usuario(Escenario.ANA)
    }

    private fun crearViewModel(registroId: Int): RevisionViewModel {
        Sesion.registroSeleccionado = registroId
        return RevisionViewModel(escenario.registroRepository, escenario.bitacoraRepository)
    }

    @Test
    fun carga_el_aporte_con_nombre_y_evidencias() {
        val viewModel = crearViewModel(2)
        val estado = viewModel.uiState.value
        assertEquals("Martina Soto", estado.estudiante)
        assertEquals(1, estado.evidencias.size)
        assertEquals("Excelente", estado.comentario)
    }

    @Test
    fun comentario_muy_corto_marca_error() {
        val viewModel = crearViewModel(3)
        viewModel.cambiarComentario("ok")
        viewModel.guardarComentario()
        assertTrue(viewModel.uiState.value.errorComentario)

        viewModel.marcarRevisado()
        assertTrue(viewModel.uiState.value.errorComentario)
    }

    @Test
    fun guardar_comentario_hace_put_y_sigue_pendiente() {
        // Arrange
        val viewModel = crearViewModel(3)
        viewModel.cambiarComentario("Muy buenas comparaciones")

        // Act
        viewModel.guardarComentario()

        // Assert
        val enServidor = escenario.registroApi.registros[2]
        assertEquals("Muy buenas comparaciones", enServidor.comentarioDocente)
        assertEquals(EstadoRevision.PENDIENTE, enServidor.estadoRevision)
        assertEquals("Retroalimentación guardada", viewModel.uiState.value.mensaje)
    }

    @Test
    fun marcar_revisado() {
        val viewModel = crearViewModel(3)
        viewModel.marcarRevisado()
        assertEquals(EstadoRevision.REVISADO, escenario.registroApi.registros[2].estadoRevision)
        assertEquals(EstadoRevision.REVISADO, viewModel.uiState.value.registro?.estadoRevision)
    }

    @Test
    fun un_borrador_o_un_registro_de_otro_docente_no_se_revisa() {
        assertTrue(crearViewModel(4).uiState.value.sinAcceso)     // borrador

        Sesion.usuario = escenario.usuario(Escenario.PEDRO)
        assertTrue(crearViewModel(3).uiState.value.sinAcceso)     // la bitácora es de Ana
    }

    @Test
    fun errores_del_servidor() {
        val viewModel = crearViewModel(3)
        escenario.registroApi.rechazar = true

        viewModel.cambiarComentario("Comentario válido")
        viewModel.guardarComentario()
        assertEquals("No se pudo guardar la retroalimentación.", viewModel.uiState.value.mensaje)

        viewModel.marcarRevisado()
        assertEquals("No se pudo marcar como revisado.", viewModel.uiState.value.mensaje)
        assertTrue(viewModel.uiState.value.mensajeEsError)
    }

    @Test
    fun sin_servidor_al_abrir() {
        escenario.apagarServidor()
        val viewModel = crearViewModel(3)
        assertEquals(ERROR_CONEXION, viewModel.uiState.value.mensaje)

        // Sin registro cargado, los botones no hacen nada
        viewModel.guardarComentario()
        viewModel.marcarRevisado()
        assertFalse(viewModel.uiState.value.errorComentario)
    }
}
