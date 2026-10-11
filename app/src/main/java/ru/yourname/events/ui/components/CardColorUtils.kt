package ru.yourname.events.ui.components

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

/** Разбирает строку вида "#RRGGBB" или "#RRGGBB,#RRGGBB" в список цветов. */
fun parseCustomColor(hex: String?): List<Color> {
    if (hex.isNullOrBlank()) return emptyList()
    return hex.split(",").mapNotNull { part ->
        val clean = part.trim().removePrefix("#")
        if (clean.length != 6) return@mapNotNull null
        try {
            val rgb = clean.toLong(16)
            Color(rgb or 0xFF000000)
        } catch (_: Exception) { null }
    }
}

/** Строит Brush для фона: сплошной цвет или вертикальный градиент. */
fun customColorBrush(hex: String?): Brush? {
    val colors = parseCustomColor(hex)
    return when (colors.size) {
        0 -> null
        1 -> SolidColor(colors[0])
        else -> Brush.verticalGradient(colors)
    }
}

/** Первый цвет из строки — для определения контраста текста. */
fun customColorPrimary(hex: String?): Color? = parseCustomColor(hex).firstOrNull()

/** Светлый ли цвет? Используется чтобы решить, какой цвет текста рисовать поверх. */
fun Color.isLight(): Boolean {
    val r = red
    val g = green
    val b = blue
    val luminance = 0.299 * r + 0.587 * g + 0.114 * b
    return luminance > 0.6
}
