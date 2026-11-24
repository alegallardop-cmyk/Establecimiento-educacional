package cl.duoc.bitacora_escolar.ui.feriados

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.FeriadosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// API EXTERNA (Nager.Date): feriados de Chile del mes de la bitácora.
// Es solo informativo: no se guarda nada en Room ni en el microservicio.
class FeriadosViewModel(
    private val repository: FeriadosRepository = FeriadosRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeriadosUiState>(FeriadosUiState.Cargando)
    val uiState: StateFlow<FeriadosUiState> = _uiState.asStateFlow()

    fun cargar(mes: Int, anio: Int) {
        viewModelScope.launch {
            _uiState.value = FeriadosUiState.Cargando
            try {
                val feriados = repository.feriadosDelMes(mes, anio)
                _uiState.value = FeriadosUiState.Exito(feriados)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = FeriadosUiState.Error("No se pudieron cargar los feriados. Revisa tu conexión a internet.")
            }
        }
    }
}
