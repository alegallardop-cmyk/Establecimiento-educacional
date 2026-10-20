package cl.duoc.bitacora_escolar.ui.docente.revision

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.AppDatabase
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.util.fechaDeHoy
import cl.duoc.bitacora_escolar.util.validarComentario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RevisionViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val registroRepository = RegistroRepository(database.registroDao(), database.evidenciaDao())
    private val bitacoraRepository = BitacoraRepository(database.bitacoraDao())

    private val _uiState = MutableStateFlow(RevisionUiState())
    val uiState: StateFlow<RevisionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val aporte = registroRepository.obtenerPorId(Sesion.registroSeleccionado)
            val registro = aporte?.registro
            val bitacora = if (registro != null) bitacoraRepository.obtenerPorId(registro.bitacoraId) else null

            // Solo el docente responsable revisa, y solo registros ENVIADOS
            if (registro == null || bitacora == null ||
                bitacora.docenteId != Sesion.idUsuario() ||
                registro.estadoEnvio != EstadoEnvio.ENVIADO
            ) {
                _uiState.update { it.copy(sinAcceso = true) }
            } else {
                _uiState.update {
                    it.copy(
                        registro = registro,
                        estudiante = aporte?.estudiante ?: "",
                        evidencias = registroRepository.obtenerEvidencias(registro.id),
                        comentario = registro.comentarioDocente
                    )
                }
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
                registroRepository.guardarRetroalimentacion(
                    id = registro.id,
                    comentario = estado.comentario.trim(),
                    fecha = fechaDeHoy(),
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
                    fecha = fechaDeHoy(),
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
