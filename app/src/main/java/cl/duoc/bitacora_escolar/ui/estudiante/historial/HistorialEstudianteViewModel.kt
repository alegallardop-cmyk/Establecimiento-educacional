package cl.duoc.bitacora_escolar.ui.estudiante.historial

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.AppDatabase
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.model.Registro
import kotlinx.coroutines.flow.*

// Valores del filtro de seguimiento
object FiltroSeguimiento {
    const val TODOS = ""
    const val BORRADORES = "BORRADORES"
    const val PENDIENTES = "PENDIENTES"
    const val REVISADOS = "REVISADOS"
}

// Historial del estudiante: TODOS sus aportes y en qué estado van (seguimiento)
class HistorialEstudianteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val bitacoraRepository = BitacoraRepository(database.bitacoraDao())
    private val registroRepository = RegistroRepository(database.registroDao(), database.evidenciaDao())

    // Solo SUS registros
    val misRegistros: StateFlow<List<Registro>> =
        registroRepository
            .observarDeEstudiante(Sesion.idUsuario())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Bitácoras de su curso (para mostrar a qué bitácora pertenece cada aporte)
    val bitacoras: StateFlow<List<BitacoraConNombres>> =
        bitacoraRepository
            .observarVisiblesDeCurso(Sesion.usuario?.cursoId ?: 0)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _filtro = MutableStateFlow(FiltroSeguimiento.TODOS)
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    fun cambiarFiltro(valor: String) {
        _filtro.value = valor
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
