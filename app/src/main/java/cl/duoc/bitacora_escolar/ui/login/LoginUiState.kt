package cl.duoc.bitacora_escolar.ui.login

data class LoginUiState(
    // Variables
    val correo: String = "",
    val password: String = "",

    // Estados de error
    val errorCorreo: Boolean = false,
    val errorPassword: Boolean = false,
    val mensajeError: String = ""      // credenciales incorrectas o error de la base de datos
)
