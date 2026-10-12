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

class EventsWidgetMini : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val events = WidgetDataProvider.loadUpcoming(context, 4)
        val fontScale = WidgetSettings.miniFontScaleFlow(context).first()
        val showHeader = WidgetSettings.miniShowHeaderFlow(context).first()
        val daysOnly = WidgetSettings.miniDaysOnlyFlow(context).first()
        val opacity = WidgetSettings.opacityFlow(context).first()
        val bgMode = WidgetSettings.bgModeFlow(context).first()

        val openAppIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("events://list")
            setPackage(context.packageName)
        }

        provideContent {
            MiniGridContent(
                events = events,
                fontScale = fontScale,
                showHeader = showHeader,
                daysOnly = daysOnly,
                opacity = opacity,
                bgMode = bgMode,
                openAppIntent = openAppIntent
            )
        }
    }
}

@Composable
private fun MiniGridContent(
    events: List<WidgetEvent>,
    fontScale: Int,
    showHeader: Boolean,
    daysOnly: Boolean,
    opacity: Int,
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
    val nameSize = (11f * scale).sp
    val daysSize = (16f * scale).sp
    val headerSize = (12f * scale).sp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity(openAppIntent))
            .padding(6.dp)
    ) {
        if (showHeader) {
            Text(
                text = "🎂 События",
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = headerSize,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(4.dp))
        }

        if (events.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет событий",
                    style = TextStyle(
                        color = ColorProvider(subColor),
                        fontSize = nameSize
                    )
                )
            }
        } else {
            // 2x2 сетка
            val row1 = events.take(2)
            val row2 = events.drop(2).take(2)

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (row1.size > 0) TileCell(row1[0], cardColor, textColor, subColor, accentColor, nameSize, daysSize, daysOnly, GlanceModifier.defaultWeight())
                if (row1.size > 1) {
                    Spacer(GlanceModifier.width(4.dp))
                    TileCell(row1[1], cardColor, textColor, subColor, accentColor, nameSize, daysSize, daysOnly, GlanceModifier.defaultWeight())
                } else {
                    Spacer(GlanceModifier.defaultWeight())
                }
            }
            Spacer(GlanceModifier.height(4.dp))
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (row2.size > 0) TileCell(row2[0], cardColor, textColor, subColor, accentColor, nameSize, daysSize, daysOnly, GlanceModifier.defaultWeight())
                if (row2.size > 1) {
                    Spacer(GlanceModifier.width(4.dp))
                    TileCell(row2[1], cardColor, textColor, subColor, accentColor, nameSize, daysSize, daysOnly, GlanceModifier.defaultWeight())
                } else {
                    Spacer(GlanceModifier.defaultWeight())
                }
            }
        }
    }
}

@Composable
private fun TileCell(
    event: WidgetEvent,
    cardColor: Color,
    textColor: Color,
    subColor: Color,
    accentColor: Color,
    nameSize: androidx.compose.ui.unit.TextUnit,
    daysSize: androidx.compose.ui.unit.TextUnit,
    daysOnly: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .background(cardColor)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Фото или инициал
        val photoRes = event.presetAvatar?.let { presetAvatarDrawable(it) }
        if (photoRes != null) {
            Image(
                provider = ImageProvider(photoRes),
                contentDescription = null,
                modifier = GlanceModifier.size(32.dp)
            )
        } else {
            Box(
                modifier = GlanceModifier
                    .size(32.dp)
                    .background(Color(0xFFE0E5FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = event.name.firstOrNull()?.uppercase() ?: "?",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF394099)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = event.name,
            maxLines = 1,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = nameSize,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = when {
                event.daysLeft == 0L -> "сегодня"
                daysOnly -> "${event.daysLeft} дн."
                else -> "${event.daysLeft} дн. · ${event.dayMonth}"
            },
            maxLines = 1,
            style = TextStyle(
                color = ColorProvider(accentColor),
                fontSize = daysSize,
                fontWeight = FontWeight.Bold
            )
        )
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

class EventsWidgetMiniReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EventsWidgetMini()
}
