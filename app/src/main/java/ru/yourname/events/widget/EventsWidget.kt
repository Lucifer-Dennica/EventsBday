package ru.yourname.events.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
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
        val bgMode = WidgetSettings.bgModeFlow(context).first()

        val openAppIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("events://list")
            setPackage(context.packageName)
        }

        provideContent {
            WidgetContent(
                events = events,
                fontScale = fontScale,
                opacity = opacity,
                photoSize = photoSize,
                showHeader = showHeader,
                showDate = showDate,
                bgMode = bgMode,
                openAppIntent = openAppIntent
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
    bgMode: WidgetBgMode,
    openAppIntent: Intent
) {
    val baseAlpha = opacity / 100f
    val (bgColor, textColor, subColor, accentColor, cardColor) = when (bgMode) {
        WidgetBgMode.LIGHT -> listOf(
            Color(0xFFF8F9FC).copy(alpha = baseAlpha),
            Color(0xFF1A1C22),
            Color(0xFF5B6478),
            Color(0xFF5B6BC0),
            Color(0xFFFFFFFF).copy(alpha = baseAlpha)
        )
        WidgetBgMode.DARK -> listOf(
            Color(0xFF121318).copy(alpha = baseAlpha),
            Color(0xFFE4E6EE),
            Color(0xFFB0B6C4),
            Color(0xFFB8C2FF),
            Color(0xFF1C1E24).copy(alpha = baseAlpha)
        )
        WidgetBgMode.TRANSPARENT -> listOf(
            Color.Transparent,
            Color(0xFF1A1C22),
            Color(0xFF5B6478),
            Color(0xFF5B6BC0),
            Color(0x40FFFFFF)
        )
    }

    val scale = fontScale / 100f
    val nameSize = (14f * scale).sp
    val subSize = (11f * scale).sp
    val daysSize = (16f * scale).sp
    val headerSize = (13f * scale).sp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity(openAppIntent))
            .padding(10.dp)
    ) {
        if (showHeader) {
            Text(
                text = "🎂 Ближайшие события",
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = headerSize,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(6.dp))
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
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(cardColor)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Фото или инициал
                    val photoRes = ev.presetAvatar?.let { presetAvatarDrawable(it) }
                    if (photoRes != null) {
                        Image(
                            provider = ImageProvider(photoRes),
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
                    Spacer(GlanceModifier.width(10.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = ev.name,
                            maxLines = 1,
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = nameSize,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        if (showDate) {
                            Text(
                                text = ev.dayMonth,
                                maxLines = 1,
                                style = TextStyle(
                                    color = ColorProvider(subColor),
                                    fontSize = subSize
                                )
                            )
                        }
                    }
                    Spacer(GlanceModifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = when (ev.daysLeft) {
                                0L -> "сегодня"
                                1L -> "1"
                                else -> ev.daysLeft.toString()
                            },
                            style = TextStyle(
                                color = ColorProvider(accentColor),
                                fontSize = daysSize,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (ev.daysLeft > 0L) {
                            Text(
                                text = "дн.",
                                style = TextStyle(
                                    color = ColorProvider(subColor),
                                    fontSize = (10f * scale).sp
                                )
                            )
                        }
                    }
                }
                if (idx < events.size - 1) {
                    Spacer(GlanceModifier.height(4.dp))
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
