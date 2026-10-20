package cl.duoc.bitacora_escolar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Azul80,
    secondary = Amarillo80,
    tertiary = Verde80
)

private val LightColorScheme = lightColorScheme(
    primary = Azul40,
    secondary = Amarillo40,
    tertiary = Verde40,
    error = ColorError
)

@Composable
fun BitacoraEscolarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // No usamos colores dinámicos: queremos la misma identidad visual en todos los equipos
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
