package cl.duoc.bitacora_escolar.ui.docente.inicio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.AppDatabase
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.CursoRepository
import cl.duoc.bitacora_escolar.data.RecordatorioRepository
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.AsignacionConNombres
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.EstadoRecordatorio
import cl.duoc.bitacora_escolar.model.Recordatorio
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DocenteInicioViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val cursoRepository = CursoRepository(database.cursoDao())
    private val bitacoraRepository = BitacoraRepository(database.bitacoraDao())
    private val registroRepository = RegistroRepository(database.registroDao(), database.evidenciaDao())
    private val recordatorioRepository = RecordatorioRepository(database.recordatorioDao())

    private val docenteId = Sesion.idUsuario()
    val nombreDocente = Sesion.usuario?.nombre ?: ""

    // Cursos y asignaturas del docente (se cargan una vez)
    private val _asignaciones = MutableStateFlow<List<AsignacionConNombres>>(emptyList())
    val asignaciones: StateFlow<List<AsignacionConNombres>> = _asignaciones.asStateFlow()

    // Listas que se actualizan solas cuando cambia la base de datos
    val bitacoras: StateFlow<List<BitacoraConNombres>> =
        bitacoraRepository
            .observarDeDocente(docenteId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val pendientesRevision: StateFlow<Int> =
        registroRepository
            .observarPendientesDeDocente(docenteId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

    val recordatorios: StateFlow<List<Recordatorio>> =
        recordatorioRepository
            .observarPendientesDeDocente(docenteId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        viewModelScope.launch {
            _asignaciones.value = cursoRepository.asignacionesDeDocente(docenteId)
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
