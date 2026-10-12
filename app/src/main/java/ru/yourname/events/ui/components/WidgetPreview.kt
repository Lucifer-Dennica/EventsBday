package ru.yourname.events.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * Живой предпросмотр виджета внутри приложения.
 * Отражает настройки и данные в реальном времени — без ожидания лаунчера.
 */

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        if (showHeader) {
            Text(
                text = "🎂 Ближайшие события",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = (13f * fontScale / 100f).sp
            )
            Spacer(Modifier.height(6.dp))
        }

        if (previewEvents.isEmpty()) {
            Text(
                text = "Пока нет событий",
                color = subColor,
                fontSize = (11f * fontScale / 100f).sp
            )
        } else {
            previewEvents.forEachIndexed { idx, ev ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(cardColor)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarPreview(ev.presetAvatar, ev.name, photoSize)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = ev.name,
                            color = textColor,
                            fontSize = (14f * fontScale / 100f).sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (showDate) {
                            Text(
                                text = ev.dayMonth,
                                color = subColor,
                                fontSize = (11f * fontScale / 100f).sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = when (ev.daysLeft) {
                                0L -> "сегодня"
                                1L -> "1"
                                else -> ev.daysLeft.toString()
                            },
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = (16f * fontScale / 100f).sp
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
                if (idx < previewEvents.size - 1) Spacer(Modifier.height(4.dp))
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(6.dp)
    ) {
        if (showHeader) {
            Text(
                text = "🎂 События",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = (12f * fontScale / 100f).sp
            )
            Spacer(Modifier.height(4.dp))
        }

        if (previewEvents.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Нет событий",
                    color = subColor,
                    fontSize = (11f * fontScale / 100f).sp
                )
            }
        } else {
            val row1 = previewEvents.take(2)
            val row2 = previewEvents.drop(2).take(2)

            Row(Modifier.fillMaxWidth().weight(1f)) {
                row1.forEachIndexed { i, ev ->
                    if (i > 0) Spacer(Modifier.width(4.dp))
                    MiniTile(ev, cardColor, textColor, accentColor, fontScale, daysOnly, Modifier.weight(1f))
                }
                if (row1.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth().weight(1f)) {
                row2.forEachIndexed { i, ev ->
                    if (i > 0) Spacer(Modifier.width(4.dp))
                    MiniTile(ev, cardColor, textColor, accentColor, fontScale, daysOnly, Modifier.weight(1f))
                }
                if (row2.size < 2) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniTile(
    ev: PreviewEvent,
    cardColor: Color,
    textColor: Color,
    accentColor: Color,
    fontScale: Int,
    daysOnly: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(cardColor)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AvatarPreview(ev.presetAvatar, ev.name, 28)
        Spacer(Modifier.height(3.dp))
        Text(
            text = ev.name,
            color = textColor,
            fontSize = (11f * fontScale / 100f).sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = when {
                ev.daysLeft == 0L -> "сегодня"
                daysOnly -> "${ev.daysLeft} дн."
                else -> "${ev.daysLeft} дн. · ${ev.dayMonth}"
            },
            color = accentColor,
            fontSize = (15f * fontScale / 100f).sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AvatarPreview(preset: Int?, name: String, size: Int) {
    val resId = preset?.let { presetAvatarDrawable(it) }
    if (resId != null) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size.dp)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(size.dp / 4))
                .background(Color(0xFFE0E5FF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                color = Color(0xFF394099),
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.5f).sp
            )
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

private data class WidgetColors(
    val bg: Color,
    val text: Color,
    val sub: Color,
    val accent: Color,
    val card: Color
)

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
