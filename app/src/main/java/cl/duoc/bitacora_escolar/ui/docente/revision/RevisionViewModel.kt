package cl.duoc.bitacora_escolar.ui.docente.revision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.Dependencias
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import cl.duoc.bitacora_escolar.util.validarComentario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RevisionViewModel(
    private val registroRepository: RegistroRepository = Dependencias.registroRepository,
    private val bitacoraRepository: BitacoraRepository = Dependencias.bitacoraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevisionUiState())
    val uiState: StateFlow<RevisionUiState> = _uiState.asStateFlow()

    // Se carga una sola vez (init) para no borrar el comentario que se está escribiendo
    init {
        viewModelScope.launch {
            try {
                val aporte = registroRepository.obtenerPorId(Sesion.registroSeleccionado)
                val registro = aporte.registro
                val bitacora = bitacoraRepository.obtenerPorId(registro.bitacoraId)

                // Solo el docente responsable revisa, y solo registros ENVIADOS
                if (bitacora == null ||
                    bitacora.docenteId != Sesion.idUsuario() ||
                    registro.estadoEnvio != EstadoEnvio.ENVIADO
                ) {
                    _uiState.update { it.copy(sinAcceso = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            registro = registro,
                            estudiante = aporte.estudiante,
                            evidencias = registro.evidencias,     // vienen dentro del JSON del registro
                            comentario = registro.comentarioDocente
                        )
                    }
                }
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

    fun cambiarComentario(comentario: String) {
        _uiState.update { it.copy(comentario = comentario, errorComentario = false, mensaje = "") }
    }

    // Guarda el comentario (el aporte sigue pendiente)
    fun guardarComentario() {
        val estado = _uiState.value
        val registro = estado.registro ?: return
        if (!validarComentario(estado.comentario)) {
            _uiState.update { it.copy(errorComentario = true) }
            return
        }
        viewModelScope.launch {
            try {
                // PUT /registros/{id}/retroalimentacion
                registroRepository.guardarRetroalimentacion(
                    id = registro.id,
                    comentario = estado.comentario.trim(),
                    docenteId = Sesion.idUsuario(),
                    estadoRevision = registro.estadoRevision
                )
                _uiState.update {
                    it.copy(
                        registro = registro.copy(comentarioDocente = estado.comentario.trim()),
                        mensaje = "Retroalimentación guardada",
                        mensajeEsError = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()   // el detalle del error queda en el Logcat
                _uiState.update { it.copy(mensaje = "No se pudo guardar la retroalimentación.", mensajeEsError = true) }
            }
        }
    }

    // Marca el aporte como revisado (si escribió un comentario, también lo guarda)
    fun marcarRevisado() {
        val estado = _uiState.value
        val registro = estado.registro ?: return
        if (estado.comentario.isNotBlank() && !validarComentario(estado.comentario)) {
            _uiState.update { it.copy(errorComentario = true) }
            return
        }
        viewModelScope.launch {
            try {
                registroRepository.guardarRetroalimentacion(
                    id = registro.id,
                    comentario = estado.comentario.trim(),
                    docenteId = Sesion.idUsuario(),
                    estadoRevision = EstadoRevision.REVISADO
                )
                _uiState.update {
                    it.copy(
                        registro = registro.copy(
                            estadoRevision = EstadoRevision.REVISADO,
                            comentarioDocente = estado.comentario.trim()
                        ),
                        mensaje = "Aporte marcado como revisado",
                        mensajeEsError = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()   // el detalle del error queda en el Logcat
                _uiState.update { it.copy(mensaje = "No se pudo marcar como revisado.", mensajeEsError = true) }
            }
        }
    }
}
