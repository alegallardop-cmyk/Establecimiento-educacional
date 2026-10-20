package cl.duoc.bitacora_escolar.ui.estudiante.bitacora

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.AppDatabase
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.Bitacora
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.util.ordenarPorFecha
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BitacoraEstudianteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val bitacoraRepository = BitacoraRepository(database.bitacoraDao())
    private val registroRepository = RegistroRepository(database.registroDao(), database.evidenciaDao())

    private val bitacoraId = Sesion.bitacoraSeleccionada

    private val _sinAcceso = MutableStateFlow(false)
    val sinAcceso: StateFlow<Boolean> = _sinAcceso.asStateFlow()

    val bitacora: StateFlow<BitacoraConNombres?> =
        bitacoraRepository
            .observarPorId(bitacoraId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    // Solo los registros de ESTE estudiante en esta bitácora
    val misRegistros: StateFlow<List<Registro>> =
        registroRepository
            .observarDeEstudianteEnBitacora(bitacoraId, Sesion.idUsuario())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        viewModelScope.launch {
            val datos = bitacoraRepository.obtenerPorId(bitacoraId)
            // Solo bitácoras de SU curso que ya fueron publicadas
            if (datos == null || datos.cursoId != Sesion.usuario?.cursoId || datos.bitacora.estado == EstadoBitacora.BORRADOR) {
                _sinAcceso.value = true
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
