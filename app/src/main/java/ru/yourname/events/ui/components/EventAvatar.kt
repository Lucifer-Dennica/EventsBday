package ru.yourname.events.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.yourname.events.R
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.settings.AvatarShape

/** Глобальные настройки отображения аватара — задаются в DesignSettingsScreen. */
data class AvatarConfig(
    val glow: Boolean = true,
    val size: Int = 54,
    val shape: AvatarShape = AvatarShape.CIRCLE
)

val LocalAvatarConfig = compositionLocalOf { AvatarConfig() }

@Composable
fun EventAvatar(
    event: Event,
    size: Dp? = null,
    accentColor: Color,
    glow: Boolean? = null
) {
    val config = LocalAvatarConfig.current
    val realSize = size ?: config.size.dp
    val realGlow = glow ?: config.glow

    val presetRes = event.presetAvatar?.let { presetAvatarRes(it) }
    val outerSize = if (realGlow) realSize + 14.dp else realSize

    val avatarShape: Shape = when (config.shape) {
        AvatarShape.CIRCLE -> CircleShape
        AvatarShape.SQUARE -> RoundedCornerShape(0.dp)
        AvatarShape.ROUNDED_SQUARE -> RoundedCornerShape(realSize * 0.28f)
    }

    Box(
        modifier = Modifier.size(outerSize),
        contentAlignment = Alignment.Center
    ) {
        if (realGlow) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = this.size.width / 2f
                val cy = this.size.height / 2f
                val outerR = this.size.width / 2f
                val innerR = realSize.toPx() / 2f

                drawCircle(
                    color = accentColor.copy(alpha = 0.08f),
                    radius = outerR,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = accentColor.copy(alpha = 0.16f),
                    radius = innerR + 6.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = accentColor.copy(alpha = 0.28f),
                    radius = innerR + 3.dp.toPx(),
                    center = Offset(cx, cy)
                )
                // Кольцо свечения работает только для круглой формы
                if (config.shape == AvatarShape.CIRCLE) {
                    drawCircle(
                        color = accentColor.copy(alpha = 0.85f),
                        radius = innerR + 1.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                } else {
                    // Для квадратной формы — рисуем скруглённый прямоугольник
                    val halfSide = innerR - 1.dp.toPx()
                    val cornerPx = if (config.shape == AvatarShape.ROUNDED_SQUARE) realSize.toPx() * 0.28f else 0f
                    drawRoundRect(
                        color = accentColor.copy(alpha = 0.85f),
                        topLeft = Offset(cx - halfSide, cy - halfSide),
                        size = androidx.compose.ui.geometry.Size(halfSide * 2, halfSide * 2),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerPx),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(realSize)
                .clip(avatarShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            when {
                !event.photoUri.isNullOrBlank() -> {
                    AsyncImage(
                        model = event.photoUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(avatarShape)
                    )
                }
                presetRes != null && presetRes != 0 -> {
                    Image(
                        painter = painterResource(presetRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(avatarShape)
                    )
                }
                else -> {
                    Text(
                        text = event.name.trim().take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = accentColor
                    )
                }
            }
        }
    }
}

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

val AllPresetAvatars: List<Int> = (1..8).toList()
