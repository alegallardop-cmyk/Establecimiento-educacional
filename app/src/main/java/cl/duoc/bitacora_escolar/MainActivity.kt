package cl.duoc.bitacora_escolar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.bitacora_escolar.navigation.Navegacion
import cl.duoc.bitacora_escolar.ui.theme.BitacoraEscolarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BitacoraEscolarTheme {
                // Igual que en la clase de Navegación: el Main solo llama a Navegacion().
                // Cada pantalla tiene su propio Scaffold.
                Navegacion()
            }
        }
    }
}
