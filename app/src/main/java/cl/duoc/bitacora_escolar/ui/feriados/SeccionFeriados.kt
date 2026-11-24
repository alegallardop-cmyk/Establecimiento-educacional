package cl.duoc.bitacora_escolar.ui.feriados

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.bitacora_escolar.ui.componentes.Cargando
import cl.duoc.bitacora_escolar.ui.componentes.Titulo
import cl.duoc.bitacora_escolar.util.fechaIsoATexto
import cl.duoc.bitacora_escolar.util.nombreMes

// Tarjeta con los feriados del mes de la bitácora (API externa).
// Se usa en el detalle del docente y en la bitácora del estudiante:
// así saben qué días no hay clases.
@Composable
fun SeccionFeriados(
    mes: Int,
    anio: Int,
    viewModel: FeriadosViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    // Cada vez que la pantalla se muestra, se piden los feriados
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargar(mes, anio)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Titulo("Feriados de ${nombreMes(mes)} $anio")
            Text(
                "Fuente: API externa Nager.Date",
                style = MaterialTheme.typography.bodySmall
            )

            when (val actual = estado) {
                is FeriadosUiState.Cargando -> {
                    Cargando()
                }

                is FeriadosUiState.Error -> {
                    Text(actual.mensaje, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { viewModel.cargar(mes, anio) }) {
                        Text("Reintentar")
                    }
                }

                is FeriadosUiState.Exito -> {
                    if (actual.feriados.isEmpty()) {
                        Text("No hay feriados este mes.")
                    }
                    for (feriado in actual.feriados) {
                        Text("${fechaIsoATexto(feriado.date)}: ${feriado.localName}")
                    }
                }
            }
        }
    }
}
