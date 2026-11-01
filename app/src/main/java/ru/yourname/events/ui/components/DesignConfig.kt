package ru.yourname.events.ui.components

import androidx.compose.runtime.compositionLocalOf
import ru.yourname.events.data.settings.DaysFormat
import ru.yourname.events.data.settings.ListDensity
import ru.yourname.events.data.settings.TitleFontSize

/** Глобальные настройки дизайна — задаются в DesignSettingsScreen через LocalDesignConfig. */
data class DesignConfig(
    val cornerRadius: Int = 18,
    val density: ListDensity = ListDensity.NORMAL,
    val titleFontSize: TitleFontSize = TitleFontSize.MEDIUM,
    val daysFormat: DaysFormat = DaysFormat.SHORT,
    val showDayOfWeek: Boolean = false,
    val neonBorder: Boolean = false,
    val neonBorderFull: Boolean = false,
    val showZodiac: Boolean = true
)

val LocalDesignConfig = compositionLocalOf { DesignConfig() }
