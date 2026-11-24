package cl.duoc.bitacora_escolar.ui.estudiante.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.Dependencias
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.Evidencia
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.model.TipoEvidencia
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import cl.duoc.bitacora_escolar.util.fechaDeHoy
import cl.duoc.bitacora_escolar.util.fechaEnPeriodo
import cl.duoc.bitacora_escolar.util.fechaNoFutura
import cl.duoc.bitacora_escolar.util.nombreMes
import cl.duoc.bitacora_escolar.util.puedeAgregarEvidencia
import cl.duoc.bitacora_escolar.util.validarEnlace
import cl.duoc.bitacora_escolar.util.validarFecha
import cl.duoc.bitacora_escolar.util.validarTexto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistroViewModel(
    private val bitacoraRepository: BitacoraRepository = Dependencias.bitacoraRepository,
    private val registroRepository: RegistroRepository = Dependencias.registroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    // Se carga una sola vez (init): si se cargara en ON_RESUME, al volver de la cámara
    // se borraría lo que el estudiante estaba escribiendo
    init {
        viewModelScope.launch {
            try {
                cargarDatos()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(mensaje = ERROR_CONEXION, mensajeEsError = true) }
            } finally {
                _uiState.update { it.copy(cargando = false) }
            }
        }
    }

    private suspend fun cargarDatos() {
        val estudiante = Sesion.usuario
        val conNombres = bitacoraRepository.obtenerPorId(Sesion.bitacoraSeleccionada)
        val bitacora = conNombres?.bitacora
        val registroId = Sesion.registroSeleccionado
        val registro = if (registroId != 0) registroRepository.obtenerPorId(registroId).registro else null

        // Control de acceso: bitácora de su curso y registro propio
        val bitacoraValida = estudiante != null && conNombres != null && bitacora != null &&
                conNombres.cursoId == estudiante.cursoId &&
                bitacora.estado != EstadoBitacora.BORRADOR
        val registroValido = registroId == 0 ||
                (registro != null && registro.estudianteId == estudiante?.id)

        if (!bitacoraValida || !registroValido) {
            _uiState.update { it.copy(sinAcceso = true) }
        } else if (registro == null) {
            // Registro nuevo
            _uiState.update {
                it.copy(
                    bitacora = bitacora,
                    fechaClase = fechaDeHoy(),
                    soloLectura = bitacora?.estado != EstadoBitacora.PUBLICADA
                )
            }
        } else {
            // Registro existente: si ya se envió, solo se puede mirar
            _uiState.update {
                it.copy(
                    bitacora = bitacora,
                    registro = registro,
                    fechaClase = registro.fechaClase,
                    contenido = registro.contenido,
                    actividad = registro.actividad,
                    aprendizaje = registro.aprendizaje,
                    dudas = registro.dudas,
                    evidencias = registro.evidencias,     // vienen dentro del JSON del registro
                    soloLectura = registro.estadoEnvio == EstadoEnvio.ENVIADO ||
                            bitacora?.estado != EstadoBitacora.PUBLICADA
                )
            }
        }
    }

    // ---------- Cambios en los campos ----------
    fun cambiarFecha(fecha: String) {
        _uiState.update { it.copy(fechaClase = fecha, errorFecha = "", mensaje = "") }
    }

    fun cambiarContenido(texto: String) {
        _uiState.update { it.copy(contenido = texto, errorContenido = false, mensaje = "") }
    }

    fun cambiarActividad(texto: String) {
        _uiState.update { it.copy(actividad = texto, errorActividad = false, mensaje = "") }
    }

    fun cambiarAprendizaje(texto: String) {
        _uiState.update { it.copy(aprendizaje = texto, errorAprendizaje = false, mensaje = "") }
    }

    fun cambiarDudas(texto: String) {
        _uiState.update { it.copy(dudas = texto, mensaje = "") }
    }

    fun cambiarDescripcionEvidencia(texto: String) {
        _uiState.update { it.copy(descripcionEvidencia = texto) }
    }

    fun cambiarEnlace(texto: String) {
        _uiState.update { it.copy(enlace = texto, errorEnlace = false) }
    }

    fun cambiarConfirmacion(valor: Boolean) {
        _uiState.update { it.copy(confirmaEnvio = valor, errorConfirmacion = false) }
    }

    // ---------- Evidencias (enlaces) ----------
    fun agregarEvidencia() {
        val estado = _uiState.value
        if (!puedeAgregarEvidencia(estado.evidencias.size)) {
            _uiState.update { it.copy(errorMaximoEvidencias = true) }
            return
        }
        if (!validarEnlace(estado.enlace)) {
            _uiState.update { it.copy(errorEnlace = true) }
            return
        }
        val evidencia = Evidencia(
            tipo = TipoEvidencia.ENLACE,
            descripcion = estado.descripcionEvidencia.ifBlank { "Enlace" },
            enlace = estado.enlace.trim()
        )
        _uiState.update {
            it.copy(evidencias = it.evidencias + evidencia, descripcionEvidencia = "", enlace = "")
        }
    }

    // Foto elegida desde la galería (Photo Picker) o tomada con la cámara.
    // En los dos casos recibimos un URI, igual que en FotoMotion (la pantalla lo pasa como texto).
    fun agregarFoto(uri: String) {
        if (!puedeAgregarEvidencia(_uiState.value.evidencias.size)) {
            _uiState.update { it.copy(errorMaximoEvidencias = true) }
            return
        }
        val foto = Evidencia(
            tipo = TipoEvidencia.FOTO,
            descripcion = "Fotografía",
            enlace = uri
        )
        _uiState.update { it.copy(evidencias = it.evidencias + foto) }
    }

    fun quitarEvidencia(evidencia: Evidencia) {
        _uiState.update {
            it.copy(evidencias = it.evidencias - evidencia, errorMaximoEvidencias = false)
        }
    }

    // ---------- Guardar ----------
    private fun validarFechaDeClase(): Boolean {
        val estado = _uiState.value
        val bitacora = estado.bitacora ?: return false
        val error = if (!validarFecha(estado.fechaClase)) {
            "Use el formato dd/mm/aaaa"
        } else if (!fechaNoFutura(estado.fechaClase, fechaDeHoy())) {
            "La fecha no puede ser futura"
        } else if (!fechaEnPeriodo(estado.fechaClase, bitacora.mes, bitacora.anio)) {
            "La fecha debe ser de ${nombreMes(bitacora.mes)} ${bitacora.anio}"
        } else {
            ""
        }
        _uiState.update { it.copy(errorFecha = error) }
        return error == ""
    }

    // El borrador solo necesita una fecha válida
    fun guardarBorrador() {
        if (!validarFechaDeClase()) return
        guardar(enviar = false)
    }

    // Para enviar se valida todo
    fun enviar() {
        val estado = _uiState.value
        val fechaValida = validarFechaDeClase()
        val errorContenido = !validarTexto(estado.contenido)
        val errorActividad = !validarTexto(estado.actividad)
        val errorAprendizaje = !validarTexto(estado.aprendizaje)
        val errorConfirmacion = !estado.confirmaEnvio

        _uiState.update {
            it.copy(
                errorContenido = errorContenido,
                errorActividad = errorActividad,
                errorAprendizaje = errorAprendizaje,
                errorConfirmacion = errorConfirmacion
            )
        }

        val formularioValido = fechaValida && !errorContenido && !errorActividad &&
                !errorAprendizaje && !errorConfirmacion
        if (formularioValido) {
            guardar(enviar = true)
        }
    }

    private fun guardar(enviar: Boolean) {
        val estado = _uiState.value
        val bitacora = estado.bitacora ?: return
        if (estado.soloLectura) return

        val base = estado.registro ?: Registro(
            bitacoraId = bitacora.id,
            estudianteId = Sesion.idUsuario(),
            fechaClase = "",
            contenido = "",
            actividad = "",
            aprendizaje = "",
            fechaCreacion = fechaDeHoy()
        )
        val registro = base.copy(
            evidencias = estado.evidencias,       // las evidencias viajan dentro del registro
            fechaClase = estado.fechaClase.trim(),
            contenido = estado.contenido.trim(),
            actividad = estado.actividad.trim(),
            aprendizaje = estado.aprendizaje.trim(),
            dudas = estado.dudas.trim(),
            estadoEnvio = if (enviar) EstadoEnvio.ENVIADO else EstadoEnvio.BORRADOR,
            fechaEnvio = if (enviar) fechaDeHoy() else ""
        )

        viewModelScope.launch {
            try {
                // POST (registro nuevo) o PUT (borrador que ya existía)
                val guardado = registroRepository.guardar(registro)
                _uiState.update {
                    it.copy(
                        registro = guardado,
                        // al enviar, la pantalla pasa a "solo lectura"
                        soloLectura = enviar,
                        mensaje = if (enviar) "Registro enviado al docente" else "Borrador guardado. Solo tú puedes verlo.",
                        mensajeEsError = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()   // el detalle del error queda en el Logcat
                // Error de la operación: no se pierde lo escrito, se puede intentar de nuevo
                _uiState.update {
                    it.copy(mensaje = "No se pudo guardar el registro. Intenta nuevamente.", mensajeEsError = true)
                }
            }
        }
    }

    // ---------- Eliminar un borrador (DELETE /registros/{id}) ----------

    // Solo un borrador que ya está guardado en el servidor y que todavía se puede editar
    fun puedeEliminar(): Boolean {
        val estado = _uiState.value
        val registro = estado.registro ?: return false
        return registro.id != 0 && registro.estadoEnvio == EstadoEnvio.BORRADOR && !estado.soloLectura
    }

    fun pedirEliminar() {
        _uiState.update { it.copy(confirmarEliminar = true, mensaje = "") }
    }

    fun cancelarEliminar() {
        _uiState.update { it.copy(confirmarEliminar = false) }
    }

    // Si sale bien, la pantalla vuelve atrás (onEliminado)
    fun eliminar(onEliminado: () -> Unit) {
        if (!puedeEliminar()) return
        val registro = _uiState.value.registro ?: return
        viewModelScope.launch {
            try {
                registroRepository.eliminar(registro.id)
                _uiState.update { it.copy(confirmarEliminar = false) }
                onEliminado()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(confirmarEliminar = false, mensaje = "No se pudo eliminar el borrador.", mensajeEsError = true)
                }
            }
        }
    }
}
