package cl.duoc.bitacora_escolar.viewmodel

import cl.duoc.bitacora_escolar.MainDispatcherRule
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.fakes.Escenario
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.TipoEvidencia
import cl.duoc.bitacora_escolar.ui.estudiante.registro.RegistroViewModel
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import cl.duoc.bitacora_escolar.util.fechaDeHoy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegistroViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val escenario = Escenario()

    @Before
    fun preparar() {
        Sesion.usuario = escenario.usuario(Escenario.MARTINA)
    }

    // bitacoraId 2 = Lenguaje octubre 2026 (publicada). registroId 0 = nuevo
    private fun crearViewModel(bitacoraId: Int = 2, registroId: Int = 0): RegistroViewModel {
        Sesion.bitacoraSeleccionada = bitacoraId
        Sesion.registroSeleccionado = registroId
        return RegistroViewModel(escenario.bitacoraRepository, escenario.registroRepository)
    }

    private fun llenarRegistro(viewModel: RegistroViewModel) {
        viewModel.cambiarFecha("15/10/2026")
        viewModel.cambiarContenido("Poesía visual")
        viewModel.cambiarActividad("Hicimos un caligrama")
        viewModel.cambiarAprendizaje("La forma también comunica")
        viewModel.cambiarDudas("Ninguna")
    }

    @Test
    fun registro_nuevo_parte_con_la_fecha_de_hoy() {
        val viewModel = crearViewModel()
        assertEquals(fechaDeHoy(), viewModel.uiState.value.fechaClase)
        assertFalse(viewModel.uiState.value.soloLectura)
        assertFalse(viewModel.puedeEliminar())       // todavía no existe en el servidor
    }

    @Test
    fun errores_de_la_fecha() {
        val viewModel = crearViewModel()

        viewModel.cambiarFecha("15-10-2026")
        viewModel.guardarBorrador()
        assertEquals("Use el formato dd/mm/aaaa", viewModel.uiState.value.errorFecha)

        viewModel.cambiarFecha("01/01/2099")
        viewModel.guardarBorrador()
        assertEquals("La fecha no puede ser futura", viewModel.uiState.value.errorFecha)

        viewModel.cambiarFecha("15/09/2026")
        viewModel.guardarBorrador()
        assertEquals("La fecha debe ser de Octubre 2026", viewModel.uiState.value.errorFecha)
    }

    @Test
    fun enviar_incompleto_marca_errores() {
        val viewModel = crearViewModel()
        viewModel.cambiarFecha("15/10/2026")
        viewModel.enviar()

        val estado = viewModel.uiState.value
        assertTrue(estado.errorContenido)
        assertTrue(estado.errorActividad)
        assertTrue(estado.errorAprendizaje)
        assertTrue(estado.errorConfirmacion)
        assertEquals(4, escenario.registroApi.registros.size)     // no se envió nada
    }

    @Test
    fun enviar_completo_hace_post_y_queda_solo_lectura() {
        // Arrange
        val viewModel = crearViewModel()
        llenarRegistro(viewModel)
        viewModel.cambiarDescripcionEvidencia("Mi caligrama")
        viewModel.cambiarEnlace("https://drive.google.com/caligrama")
        viewModel.agregarEvidencia()
        viewModel.cambiarConfirmacion(true)

        // Act
        viewModel.enviar()

        // Assert
        val enServidor = escenario.registroApi.registros.last()
        assertEquals(EstadoEnvio.ENVIADO, enServidor.estadoEnvio)
        assertEquals(1, enServidor.evidencias.size)
        assertTrue(viewModel.uiState.value.soloLectura)
        assertEquals("Registro enviado al docente", viewModel.uiState.value.mensaje)
    }

    @Test
    fun guardar_borrador_y_luego_eliminarlo() {
        // Arrange
        val viewModel = crearViewModel()
        llenarRegistro(viewModel)
        viewModel.guardarBorrador()
        assertEquals(5, viewModel.uiState.value.registro?.id)
        assertTrue(viewModel.puedeEliminar())
        var volvio = false

        // Act
        viewModel.pedirEliminar()
        viewModel.eliminar(onEliminado = { volvio = true })

        // Assert
        assertTrue(volvio)
        assertEquals(4, escenario.registroApi.registros.size)
    }

    @Test
    fun abrir_un_borrador_existente_y_actualizarlo() {
        val viewModel = crearViewModel(registroId = 4)
        assertEquals("Escritura de un caligrama", viewModel.uiState.value.contenido)
        assertFalse(viewModel.uiState.value.soloLectura)

        viewModel.cambiarActividad("Dibujamos el caligrama")
        viewModel.guardarBorrador()
        assertEquals("Dibujamos el caligrama", escenario.registroApi.registros[3].actividad)
    }

    @Test
    fun cancelar_eliminar() {
        val viewModel = crearViewModel(registroId = 4)
        viewModel.pedirEliminar()
        assertTrue(viewModel.uiState.value.confirmarEliminar)
        viewModel.cancelarEliminar()
        assertFalse(viewModel.uiState.value.confirmarEliminar)
    }

    @Test
    fun un_registro_enviado_se_ve_pero_no_se_cambia() {
        val viewModel = crearViewModel(registroId = 2)
        assertTrue(viewModel.uiState.value.soloLectura)
        assertEquals(1, viewModel.uiState.value.evidencias.size)
        assertFalse(viewModel.puedeEliminar())

        viewModel.guardarBorrador()
        assertEquals("Excelente", escenario.registroApi.registros[1].comentarioDocente)   // no cambió
    }

    @Test
    fun bitacora_cerrada_es_solo_lectura() {
        assertTrue(crearViewModel(bitacoraId = 1).uiState.value.soloLectura)
    }

    @Test
    fun registro_de_otro_estudiante_no_tiene_acceso() {
        assertTrue(crearViewModel(registroId = 3).uiState.value.sinAcceso)   // es de Benjamín
    }

    @Test
    fun evidencias_enlace_invalido_fotos_maximo_y_quitar() {
        val viewModel = crearViewModel()

        viewModel.cambiarEnlace("no es un enlace")
        viewModel.agregarEvidencia()
        assertTrue(viewModel.uiState.value.errorEnlace)

        // 5 fotos = máximo
        for (i in 1..5) {
            viewModel.agregarFoto("content://fotos/$i")
        }
        assertEquals(TipoEvidencia.FOTO, viewModel.uiState.value.evidencias[0].tipo)

        viewModel.agregarFoto("content://fotos/6")
        assertTrue(viewModel.uiState.value.errorMaximoEvidencias)
        viewModel.cambiarEnlace("https://ejemplo.cl")
        viewModel.agregarEvidencia()
        assertEquals(5, viewModel.uiState.value.evidencias.size)

        viewModel.quitarEvidencia(viewModel.uiState.value.evidencias[0])
        assertEquals(4, viewModel.uiState.value.evidencias.size)
        assertFalse(viewModel.uiState.value.errorMaximoEvidencias)
    }

    @Test
    fun errores_del_servidor() {
        val viewModel = crearViewModel(registroId = 4)
        escenario.registroApi.rechazar = true
        viewModel.guardarBorrador()
        assertEquals("No se pudo guardar el registro. Intenta nuevamente.", viewModel.uiState.value.mensaje)

        escenario.apagarServidor()
        viewModel.eliminar(onEliminado = {})
        assertEquals("No se pudo eliminar el borrador.", viewModel.uiState.value.mensaje)
        assertTrue(viewModel.uiState.value.mensajeEsError)
    }

    @Test
    fun sin_servidor_al_abrir() {
        escenario.apagarServidor()
        val viewModel = crearViewModel()
        assertEquals(ERROR_CONEXION, viewModel.uiState.value.mensaje)
        assertFalse(viewModel.uiState.value.cargando)
    }
}
