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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
                val outerHalf = this.size.width / 2f
                val innerHalf = realSize.toPx() / 2f

                // Радиус скругления для формы (для круга — половина)
                val cornerPx = when (config.shape) {
                    AvatarShape.CIRCLE -> innerHalf
                    AvatarShape.SQUARE -> 0f
                    AvatarShape.ROUNDED_SQUARE -> realSize.toPx() * 0.28f
                }

                if (config.shape == AvatarShape.CIRCLE) {
                    // Круглая форма — круги
                    drawCircle(
                        color = accentColor.copy(alpha = 0.08f),
                        radius = outerHalf,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = accentColor.copy(alpha = 0.16f),
                        radius = innerHalf + 6.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = accentColor.copy(alpha = 0.28f),
                        radius = innerHalf + 3.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = accentColor.copy(alpha = 0.85f),
                        radius = innerHalf + 1.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                } else {
                    // Квадрат / Скруглённый квадрат — roundRect

                    // Слой 1 (самый дальний)
                    val outerCorner = if (config.shape == AvatarShape.ROUNDED_SQUARE) {
                        cornerPx + 4.dp.toPx()
                    } else 0f
                    drawRoundRect(
                        color = accentColor.copy(alpha = 0.08f),
                        topLeft = Offset(cx - outerHalf, cy - outerHalf),
                        size = Size(outerHalf * 2, outerHalf * 2),
                        cornerRadius = CornerRadius(outerCorner)
                    )

                    // Слой 2
                    val layer2Half = innerHalf + 6.dp.toPx()
                    val layer2Corner = if (config.shape == AvatarShape.ROUNDED_SQUARE) {
                        cornerPx + 3.dp.toPx()
                    } else 0f
                    drawRoundRect(
                        color = accentColor.copy(alpha = 0.16f),
                        topLeft = Offset(cx - layer2Half, cy - layer2Half),
                        size = Size(layer2Half * 2, layer2Half * 2),
                        cornerRadius = CornerRadius(layer2Corner)
                    )

                    // Слой 3
                    val layer3Half = innerHalf + 3.dp.toPx()
                    val layer3Corner = if (config.shape == AvatarShape.ROUNDED_SQUARE) {
                        cornerPx + 2.dp.toPx()
                    } else 0f
                    drawRoundRect(
                        color = accentColor.copy(alpha = 0.28f),
                        topLeft = Offset(cx - layer3Half, cy - layer3Half),
                        size = Size(layer3Half * 2, layer3Half * 2),
                        cornerRadius = CornerRadius(layer3Corner)
                    )

                    // Чёткое кольцо
                    val ringHalf = innerHalf + 1.dp.toPx()
                    drawRoundRect(
                        color = accentColor.copy(alpha = 0.85f),
                        topLeft = Offset(cx - ringHalf, cy - ringHalf),
                        size = Size(ringHalf * 2, ringHalf * 2),
                        cornerRadius = CornerRadius(cornerPx),
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
