package cl.duoc.bitacora_escolar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

// Los ViewModel usan viewModelScope, que corre en el hilo principal (Dispatchers.Main).
// En una prueba unitaria NO hay hilo principal de Android, así que esta regla
// lo reemplaza por un dispatcher de prueba que ejecuta todo al instante.
// Se usa en cada prueba de ViewModel con:  @get:Rule val reglaMain = MainDispatcherRule()
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
