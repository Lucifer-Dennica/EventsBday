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
        presetRes != null && presetRes != 0 -> {
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
 * Маппинг номера preset-аватара на локальный ресурс.
 * Сейчас доступно 8 аватарок.
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
    else -> 0
}

/** Все доступные пресеты для UI-выбора. */
val AllPresetAvatars: List<Int> = (1..8).toList()
