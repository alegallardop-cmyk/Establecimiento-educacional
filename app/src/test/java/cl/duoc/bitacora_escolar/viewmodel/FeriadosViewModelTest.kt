package cl.duoc.bitacora_escolar.viewmodel

import cl.duoc.bitacora_escolar.MainDispatcherRule
import cl.duoc.bitacora_escolar.data.FeriadosRepository
import cl.duoc.bitacora_escolar.fakes.FakeFeriadosApi
import cl.duoc.bitacora_escolar.ui.feriados.FeriadosUiState
import cl.duoc.bitacora_escolar.ui.feriados.FeriadosViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FeriadosViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val api = FakeFeriadosApi()
    private val viewModel = FeriadosViewModel(FeriadosRepository(api))

    @Test
    fun empieza_cargando() {
        assertTrue(viewModel.uiState.value is FeriadosUiState.Cargando)
    }

    @Test
    fun exito_con_los_feriados_del_mes() {
        viewModel.cargar(9, 2026)

        val estado = viewModel.uiState.value
        assertTrue(estado is FeriadosUiState.Exito)
        assertEquals(2, (estado as FeriadosUiState.Exito).feriados.size)
    }

    @Test
    fun error_sin_internet() {
        api.sinConexion = true
        viewModel.cargar(9, 2026)
        assertTrue(viewModel.uiState.value is FeriadosUiState.Error)
    }
}
