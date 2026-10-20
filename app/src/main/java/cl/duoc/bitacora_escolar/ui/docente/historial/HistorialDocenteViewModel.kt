package cl.duoc.bitacora_escolar.ui.docente.historial

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.AppDatabase
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.BitacoraConNombres
import kotlinx.coroutines.flow.*

// Historial del docente: TODAS sus bitácoras (también las cerradas), con filtros
class HistorialDocenteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val bitacoraRepository = BitacoraRepository(database.bitacoraDao())

    val bitacoras: StateFlow<List<BitacoraConNombres>> =
        bitacoraRepository
            .observarDeDocente(Sesion.idUsuario())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Filtros: estado ("" = todos) y texto (curso, asignatura o título)
    private val _filtroEstado = MutableStateFlow("")
    val filtroEstado: StateFlow<String> = _filtroEstado.asStateFlow()

    private val _filtroTexto = MutableStateFlow("")
    val filtroTexto: StateFlow<String> = _filtroTexto.asStateFlow()

    fun cambiarFiltroEstado(estado: String) {
        _filtroEstado.value = estado
    }

    fun cambiarFiltroTexto(texto: String) {
        _filtroTexto.value = texto
    }

    fun filtradas(lista: List<BitacoraConNombres>, estado: String, texto: String): List<BitacoraConNombres> {
        return lista.filter { item ->
            (estado.isBlank() || item.bitacora.estado == estado) &&
                    (texto.isBlank() ||
                            item.curso.contains(texto.trim(), ignoreCase = true) ||
                            item.asignatura.contains(texto.trim(), ignoreCase = true) ||
                            item.bitacora.titulo.contains(texto.trim(), ignoreCase = true))
        }
    }

    fun seleccionarBitacora(id: Int) {
        Sesion.bitacoraSeleccionada = id
    }
}
