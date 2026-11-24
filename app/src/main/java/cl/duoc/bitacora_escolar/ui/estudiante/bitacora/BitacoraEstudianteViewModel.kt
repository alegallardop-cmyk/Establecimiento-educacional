package cl.duoc.bitacora_escolar.ui.estudiante.bitacora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.Dependencias
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.Bitacora
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import cl.duoc.bitacora_escolar.util.ordenarPorFecha
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BitacoraEstudianteViewModel(
    private val bitacoraRepository: BitacoraRepository = Dependencias.bitacoraRepository,
    private val registroRepository: RegistroRepository = Dependencias.registroRepository
) : ViewModel() {

    private val bitacoraId = Sesion.bitacoraSeleccionada

    private val _uiState = MutableStateFlow(BitacoraEstudianteUiState())
    val uiState: StateFlow<BitacoraEstudianteUiState> = _uiState.asStateFlow()

    // La pantalla lo llama cada vez que se muestra (ON_RESUME): al volver de un registro se ve actualizado
    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                val datos = bitacoraRepository.obtenerPorId(bitacoraId)
                // Solo bitácoras de SU curso que ya fueron publicadas
                if (datos == null || datos.cursoId != Sesion.usuario?.cursoId || datos.bitacora.estado == EstadoBitacora.BORRADOR) {
                    _uiState.update { it.copy(sinAcceso = true, cargando = false) }
                    return@launch
                }
                val misRegistros = registroRepository.obtenerDeEstudianteEnBitacora(bitacoraId, Sesion.idUsuario())
                _uiState.update {
                    it.copy(bitacora = datos, misRegistros = misRegistros, cargando = false, error = "")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(cargando = false, error = ERROR_CONEXION) }
            }
        }
    }

    // Solo se pueden agregar registros si la bitácora está activa
    fun puedeRegistrar(bitacora: Bitacora): Boolean {
        return bitacora.estado == EstadoBitacora.PUBLICADA
    }

    fun ordenados(registros: List<Registro>): List<Registro> {
        return ordenarPorFecha(registros)
    }

    fun nuevoRegistro() {
        Sesion.registroSeleccionado = 0        // 0 = nuevo
    }

    fun abrirRegistro(id: Int) {
        Sesion.registroSeleccionado = id
    }
}
