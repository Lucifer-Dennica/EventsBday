package ru.yourname.events.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Primary, onPrimary = OnPrimary, primaryContainer = PrimaryContainer,
    secondary = Secondary, tertiary = Tertiary,
    background = Background, surface = Surface, surfaceVariant = SurfaceVariant,
    onSurface = OnSurface, onSurfaceVariant = OnSurfaceVariant,
    outline = Outline, error = Error
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB8C2FF),
    onPrimary = Color(0xFF1D2878),
    primaryContainer = Color(0xFF394099),
    background = Color(0xFF121318),
    surface = Color(0xFF1C1E24),
    surfaceVariant = Color(0xFF2A2D36),
    onSurface = Color(0xFFE4E6EE),
    onSurfaceVariant = Color(0xFFB0B6C4)
)

@Composable
fun EventsTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
