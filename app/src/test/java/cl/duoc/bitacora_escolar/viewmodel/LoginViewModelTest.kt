package cl.duoc.bitacora_escolar.viewmodel

import cl.duoc.bitacora_escolar.MainDispatcherRule
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.data.UsuarioRepository
import cl.duoc.bitacora_escolar.fakes.FakeCursoDao
import cl.duoc.bitacora_escolar.fakes.FakeRecordatorioDao
import cl.duoc.bitacora_escolar.fakes.FakeUsuarioDao
import cl.duoc.bitacora_escolar.ui.login.LoginViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    // Room falso VACÍO: el ViewModel debe cargar los datos de prueba la primera vez
    private val usuarioDao = FakeUsuarioDao()
    private lateinit var viewModel: LoginViewModel

    private var entroDocente = false
    private var entroEstudiante = false

    @Before
    fun preparar() {
        Sesion.cerrar()
        viewModel = LoginViewModel(UsuarioRepository(usuarioDao, FakeCursoDao(), FakeRecordatorioDao()))
    }

    private fun ingresar() {
        viewModel.ingresar(onDocente = { entroDocente = true }, onEstudiante = { entroEstudiante = true })
    }

    @Test
    fun formulario_vacio_marca_errores() {
        ingresar()
        assertTrue(viewModel.uiState.value.errorCorreo)
        assertTrue(viewModel.uiState.value.errorPassword)
        assertFalse(entroDocente)
    }

    @Test
    fun al_escribir_se_quita_el_error() {
        ingresar()
        viewModel.cambiarCorreo("ana.fuentes@escuela.cl")
        viewModel.cambiarPassword("12345678")
        assertFalse(viewModel.uiState.value.errorCorreo)
        assertFalse(viewModel.uiState.value.errorPassword)
    }

    @Test
    fun docente_entra_y_queda_en_la_sesion() {
        // Arrange
        viewModel.usarCuentaDePrueba("ana.fuentes@escuela.cl")

        // Act
        ingresar()

        // Assert
        assertTrue(entroDocente)
        assertEquals("Ana Fuentes", Sesion.usuario?.nombre)
        assertEquals("", viewModel.uiState.value.correo)        // formulario limpio
        assertEquals(8, usuarioDao.usuarios.size)              // se cargaron los datos de prueba
    }

    @Test
    fun estudiante_entra() {
        viewModel.usarCuentaDePrueba("martina.soto@escuela.cl")
        ingresar()
        assertTrue(entroEstudiante)
        assertTrue(Sesion.esEstudiante())
    }

    @Test
    fun password_incorrecta() {
        viewModel.cambiarCorreo("ana.fuentes@escuela.cl")
        viewModel.cambiarPassword("87654321")
        ingresar()
        assertEquals("Correo o contraseña incorrectos", viewModel.uiState.value.mensajeError)
        assertFalse(entroDocente)
    }

    @Test
    fun si_la_base_de_datos_falla_se_muestra_un_mensaje() {
        usuarioDao.fallar = true
        viewModel.usarCuentaDePrueba("ana.fuentes@escuela.cl")
        ingresar()
        assertEquals("No se pudo iniciar sesión. Intenta nuevamente.", viewModel.uiState.value.mensajeError)
    }
}
