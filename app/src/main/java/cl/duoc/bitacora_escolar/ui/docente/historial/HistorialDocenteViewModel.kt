package cl.duoc.bitacora_escolar.ui.docente.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.Dependencias
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Historial del docente: TODAS sus bitácoras (también las cerradas), con filtros
class HistorialDocenteViewModel(
    private val bitacoraRepository: BitacoraRepository = Dependencias.bitacoraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialDocenteUiState())
    val uiState: StateFlow<HistorialDocenteUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                val bitacoras = bitacoraRepository.obtenerDeDocente(Sesion.idUsuario())
                _uiState.update { it.copy(bitacoras = bitacoras, cargando = false, error = "") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(cargando = false, error = ERROR_CONEXION) }
            }
        }
    }

    fun cambiarFiltroEstado(estado: String) {
        _uiState.update { it.copy(filtroEstado = estado) }
    }

    fun cambiarFiltroTexto(texto: String) {
        _uiState.update { it.copy(filtroTexto = texto) }
    }

    fun filtradas(lista: List<BitacoraConNombres>, estado: String, texto: String): List<BitacoraConNombres> {
        return lista.filter { item ->
            (estado.isBlank() || item.bitacora.estado == estado) &&
                    (texto.isBlank() ||
                            item.curso.contains(texto.trim(), ignoreCase = true) ||
                            item.asignatura.contains(texto.trim(), ignoreCase = true) ||
                            item.bitacora.titulo.contains(texto.trim(), ignoreCase = true))
        }
    }

    fun seleccionarBitacora(id: Int) {
        Sesion.bitacoraSeleccionada = id
    }
}
