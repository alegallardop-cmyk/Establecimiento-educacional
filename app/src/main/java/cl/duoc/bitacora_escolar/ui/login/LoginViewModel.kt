package cl.duoc.bitacora_escolar.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.AppDatabase
import cl.duoc.bitacora_escolar.data.DatosDePrueba
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.data.UsuarioRepository
import cl.duoc.bitacora_escolar.model.Perfil
import cl.duoc.bitacora_escolar.util.validarCorreo
import cl.duoc.bitacora_escolar.util.validarPassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = UsuarioRepository(database.usuarioDao())

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun cambiarCorreo(nuevoCorreo: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(correo = nuevoCorreo, errorCorreo = false, mensajeError = "")
        }
    }

    fun cambiarPassword(nuevoPassword: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(password = nuevoPassword, errorPassword = false, mensajeError = "")
        }
    }

    // Botón de ayuda: rellena con una cuenta ficticia
    fun usarCuentaDePrueba(correo: String) {
        _uiState.value = LoginUiState(correo = correo, password = "12345678")
    }

    // Acción del formulario.
    // Recibe dos funciones: qué hacer si entra un docente y qué hacer si entra un estudiante.
    fun ingresar(onDocente: () -> Unit, onEstudiante: () -> Unit) {
        val estado = _uiState.value
        val errorCorreo = !validarCorreo(estado.correo)
        val errorPassword = !validarPassword(estado.password)

        _uiState.update { estadoActual ->
            estadoActual.copy(errorCorreo = errorCorreo, errorPassword = errorPassword)
        }

        val formularioValido = !errorCorreo && !errorPassword
        if (!formularioValido) return

        viewModelScope.launch {
            // try / catch: si la base de datos falla, se muestra un mensaje en vez de cerrarse la app
            try {
                // La primera vez se cargan los datos ficticios
                DatosDePrueba.cargarSiEstaVacia(database)

                val usuario = repository.buscarPorCorreo(estado.correo)
                if (usuario == null || usuario.password != estado.password) {
                    _uiState.update { it.copy(mensajeError = "Correo o contraseña incorrectos") }
                } else {
                    Sesion.usuario = usuario
                    _uiState.value = LoginUiState()       // limpiamos el formulario
                    if (usuario.perfil == Perfil.DOCENTE) onDocente() else onEstudiante()
                }
            } catch (e: Exception) {
                e.printStackTrace()   // el detalle del error queda en el Logcat
                _uiState.update { it.copy(mensajeError = "No se pudo iniciar sesión. Intenta nuevamente.") }
            }
        }
    }
}
