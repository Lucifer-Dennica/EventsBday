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

class EventsWidgetMini : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val nearest = WidgetDataProvider.loadNearest(context)
        val fontScale = WidgetSettings.miniFontScaleFlow(context).first()
        val showHeader = WidgetSettings.miniShowHeaderFlow(context).first()
        val opacity = WidgetSettings.opacityFlow(context).first()
        val bgMode = WidgetSettings.bgModeFlow(context).first()

        provideContent {
            MiniContent(
                event = nearest,
                fontScale = fontScale,
                showHeader = showHeader,
                opacity = opacity,
                bgMode = bgMode
            )
        }
    }
}

@Composable
private fun MiniContent(
    event: WidgetEvent?,
    fontScale: Int,
    showHeader: Boolean,
    opacity: Int,
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
    val nameSize = (14f * scale).sp
    val subSize = (11f * scale).sp
    val headerSize = (11f * scale).sp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(10.dp)
    ) {
        if (showHeader) {
            Text(
                text = "🎂 Ближайшее",
                style = TextStyle(
                    color = ColorProvider(subColor),
                    fontSize = headerSize,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(GlanceModifier.height(6.dp))
        }

        if (event == null) {
            Text(
                text = "Нет событий",
                style = TextStyle(color = ColorProvider(subColor), fontSize = subSize)
            )
        } else {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = event.name,
                        style = TextStyle(
                            color = ColorProvider(textColor),
                            fontSize = nameSize,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = if (event.daysLeft == 0L) "Сегодня" else "${event.daysLeft} дн. · ${event.dayMonth}",
                        style = TextStyle(color = ColorProvider(subColor), fontSize = subSize)
                    )
                }
            }
        }
    }
}

class EventsWidgetMiniReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EventsWidgetMini()
}
