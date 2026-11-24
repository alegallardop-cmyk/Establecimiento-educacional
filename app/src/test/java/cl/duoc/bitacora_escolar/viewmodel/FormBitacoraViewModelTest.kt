package cl.duoc.bitacora_escolar.viewmodel

import cl.duoc.bitacora_escolar.MainDispatcherRule
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.fakes.Escenario
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.TipoRecordatorio
import cl.duoc.bitacora_escolar.ui.docente.formbitacora.FormBitacoraViewModel
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FormBitacoraViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val escenario = Escenario()

    @Before
    fun preparar() {
        Sesion.usuario = escenario.usuario(Escenario.ANA)
    }

    private fun crearViewModel(bitacoraId: Int): FormBitacoraViewModel {
        Sesion.bitacoraSeleccionada = bitacoraId
        return FormBitacoraViewModel(
            escenario.bitacoraRepository,
            escenario.cursoRepository,
            escenario.recordatorioRepository
        )
    }

    // Llena el formulario con datos válidos (Historia 7°A, diciembre 2026)
    private fun llenarFormulario(viewModel: FormBitacoraViewModel) {
        val historia = viewModel.uiState.value.asignaciones.first { it.asignatura == "Historia" }
        viewModel.elegirAsignacion(historia)
        viewModel.cambiarMes("12")
        viewModel.cambiarAnio("2026")
        viewModel.cambiarTitulo("Historia - Diciembre")
        viewModel.cambiarProposito("Repasar la independencia de Chile")
        viewModel.cambiarInstrucciones("Escribe un registro por clase")
        viewModel.cambiarNuevaPregunta("¿Qué personaje investigaste?")
        viewModel.agregarPregunta()
    }

    @Test
    fun formulario_vacio_marca_todos_los_errores() {
        val viewModel = crearViewModel(0)
        viewModel.guardar(publicar = false)

        val estado = viewModel.uiState.value
        assertTrue(estado.errorAsignacion)
        assertTrue(estado.errorMes)
        assertTrue(estado.errorAnio)
        assertTrue(estado.errorTitulo)
        assertTrue(estado.errorProposito)
        assertTrue(estado.errorInstrucciones)
        assertTrue(estado.errorPreguntas)
        assertFalse(estado.guardada)
    }

    @Test
    fun crear_y_publicar_hace_post_y_agrega_recordatorio() {
        // Arrange
        val viewModel = crearViewModel(0)
        assertFalse(viewModel.esEdicion())
        llenarFormulario(viewModel)

        // Act
        viewModel.guardar(publicar = true)

        // Assert
        assertTrue(viewModel.uiState.value.guardada)
        assertEquals("Bitácora creada y publicada", viewModel.uiState.value.mensaje)
        val nueva = escenario.bitacoraApi.bitacoras.last()
        assertEquals("Historia - Diciembre", nueva.titulo)
        assertEquals(EstadoBitacora.PUBLICADA, nueva.estado)
        assertEquals(TipoRecordatorio.CIERRE_MENSUAL, escenario.recordatorioDao.recordatorios.last().tipo)
    }

    @Test
    fun guardar_como_borrador() {
        val viewModel = crearViewModel(0)
        llenarFormulario(viewModel)
        viewModel.guardar(publicar = false)
        assertEquals("Bitácora guardada como borrador", viewModel.uiState.value.mensaje)
        assertEquals(EstadoBitacora.BORRADOR, escenario.bitacoraApi.bitacoras.last().estado)
    }

    @Test
    fun no_se_puede_repetir_asignacion_y_mes() {
        val viewModel = crearViewModel(0)
        llenarFormulario(viewModel)
        val lenguaje7 = viewModel.uiState.value.asignaciones.first { it.asignacion.id == 1 }
        viewModel.elegirAsignacion(lenguaje7)
        viewModel.cambiarMes("10")           // ya existe Lenguaje 7°A octubre 2026

        viewModel.guardar(publicar = true)

        assertTrue(viewModel.uiState.value.errorRepetida)
        assertEquals(4, escenario.bitacoraApi.bitacoras.size)
    }

    @Test
    fun preguntas_agregar_invalida_y_quitar() {
        val viewModel = crearViewModel(0)
        viewModel.cambiarNuevaPregunta("¿?")
        viewModel.agregarPregunta()
        assertTrue(viewModel.uiState.value.errorNuevaPregunta)

        viewModel.cambiarNuevaPregunta("¿Qué aprendí hoy?")
        viewModel.agregarPregunta()
        assertEquals(1, viewModel.uiState.value.preguntas.size)

        viewModel.quitarPregunta("¿Qué aprendí hoy?")
        assertTrue(viewModel.uiState.value.preguntas.isEmpty())
    }

    @Test
    fun editar_carga_los_datos_y_hace_put() {
        // Arrange: la bitácora 4 es un borrador de Ana
        val viewModel = crearViewModel(4)
        assertFalse(viewModel.uiState.value.cargando)      // ya cargó desde el servidor
        assertTrue(viewModel.esEdicion())
        assertEquals("Bitácora de Lenguaje - Noviembre", viewModel.uiState.value.titulo)
        assertEquals("11", viewModel.uiState.value.mes)

        // Act
        viewModel.cambiarTitulo("Teatro - Noviembre")
        viewModel.guardar(publicar = false)

        // Assert
        assertEquals("Cambios guardados correctamente", viewModel.uiState.value.mensaje)
        assertEquals("Teatro - Noviembre", escenario.bitacoraApi.bitacoras[3].titulo)
        assertEquals(2, escenario.recordatorioDao.recordatorios.size)    // al editar no agrega recordatorio
    }

    @Test
    fun no_se_puede_editar_una_cerrada_ni_una_de_otro_docente() {
        assertTrue(crearViewModel(1).uiState.value.sinAcceso)     // cerrada
        assertTrue(crearViewModel(3).uiState.value.sinAcceso)     // es de Pedro
    }

    @Test
    fun si_el_servidor_rechaza_se_muestra_error() {
        val viewModel = crearViewModel(0)
        llenarFormulario(viewModel)
        escenario.bitacoraApi.rechazar = true

        viewModel.guardar(publicar = true)

        assertTrue(viewModel.uiState.value.mensajeEsError)
        assertFalse(viewModel.uiState.value.guardada)
    }

    @Test
    fun sin_servidor_al_abrir_para_editar() {
        escenario.apagarServidor()
        val viewModel = crearViewModel(4)
        assertEquals(ERROR_CONEXION, viewModel.uiState.value.mensaje)
        assertTrue(viewModel.uiState.value.mensajeEsError)
    }
}
