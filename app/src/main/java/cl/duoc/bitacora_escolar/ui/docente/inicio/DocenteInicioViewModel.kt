package cl.duoc.bitacora_escolar.ui.docente.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.CursoRepository
import cl.duoc.bitacora_escolar.data.Dependencias
import cl.duoc.bitacora_escolar.data.RecordatorioRepository
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.EstadoRecordatorio
import cl.duoc.bitacora_escolar.model.Recordatorio
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DocenteInicioViewModel(
    private val cursoRepository: CursoRepository = Dependencias.cursoRepository,
    private val bitacoraRepository: BitacoraRepository = Dependencias.bitacoraRepository,
    private val registroRepository: RegistroRepository = Dependencias.registroRepository,
    private val recordatorioRepository: RecordatorioRepository = Dependencias.recordatorioRepository
) : ViewModel() {

    private val docenteId = Sesion.idUsuario()
    val nombreDocente = Sesion.usuario?.nombre ?: ""

    private val _uiState = MutableStateFlow(DocenteInicioUiState())
    val uiState: StateFlow<DocenteInicioUiState> = _uiState.asStateFlow()

    // La pantalla lo llama cada vez que se muestra (ON_RESUME), así ve los cambios del servidor
    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                // Room
                val asignaciones = cursoRepository.asignacionesDeDocente(docenteId)
                val recordatorios = recordatorioRepository.pendientesDeDocente(docenteId)
                _uiState.update { it.copy(asignaciones = asignaciones, recordatorios = recordatorios) }

                // Microservicio
                val bitacoras = bitacoraRepository.obtenerDeDocente(docenteId)
                val pendientes = registroRepository.contarPendientes(bitacoras.map { it.bitacora.id })
                _uiState.update {
                    it.copy(bitacoras = bitacoras, pendientesRevision = pendientes, cargando = false, error = "")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(cargando = false, error = ERROR_CONEXION) }
            }
        }
    }

    // Bitácoras que aún no se cierran (borradores y activas)
    fun bitacorasEnCurso(lista: List<BitacoraConNombres>): List<BitacoraConNombres> {
        return lista.filter { it.bitacora.estado != EstadoBitacora.CERRADA }
    }

    // Para los indicadores del dashboard: cuántas bitácoras hay en un estado
    fun contar(lista: List<BitacoraConNombres>, estado: String): Int {
        return lista.count { it.bitacora.estado == estado }
    }

    // El docente marca un recordatorio como hecho y deja de aparecer en los avisos
    fun completarRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch {
            recordatorioRepository.cambiarEstado(recordatorio.id, EstadoRecordatorio.COMPLETADO)
            val pendientes = recordatorioRepository.pendientesDeDocente(docenteId)
            _uiState.update { it.copy(recordatorios = pendientes) }
        }
    }

    // Guardamos cuál tocó, para que la siguiente pantalla sepa qué mostrar
    fun seleccionarBitacora(id: Int) {
        Sesion.bitacoraSeleccionada = id
    }

    // 0 = el formulario crea una bitácora nueva
    fun nuevaBitacora() {
        Sesion.bitacoraSeleccionada = 0
    }

    fun cerrarSesion() {
        Sesion.cerrar()
    }
}
