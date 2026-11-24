package cl.duoc.bitacora_escolar.ui.docente.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.Dependencias
import cl.duoc.bitacora_escolar.data.RecordatorioRepository
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.data.UsuarioRepository
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.Recordatorio
import cl.duoc.bitacora_escolar.model.RegistroConEstudiante
import cl.duoc.bitacora_escolar.model.TipoRecordatorio
import cl.duoc.bitacora_escolar.model.Usuario
import cl.duoc.bitacora_escolar.util.ERROR_CONEXION
import cl.duoc.bitacora_escolar.util.Indicadores
import cl.duoc.bitacora_escolar.util.calcularIndicadores
import cl.duoc.bitacora_escolar.util.estudiantesSinParticipacion
import cl.duoc.bitacora_escolar.util.fechaComoNumero
import cl.duoc.bitacora_escolar.util.fechaDeHoy
import cl.duoc.bitacora_escolar.util.filtrarRegistros
import cl.duoc.bitacora_escolar.util.mesSiguiente
import cl.duoc.bitacora_escolar.util.nombreMes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetalleBitacoraViewModel(
    private val bitacoraRepository: BitacoraRepository = Dependencias.bitacoraRepository,
    private val registroRepository: RegistroRepository = Dependencias.registroRepository,
    private val usuarioRepository: UsuarioRepository = Dependencias.usuarioRepository,
    private val recordatorioRepository: RecordatorioRepository = Dependencias.recordatorioRepository
) : ViewModel() {

    // La bitácora que el docente tocó en la pantalla anterior
    private val bitacoraId = Sesion.bitacoraSeleccionada

    private val _uiState = MutableStateFlow(DetalleBitacoraUiState())
    val uiState: StateFlow<DetalleBitacoraUiState> = _uiState.asStateFlow()

    // La pantalla lo llama cada vez que se muestra (ON_RESUME)
    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                val datos = bitacoraRepository.obtenerPorId(bitacoraId)
                // Control de acceso: solo el docente responsable
                if (datos == null || datos.docenteId != Sesion.idUsuario()) {
                    _uiState.update { it.copy(sinAcceso = true, cargando = false) }
                    return@launch
                }
                val registros = registroRepository.obtenerEnviadosDeBitacora(bitacoraId)
                val estudiantes = usuarioRepository.obtenerEstudiantesDeCurso(datos.cursoId)
                _uiState.update {
                    it.copy(bitacora = datos, registros = registros, estudiantes = estudiantes, cargando = false, error = "")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(cargando = false, error = ERROR_CONEXION) }
            }
        }
    }

    // ---------- Cálculos (los hace util/Indicadores.kt) ----------
    // .map { it.registro } saca el Registro de cada RegistroConEstudiante
    fun indicadores(registros: List<RegistroConEstudiante>): Indicadores {
        return calcularIndicadores(_uiState.value.estudiantes, registros.map { it.registro })
    }

    fun sinParticipacion(registros: List<RegistroConEstudiante>): List<Usuario> {
        return estudiantesSinParticipacion(_uiState.value.estudiantes, registros.map { it.registro })
    }

    fun registrosFiltrados(registros: List<RegistroConEstudiante>): List<RegistroConEstudiante> {
        val estado = _uiState.value
        return filtrarRegistros(registros, estado.filtroNombre, estado.filtroFecha, estado.filtroEstado)
            .sortedBy { fechaComoNumero(it.registro.fechaClase) }
    }

    // ---------- Filtros ----------
    fun cambiarFiltroNombre(nombre: String) {
        _uiState.update { it.copy(filtroNombre = nombre) }
    }

    fun cambiarFiltroFecha(fecha: String) {
        _uiState.update { it.copy(filtroFecha = fecha) }
    }

    fun cambiarFiltroEstado(estado: String) {
        _uiState.update { it.copy(filtroEstado = estado) }
    }

    // ---------- Acciones (PUT y DELETE al microservicio) ----------
    fun publicar() {
        viewModelScope.launch {
            try {
                bitacoraRepository.publicar(bitacoraId)
                _uiState.update { it.copy(mensaje = "Bitácora publicada. Los estudiantes ya pueden verla.", mensajeEsError = false) }
                cargar()   // vuelve a pedir la bitácora para ver el estado nuevo
            } catch (e: Exception) {
                e.printStackTrace()   // el detalle del error queda en el Logcat
                _uiState.update { it.copy(mensaje = "No se pudo publicar la bitácora.", mensajeEsError = true) }
            }
        }
    }

    fun cerrar() {
        val actual = _uiState.value.bitacora ?: return
        viewModelScope.launch {
            try {
                bitacoraRepository.cerrar(bitacoraId)

                // Recordatorio interno (Room) para preparar la bitácora del mes siguiente
                val (mes, anio) = mesSiguiente(actual.bitacora.mes, actual.bitacora.anio)
                recordatorioRepository.guardar(
                    Recordatorio(
                        docenteId = Sesion.idUsuario(),
                        tipo = TipoRecordatorio.NUEVA_BITACORA,
                        descripcion = "Crear la bitácora de ${actual.asignatura} ${actual.curso} para ${nombreMes(mes)} $anio",
                        fechaProgramada = fechaDeHoy()
                    )
                )
                _uiState.update { it.copy(mensaje = "Bitácora cerrada. Se agregó un recordatorio para la siguiente.", mensajeEsError = false) }
                cargar()
            } catch (e: Exception) {
                e.printStackTrace()   // el detalle del error queda en el Logcat
                _uiState.update { it.copy(mensaje = "No se pudo cerrar la bitácora.", mensajeEsError = true) }
            }
        }
    }

    // Solo se puede eliminar una bitácora en BORRADOR (así no se pierden aportes de estudiantes)
    fun puedeEliminar(): Boolean {
        return _uiState.value.bitacora?.bitacora?.estado == EstadoBitacora.BORRADOR
    }

    // Primer toque: pide confirmar
    fun pedirEliminar() {
        _uiState.update { it.copy(confirmarEliminar = true, mensaje = "") }
    }

    fun cancelarEliminar() {
        _uiState.update { it.copy(confirmarEliminar = false) }
    }

    // DELETE /bitacoras/{id}. Si sale bien, la pantalla vuelve atrás (onEliminada).
    fun eliminar(onEliminada: () -> Unit) {
        if (!puedeEliminar()) return
        viewModelScope.launch {
            try {
                bitacoraRepository.eliminar(bitacoraId)
                _uiState.update { it.copy(confirmarEliminar = false) }
                onEliminada()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(confirmarEliminar = false, mensaje = "No se pudo eliminar la bitácora.", mensajeEsError = true)
                }
            }
        }
    }

    fun seleccionarRegistro(id: Int) {
        Sesion.registroSeleccionado = id
    }
}
