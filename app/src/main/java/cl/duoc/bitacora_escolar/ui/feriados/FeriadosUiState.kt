package cl.duoc.bitacora_escolar.ui.feriados

import cl.duoc.bitacora_escolar.model.Feriado

// Igual que en la clase de Retrofit: un UiState con 3 casos posibles
sealed interface FeriadosUiState {
    data object Cargando : FeriadosUiState

    data class Exito(
        val feriados: List<Feriado>
    ) : FeriadosUiState

    data class Error(
        val mensaje: String
    ) : FeriadosUiState
}
