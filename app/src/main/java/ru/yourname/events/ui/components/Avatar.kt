package ru.yourname.events.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.yourname.events.data.model.Event

/**
 * Универсальный аватар:
 * 1) если event.photoUri не пуст → грузим фото
 * 2) если event.presetAvatar в 1..6 → грузим DiceBear PNG
 * 3) иначе → цветной круг с инициалом
 */
@Composable
fun EventAvatar(event: Event, size: Dp = 56.dp, accentColor: androidx.compose.ui.graphics.Color) {
    val url = when {
        !event.photoUri.isNullOrBlank() -> event.photoUri
        event.presetAvatar != null -> presetAvatarUrl(event.presetAvatar)
        else -> null
    }

    if (url != null) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = event.name.trim().take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = accentColor
            )
        }
    }
}

/** Возвращает URL сгенерированного аватара. 6 стилей по кругу. */
fun presetAvatarUrl(index: Int): String {
    val style = when (index) {
        1 -> "adventurer"
        2 -> "micah"
        3 -> "avataaars"
        4 -> "thumbs"
        5 -> "bottts"
        else -> "fun-emoji"
    }
    return "https://api.dicebear.com/7.x/$style/png?seed=$index&size=256&backgroundType=gradientLinear"
}
