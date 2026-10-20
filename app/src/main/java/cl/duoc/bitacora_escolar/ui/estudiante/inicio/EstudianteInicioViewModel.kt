package cl.duoc.bitacora_escolar.ui.estudiante.inicio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.AppDatabase
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.CursoRepository
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.AsignacionConNombres
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.model.Registro
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EstudianteInicioViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val cursoRepository = CursoRepository(database.cursoDao())
    private val bitacoraRepository = BitacoraRepository(database.bitacoraDao())
    private val registroRepository = RegistroRepository(database.registroDao(), database.evidenciaDao())

    val nombreEstudiante = Sesion.usuario?.nombre ?: ""
    private val cursoId = Sesion.usuario?.cursoId ?: 0

    private val _nombreCurso = MutableStateFlow("")
    val nombreCurso: StateFlow<String> = _nombreCurso.asStateFlow()

    private val _asignaciones = MutableStateFlow<List<AsignacionConNombres>>(emptyList())
    val asignaciones: StateFlow<List<AsignacionConNombres>> = _asignaciones.asStateFlow()

    // Solo bitácoras publicadas o cerradas de SU curso
    val bitacoras: StateFlow<List<BitacoraConNombres>> =
        bitacoraRepository
            .observarVisiblesDeCurso(cursoId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Solo SUS registros (para los avisos y los indicadores)
    val misRegistros: StateFlow<List<Registro>> =
        registroRepository
            .observarDeEstudiante(Sesion.idUsuario())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        viewModelScope.launch {
            _nombreCurso.value = cursoRepository.nombreCurso(cursoId)
            _asignaciones.value = cursoRepository.asignacionesDeCurso(cursoId)
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
