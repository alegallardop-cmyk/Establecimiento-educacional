package cl.duoc.bitacora_escolar.ui.estudiante.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.CursoRepository
import cl.duoc.bitacora_escolar.data.Dependencias
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EstudianteInicioViewModel(
    private val cursoRepository: CursoRepository = Dependencias.cursoRepository,
    private val bitacoraRepository: BitacoraRepository = Dependencias.bitacoraRepository,
    private val registroRepository: RegistroRepository = Dependencias.registroRepository
) : ViewModel() {

    val nombreEstudiante = Sesion.usuario?.nombre ?: ""
    private val cursoId = Sesion.usuario?.cursoId ?: 0

    private val _uiState = MutableStateFlow(EstudianteInicioUiState())
    val uiState: StateFlow<EstudianteInicioUiState> = _uiState.asStateFlow()

    // La pantalla lo llama cada vez que se muestra (ON_RESUME)
    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                // Room
                val nombreCurso = cursoRepository.nombreCurso(cursoId)
                val asignaciones = cursoRepository.asignacionesDeCurso(cursoId)
                _uiState.update { it.copy(nombreCurso = nombreCurso, asignaciones = asignaciones) }

                // Microservicio
                val bitacoras = bitacoraRepository.obtenerVisiblesDeCurso(cursoId)
                val misRegistros = registroRepository.obtenerDeEstudiante(Sesion.idUsuario())
                _uiState.update {
                    it.copy(bitacoras = bitacoras, misRegistros = misRegistros, cargando = false, error = "")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(cargando = false, error = ERROR_CONEXION) }
            }
        }
    }

    fun activas(lista: List<BitacoraConNombres>): List<BitacoraConNombres> {
        return lista.filter { it.bitacora.estado == EstadoBitacora.PUBLICADA }
    }

    fun cerradas(lista: List<BitacoraConNombres>): List<BitacoraConNombres> {
        return lista.filter { it.bitacora.estado == EstadoBitacora.CERRADA }
    }

    fun cantidadBorradores(registros: List<Registro>): Int {
        return registros.count { it.estadoEnvio == EstadoEnvio.BORRADOR }
    }

    fun cantidadEnviados(registros: List<Registro>): Int {
        return registros.count { it.estadoEnvio == EstadoEnvio.ENVIADO }
    }

    fun cantidadRevisados(registros: List<Registro>): Int {
        return registros.count { it.estadoRevision == EstadoRevision.REVISADO && it.estadoEnvio == EstadoEnvio.ENVIADO }
    }

    fun cantidadComentarios(registros: List<Registro>): Int {
        return registros.count { it.comentarioDocente.isNotBlank() }
    }

    fun seleccionarBitacora(id: Int) {
        Sesion.bitacoraSeleccionada = id
    }

    fun cerrarSesion() {
        Sesion.cerrar()
    }
}
