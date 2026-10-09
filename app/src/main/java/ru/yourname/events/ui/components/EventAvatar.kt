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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.yourname.events.R
import ru.yourname.events.data.model.Event

/**
 * Универсальный аватар:
 * 1) если event.photoUri не пуст → грузим фото пользователя
 * 2) если event.presetAvatar в 1..12 → берём PNG из drawable
 * 3) иначе → цветной круг с инициалом
 */
@Composable
fun EventAvatar(
    event: Event,
    size: Dp = 56.dp,
    accentColor: androidx.compose.ui.graphics.Color
) {
    val presetRes = event.presetAvatar?.let { presetAvatarRes(it) }

    when {
        !event.photoUri.isNullOrBlank() -> {
            AsyncImage(
                model = event.photoUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape)
            )
        }
        presetRes != null -> {
            androidx.compose.foundation.Image(
                painter = painterResource(presetRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape)
            )
        }
        else -> {
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
}

/**
 * Маппинг номера preset-аватара (1..12) на локальный ресурс.
 * Возвращает 0, если ресурса нет — тогда показываем инициал.
 */
fun presetAvatarRes(index: Int): Int = when (index) {
    1 -> R.drawable.ic_avatar_girl
    2 -> R.drawable.ic_avatar_young_woman
    3 -> R.drawable.ic_avatar_old_woman
    4 -> R.drawable.ic_avatar_boy
    5 -> R.drawable.ic_avatar_young_man
    6 -> R.drawable.ic_avatar_old_man
    7 -> R.drawable.ic_avatar_astronaut
    8 -> R.drawable.ic_avatar_cat
    9 -> R.drawable.ic_avatar_orb
    10 -> R.drawable.ic_avatar_smiley
    11 -> R.drawable.ic_avatar_flower
    12 -> R.drawable.ic_avatar_penguin
    else -> 0
}

/** Все 12 пресетов для UI-выбора. */
val AllPresetAvatars: List<Int> = (1..12).toList()
