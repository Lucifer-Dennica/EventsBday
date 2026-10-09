package ru.yourname.events.data.model

import androidx.compose.ui.graphics.Color

// 10 пастельных цветов для карточек
val CardColors = listOf(
    Color(0xFFFFFFFF),   // 0 — чисто белый
    Color(0xFFFCE4EC),   // 1 — розовый
    Color(0xFFE3F2FD),   // 2 — голубой
    Color(0xFFE8F5E9),   // 3 — мятный
    Color(0xFFFFF3E0),   // 4 — персиковый
    Color(0xFFF3E5F5),   // 5 — лавандовый
    Color(0xFFFFFDE7),   // 6 — лимонный
    Color(0xFFE0F7FA),   // 7 — аквамарин
    Color(0xFFFBE9E7),   // 8 — тёплый коралл
    Color(0xFFECEFF1)    // 9 — серый
)

fun cardColorAt(index: Int): Color = CardColors[index.coerceIn(0, CardColors.lastIndex)]
