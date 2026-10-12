package ru.yourname.events.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.yourname.events.R
import ru.yourname.events.data.model.Event
import ru.yourname.events.widget.WidgetBgMode
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private data class PreviewEvent(
    val name: String,
    val dayMonth: String,
    val daysLeft: Long,
    val presetAvatar: Int?
)

@Composable
fun BigWidgetPreview(
    events: List<Event>,
    fontScale: Int,
    opacity: Int,
    photoSize: Int,
    showHeader: Boolean,
    showDate: Boolean,
    bgMode: WidgetBgMode
) {
    val previewEvents = rememberPreview(events, 3)
    val alpha = opacity / 100f
    val (bgColor, textColor, subColor, accentColor, cardColor) = widgetColors(bgMode, alpha)

    // Внешний контейнер "рабочего стола"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(14.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (showHeader) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎂", fontSize = (14f * fontScale / 100f).sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Ближайшие события",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = (14f * fontScale / 100f).sp
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            if (previewEvents.isEmpty()) {
                Text(
                    text = "Пока нет событий",
                    color = subColor,
                    fontSize = (12f * fontScale / 100f).sp
                )
            } else {
                previewEvents.forEachIndexed { idx, ev ->
                    // Карточка события
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardColor)
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarPreview(ev.presetAvatar, ev.name, photoSize)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = ev.name,
                                color = textColor,
                                fontSize = (14f * fontScale / 100f).sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (showDate) {
                                Text(
                                    text = ev.dayMonth,
                                    color = subColor,
                                    fontSize = (12f * fontScale / 100f).sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = when (ev.daysLeft) {
                                    0L -> "сегодня"
                                    else -> ev.daysLeft.toString()
                                },
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = (18f * fontScale / 100f).sp
                            )
                            if (ev.daysLeft > 0L) {
                                Text(
                                    text = "дн.",
                                    color = subColor,
                                    fontSize = (10f * fontScale / 100f).sp
                                )
                            }
                        }
                    }
                    if (idx < previewEvents.size - 1) {
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MiniWidgetPreview(
    events: List<Event>,
    fontScale: Int,
    opacity: Int,
    showHeader: Boolean,
    daysOnly: Boolean,
    bgMode: WidgetBgMode
) {
    val previewEvents = rememberPreview(events, 4)
    val alpha = opacity / 100f
    val (bgColor, textColor, subColor, accentColor, cardColor) = widgetColors(bgMode, alpha)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(10.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            if (showHeader) {
                Text(
                    text = "🎂 События",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = (13f * fontScale / 100f).sp
                )
                Spacer(Modifier.height(6.dp))
            }

            if (previewEvents.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Нет событий",
                        color = subColor,
                        fontSize = (12f * fontScale / 100f).sp
                    )
                }
            } else {
                // 2 x 2 сетка плиток
                val rows = previewEvents.chunked(2)
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rows.forEach { rowEvents ->
                        Row(
                            Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowEvents.forEach { ev ->
                                MiniTile(
                                    ev = ev,
                                    cardColor = cardColor,
                                    textColor = textColor,
                                    subColor = subColor,
                                    accentColor = accentColor,
                                    fontScale = fontScale,
                                    daysOnly = daysOnly,
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
                            }
                            // Если в ряду один элемент — растягиваем его на всю ширину
                            if (rowEvents.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniTile(
    ev: PreviewEvent,
    cardColor: Color,
    textColor: Color,
    subColor: Color,
    accentColor: Color,
    fontScale: Int,
    daysOnly: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardColor)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarPreview(ev.presetAvatar, ev.name, 34)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = ev.name,
                color = textColor,
                fontSize = (12f * fontScale / 100f).sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = when {
                    ev.daysLeft == 0L -> "сегодня"
                    daysOnly -> "${ev.daysLeft} дн."
                    else -> "${ev.daysLeft} дн."
                },
                color = accentColor,
                fontSize = (14f * fontScale / 100f).sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AvatarPreview(preset: Int?, name: String, size: Int) {
    val resId = preset?.let { presetAvatarDrawable(it) }
    // Мягкое розовое свечение вокруг — как на мокапе
    Box(
        modifier = Modifier.size((size + 6).dp),
        contentAlignment = Alignment.Center
    ) {
        // Свечение
        Box(
            modifier = Modifier
                .size((size + 6).dp)
                .clip(CircleShape)
                .background(Color(0x33F48FB1))
        )
        // Сам аватар
        if (resId != null) {
            Image(
                painter = painterResource(resId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E5FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "?",
                    color = Color(0xFF394099),
                    fontWeight = FontWeight.Bold,
                    fontSize = (size * 0.42f).sp
                )
            }
        }
    }
}

@Composable
private fun rememberPreview(events: List<Event>, limit: Int): List<PreviewEvent> {
    val today = LocalDate.now()
    return events
        .sortedBy { it.daysUntil(today) }
        .take(limit)
        .map { e ->
            val next = e.nextOccurrence(today)
            val monthShort = next.month.getDisplayName(TextStyle.SHORT, Locale("ru"))
            PreviewEvent(
                name = e.name,
                dayMonth = "${next.dayOfMonth} $monthShort",
                daysLeft = e.daysUntil(today),
                presetAvatar = e.presetAvatar
            )
        }
}

private fun widgetColors(mode: WidgetBgMode, alpha: Float): List<Color> = when (mode) {
    WidgetBgMode.LIGHT -> listOf(
        Color(0xFFF8F9FC).copy(alpha = alpha),
        Color(0xFF1A1C22),
        Color(0xFF5B6478),
        Color(0xFF5B6BC0),
        Color.White.copy(alpha = alpha)
    )
    WidgetBgMode.DARK -> listOf(
        Color(0xFF121318).copy(alpha = alpha),
        Color(0xFFE4E6EE),
        Color(0xFFB0B6C4),
        Color(0xFFB8C2FF),
        Color(0xFF1C1E24).copy(alpha = alpha)
    )
    WidgetBgMode.TRANSPARENT -> listOf(
        Color.Transparent,
        Color(0xFF1A1C22),
        Color(0xFF5B6478),
        Color(0xFF5B6BC0),
        Color(0x40FFFFFF)
    )
}

private fun presetAvatarDrawable(index: Int): Int? = when (index) {
    1 -> R.drawable.ic_avatar_girl
    2 -> R.drawable.ic_avatar_young_woman
    3 -> R.drawable.ic_avatar_old_woman
    4 -> R.drawable.ic_avatar_boy
    5 -> R.drawable.ic_avatar_young_man
    6 -> R.drawable.ic_avatar_old_man
    7 -> R.drawable.ic_avatar_astronaut
    8 -> R.drawable.ic_avatar_cat
    else -> null
}
