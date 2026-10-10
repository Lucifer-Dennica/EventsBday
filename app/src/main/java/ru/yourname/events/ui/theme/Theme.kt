package ru.yourname.events.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.yourname.events.data.settings.AppTheme

// === СВЕТЛАЯ ===
private val LightColors = lightColorScheme(
    primary = Primary, onPrimary = OnPrimary, primaryContainer = PrimaryContainer,
    secondary = Secondary, tertiary = Tertiary,
    background = Background, surface = Surface, surfaceVariant = SurfaceVariant,
    onSurface = OnSurface, onSurfaceVariant = OnSurfaceVariant,
    outline = Outline, error = Error
)

// === ТЁМНАЯ ===
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

// === AMOLED ===
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

// === СИНЯЯ НОЧЬ ===
private val BlueNightColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF00325C),
    primaryContainer = Color(0xFF004A7F),
    background = Color(0xFF0A1929),
    surface = Color(0xFF102A43),
    surfaceVariant = Color(0xFF1A3A5C),
    onSurface = Color(0xFFE3F2FD),
    onSurfaceVariant = Color(0xFF90CAF9)
)

// === ТЁПЛЫЙ ЗАКАТ ===
private val WarmSunsetColors = lightColorScheme(
    primary = Color(0xFFD84315),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFCCBC),
    background = Color(0xFFFFF8F5),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFEBE3),
    onSurface = Color(0xFF2C1810),
    onSurfaceVariant = Color(0xFF7A4B3A),
    secondary = Color(0xFFEF6C00),
    tertiary = Color(0xFFBF360C)
)

// === ЛЕС ===
private val ForestColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    background = Color(0xFFF5FBF5),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8F5E9),
    onSurface = Color(0xFF1B2E1C),
    onSurfaceVariant = Color(0xFF4B6A4E),
    secondary = Color(0xFF558B2F),
    tertiary = Color(0xFF00695C)
)

// === ЯГОДНЫЙ МИЛКШЕЙК ===
private val BerryColors = lightColorScheme(
    primary = Color(0xFFAD1457),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF8BBD0),
    background = Color(0xFFFFF5F9),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFCE4EC),
    onSurface = Color(0xFF3A1524),
    onSurfaceVariant = Color(0xFF7A3A55),
    secondary = Color(0xFFC2185B),
    tertiary = Color(0xFF7B1FA2)
)

// === МИНИМАЛ ===
private val MinimalColors = lightColorScheme(
    primary = Color(0xFF212121),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEEEEE),
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurface = Color(0xFF212121),
    onSurfaceVariant = Color(0xFF757575),
    secondary = Color(0xFF424242),
    tertiary = Color(0xFF616161)
)

// === ВЫСОКИЙ КОНТРАСТ ===
private val HighContrastColors = lightColorScheme(
    primary = Color(0xFF0000CC),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCCCFF),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurface = Color(0xFF000000),
    onSurfaceVariant = Color(0xFF222222),
    secondary = Color(0xFFAA0000),
    tertiary = Color(0xFF006600)
)

@Composable
fun EventsTheme(
    theme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val scheme = when (theme) {
        AppTheme.LIGHT -> LightColors
        AppTheme.DARK -> DarkColors
        AppTheme.AMOLED -> AmoledColors
        AppTheme.BLUE_NIGHT -> BlueNightColors
        AppTheme.WARM_SUNSET -> WarmSunsetColors
        AppTheme.FOREST -> ForestColors
        AppTheme.BERRY -> BerryColors
        AppTheme.MINIMAL -> MinimalColors
        AppTheme.HIGH_CONTRAST -> HighContrastColors
        AppTheme.SYSTEM -> if (isSystemInDarkTheme()) DarkColors else LightColors
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = AppTypography,
        content = content
    )
}
