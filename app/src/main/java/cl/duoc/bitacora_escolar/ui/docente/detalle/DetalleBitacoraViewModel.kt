package cl.duoc.bitacora_escolar.ui.docente.detalle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.AppDatabase
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.RecordatorioRepository
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.data.UsuarioRepository
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.Recordatorio
import cl.duoc.bitacora_escolar.model.RegistroConEstudiante
import cl.duoc.bitacora_escolar.model.TipoRecordatorio
import cl.duoc.bitacora_escolar.model.Usuario
import cl.duoc.bitacora_escolar.util.Indicadores
import cl.duoc.bitacora_escolar.util.calcularIndicadores
import cl.duoc.bitacora_escolar.util.estudiantesSinParticipacion
import cl.duoc.bitacora_escolar.util.fechaDeHoy
import cl.duoc.bitacora_escolar.util.filtrarRegistros
import cl.duoc.bitacora_escolar.util.mesSiguiente
import cl.duoc.bitacora_escolar.util.nombreMes
import cl.duoc.bitacora_escolar.util.fechaComoNumero
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DetalleBitacoraViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val bitacoraRepository = BitacoraRepository(database.bitacoraDao())
    private val registroRepository = RegistroRepository(database.registroDao(), database.evidenciaDao())
    private val usuarioRepository = UsuarioRepository(database.usuarioDao())
    private val recordatorioRepository = RecordatorioRepository(database.recordatorioDao())

    // La bitácora que el docente tocó en la pantalla anterior
    private val bitacoraId = Sesion.bitacoraSeleccionada

    private val _uiState = MutableStateFlow(DetalleBitacoraUiState())
    val uiState: StateFlow<DetalleBitacoraUiState> = _uiState.asStateFlow()

    val bitacora: StateFlow<BitacoraConNombres?> =
        bitacoraRepository
            .observarPorId(bitacoraId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    // Solo los registros ENVIADOS
    val registros: StateFlow<List<RegistroConEstudiante>> =
        registroRepository
            .observarEnviadosDeBitacora(bitacoraId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        viewModelScope.launch {
            val datos = bitacoraRepository.obtenerPorId(bitacoraId)
            // Control de acceso: solo el docente responsable
            if (datos == null || datos.docenteId != Sesion.idUsuario()) {
                _uiState.update { it.copy(sinAcceso = true) }
            } else {
                val estudiantes = usuarioRepository.obtenerEstudiantesDeCurso(datos.cursoId)
                _uiState.update { it.copy(estudiantes = estudiantes) }
            }
        }
    }

    // ---------- Cálculos (los hace util/Indicadores.kt) ----------
    // .map { it.registro } saca el Registro de cada resultado del JOIN
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

    // ---------- Acciones ----------
    fun publicar() {
        viewModelScope.launch {
            try {
                bitacoraRepository.publicar(bitacoraId, fechaDeHoy())
                _uiState.update { it.copy(mensaje = "Bitácora publicada. Los estudiantes ya pueden verla.", mensajeEsError = false) }
            } catch (e: Exception) {
                e.printStackTrace()   // el detalle del error queda en el Logcat
                _uiState.update { it.copy(mensaje = "No se pudo publicar la bitácora.", mensajeEsError = true) }
            }
        }
    }

    fun cerrar() {
        val actual = bitacora.value ?: return
        viewModelScope.launch {
            try {
                bitacoraRepository.cerrar(bitacoraId, fechaDeHoy())

                // Recordatorio interno para preparar la bitácora del mes siguiente
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
            } catch (e: Exception) {
                e.printStackTrace()   // el detalle del error queda en el Logcat
                _uiState.update { it.copy(mensaje = "No se pudo cerrar la bitácora.", mensajeEsError = true) }
            }
        }
    }

    fun seleccionarRegistro(id: Int) {
        Sesion.registroSeleccionado = id
    }
}
