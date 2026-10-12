package ru.yourname.events.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.flow.first
import ru.yourname.events.R

class EventsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val events = WidgetDataProvider.loadUpcoming(context, 3)
        val fontScale = WidgetSettings.fontScaleFlow(context).first()
        val opacity = WidgetSettings.opacityFlow(context).first()
        val photoSize = WidgetSettings.photoSizeFlow(context).first()
        val showHeader = WidgetSettings.showHeaderFlow(context).first()
        val showDate = WidgetSettings.showDateFlow(context).first()
        val showGroup = WidgetSettings.showGroupFlow(context).first()
        val showAge = WidgetSettings.showAgeFlow(context).first()
        val bgMode = WidgetSettings.bgModeFlow(context).first()

        provideContent {
            WidgetContent(
                events = events,
                fontScale = fontScale,
                opacity = opacity,
                photoSize = photoSize,
                showHeader = showHeader,
                showDate = showDate,
                showGroup = showGroup,
                showAge = showAge,
                bgMode = bgMode
            )
        }
    }
}

@Composable
private fun WidgetContent(
    events: List<WidgetEvent>,
    fontScale: Int,
    opacity: Int,
    photoSize: Int,
    showHeader: Boolean,
    showDate: Boolean,
    showGroup: Boolean,
    showAge: Boolean,
    bgMode: WidgetBgMode
) {
    val baseAlpha = opacity / 100f

    val (bgColor, textColor, subColor) = when (bgMode) {
        WidgetBgMode.SYSTEM -> Triple(
            Color(0xFFF8F9FC).copy(alpha = baseAlpha),
            Color(0xFF1A1C22),
            Color(0xFF5B6478)
        )
        WidgetBgMode.LIGHT -> Triple(
            Color.White.copy(alpha = baseAlpha),
            Color(0xFF1A1C22),
            Color(0xFF5B6478)
        )
        WidgetBgMode.DARK -> Triple(
            Color(0xFF121318).copy(alpha = baseAlpha),
            Color(0xFFE4E6EE),
            Color(0xFFB0B6C4)
        )
        WidgetBgMode.TRANSPARENT -> Triple(
            Color.Transparent,
            Color(0xFF1A1C22),
            Color(0xFF5B6478)
        )
    }

    val scale = fontScale / 100f
    val nameSize = (15f * scale).sp
    val subSize = (12f * scale).sp
    val headerSize = (13f * scale).sp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(12.dp)
    ) {
        if (showHeader) {
            Text(
                text = "🎂 Мои события",
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = headerSize,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(GlanceModifier.height(8.dp))
        }

        if (events.isEmpty()) {
            Text(
                text = "Пока нет событий",
                style = TextStyle(
                    color = ColorProvider(subColor),
                    fontSize = subSize
                )
            )
        } else {
            events.forEachIndexed { idx, ev ->
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Фото или инициал
                    val photoRes = ev.presetAvatar?.let { presetAvatarDrawable(it) }
                    if (photoRes != null) {
                        androidx.glance.Image(
                            provider = androidx.glance.ImageProvider(photoRes),
                            contentDescription = null,
                            modifier = GlanceModifier.size(photoSize.dp)
                        )
                    } else {
                        Box(
                            modifier = GlanceModifier
                                .size(photoSize.dp)
                                .background(Color(0xFFE0E5FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ev.name.firstOrNull()?.uppercase() ?: "?",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF394099)),
                                    fontSize = (photoSize * 0.45f).sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(GlanceModifier.width(12.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = ev.name,
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = nameSize,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        if (showDate) {
                            val line = buildString {
                                append(ev.dayMonth)
                                if (showAge && ev.age != null) {
                                    append(" • исполнится ")
                                    append(ev.age)
                                }
                                if (showGroup && ev.group.isNotBlank()) {
                                    append(" • ")
                                    append(ev.group)
                                }
                            }
                            Text(
                                text = line,
                                style = TextStyle(
                                    color = ColorProvider(subColor),
                                    fontSize = subSize
                                )
                            )
                        }
                    }
                    Spacer(GlanceModifier.width(8.dp))
                    Text(
                        text = if (ev.daysLeft == 0L) "сегодня" else "${ev.daysLeft} дн.",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF5B6BC0)),
                            fontSize = nameSize,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                if (idx < events.size - 1) {
                    Spacer(GlanceModifier.height(2.dp))
                }
            }
        }
    }
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

class EventsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EventsWidget()
}
