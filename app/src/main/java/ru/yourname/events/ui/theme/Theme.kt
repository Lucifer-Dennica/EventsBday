package ru.yourname.events.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.yourname.events.data.settings.AppTheme

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

// Полностью чёрная для AMOLED-экранов — экономит батарею
private val AmoledColors = darkColorScheme(
    primary = Color(0xFFB8C2FF),
    onPrimary = Color(0xFF1D2878),
    primaryContainer = Color(0xFF2A2F73),
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    surfaceVariant = Color(0xFF141414),
    onSurface = Color(0xFFE4E6EE),
    onSurfaceVariant = Color(0xFF9DA3B0),
    outline = Color(0xFF2A2A2A)
)

@Composable
fun EventsTheme(
    theme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (theme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.AMOLED -> true
    }
    val scheme = when (theme) {
        AppTheme.LIGHT -> LightColors
        AppTheme.DARK -> DarkColors
        AppTheme.AMOLED -> AmoledColors
        AppTheme.SYSTEM -> if (dark) DarkColors else LightColors
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = AppTypography,
        content = content
    )
}
