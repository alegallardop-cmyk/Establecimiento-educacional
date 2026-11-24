package cl.duoc.bitacora_escolar.ui.estudiante.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.Dependencias
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.model.FiltroSeguimiento
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Historial del estudiante: TODOS sus aportes y en qué estado van (seguimiento)
class HistorialEstudianteViewModel(
    private val bitacoraRepository: BitacoraRepository = Dependencias.bitacoraRepository,
    private val registroRepository: RegistroRepository = Dependencias.registroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialEstudianteUiState())
    val uiState: StateFlow<HistorialEstudianteUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                val misRegistros = registroRepository.obtenerDeEstudiante(Sesion.idUsuario())
                val bitacoras = bitacoraRepository.obtenerVisiblesDeCurso(Sesion.usuario?.cursoId ?: 0)
                _uiState.update {
                    it.copy(misRegistros = misRegistros, bitacoras = bitacoras, cargando = false, error = "")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(cargando = false, error = ERROR_CONEXION) }
            }
        }
    }

    fun cambiarFiltro(valor: String) {
        _uiState.update { it.copy(filtro = valor) }
    }

    fun filtrados(registros: List<Registro>, filtro: String): List<Registro> {
        return registros.filter { registro ->
            when (filtro) {
                FiltroSeguimiento.BORRADORES -> registro.estadoEnvio == EstadoEnvio.BORRADOR
                FiltroSeguimiento.PENDIENTES -> registro.estadoEnvio == EstadoEnvio.ENVIADO &&
                        registro.estadoRevision == EstadoRevision.PENDIENTE
                FiltroSeguimiento.REVISADOS -> registro.estadoEnvio == EstadoEnvio.ENVIADO &&
                        registro.estadoRevision == EstadoRevision.REVISADO
                else -> true
            }
        }
    }

    // Busca el nombre de la bitácora del aporte en la lista de bitácoras
    fun nombreBitacora(registro: Registro, bitacoras: List<BitacoraConNombres>): String {
        val encontrada = bitacoras.find { it.bitacora.id == registro.bitacoraId }
        return if (encontrada == null) "Bitácora" else "${encontrada.asignatura}: ${encontrada.bitacora.titulo}"
    }

    // Para abrir el aporte se necesitan las dos cosas: su bitácora y su id
    fun abrirRegistro(registro: Registro) {
        Sesion.bitacoraSeleccionada = registro.bitacoraId
        Sesion.registroSeleccionado = registro.id
    }
}
