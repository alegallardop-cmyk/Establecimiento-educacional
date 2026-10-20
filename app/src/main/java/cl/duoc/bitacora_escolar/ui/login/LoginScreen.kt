package cl.duoc.bitacora_escolar.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.bitacora_escolar.ui.componentes.BarraSuperior
import cl.duoc.bitacora_escolar.ui.componentes.ContenidoPantalla
import cl.duoc.bitacora_escolar.ui.componentes.IconoError
import cl.duoc.bitacora_escolar.ui.componentes.Mensaje

@Composable
fun LoginScreen(
    onIngresoDocente: () -> Unit,
    onIngresoEstudiante: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    // variable para mantener la sincronización de los estados
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(titulo = "Bitácora Escolar") }
    ) { innerPadding ->
        ContenidoPantalla(innerPadding) {
            item {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.correo,
                    onValueChange = {
                        viewModel.cambiarCorreo(it)
                    },
                    label = {
                        Text("Correo")
                    },
                    isError = uiState.errorCorreo,
                    supportingText = {
                        if (uiState.errorCorreo) {
                            Text("Ingrese un correo válido terminado en .cl")
                        }
                    },
                    trailingIcon = { if (uiState.errorCorreo) IconoError() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = {
                        viewModel.cambiarPassword(it)
                    },
                    label = {
                        Text("Contraseña")
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = uiState.errorPassword,
                    supportingText = {
                        if (uiState.errorPassword) {
                            Text("Debe contener al menos 8 caracteres")
                        }
                    },
                    trailingIcon = { if (uiState.errorPassword) IconoError() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // El error aparece con un desvanecimiento suave
            item {
                AnimatedVisibility(
                    visible = uiState.mensajeError.isNotBlank(),
                    enter = fadeIn(animationSpec = tween(400)),
                    exit = fadeOut(animationSpec = tween(400))
                ) {
                    Mensaje(uiState.mensajeError, esError = true)
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.ingresar(
                            onDocente = { onIngresoDocente() },
                            onEstudiante = { onIngresoEstudiante() }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ingresar")
                }
            }

            // Ayuda para la demostración: cuentas ficticias
            item { Text("Cuentas de prueba (contraseña 12345678):") }
            item {
                TextButton(onClick = { viewModel.usarCuentaDePrueba("ana.fuentes@escuela.cl") }) {
                    Text("Docente: Ana Fuentes")
                }
            }
            item {
                TextButton(onClick = { viewModel.usarCuentaDePrueba("martina.soto@escuela.cl") }) {
                    Text("Estudiante: Martina Soto")
                }
            }
            item {
                TextButton(onClick = { viewModel.usarCuentaDePrueba("florencia.ruiz@escuela.cl") }) {
                    Text("Estudiante: Florencia Ruiz")
                }
            }
        }
    }
}
