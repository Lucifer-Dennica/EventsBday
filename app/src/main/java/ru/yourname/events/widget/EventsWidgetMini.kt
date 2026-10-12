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
import java.time.LocalDate

class EventsWidgetMini : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val calendar = WidgetDataProvider.loadCalendar(context)
        val fontScale = WidgetSettings.miniFontScaleFlow(context).first()
        val showHeader = WidgetSettings.miniShowHeaderFlow(context).first()
        val opacity = WidgetSettings.opacityFlow(context).first()
        val bgMode = WidgetSettings.bgModeFlow(context).first()

        val openAppIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("events://calendar")
            setPackage(context.packageName)
        }

        provideContent {
            MiniCalendarContent(
                calendar = calendar,
                fontScale = fontScale,
                showHeader = showHeader,
                opacity = opacity,
                bgMode = bgMode,
                openAppIntent = openAppIntent
            )
        }
    }
}

@Composable
private fun MiniCalendarContent(
    calendar: WidgetCalendarMonth,
    fontScale: Int,
    showHeader: Boolean,
    opacity: Int,
    bgMode: WidgetBgMode,
    openAppIntent: Intent
) {
    val baseAlpha = opacity / 100f
    val (bgColor, textColor, subColor, accentColor) = when (bgMode) {
        WidgetBgMode.SYSTEM -> listOf(
            Color(0xFFF8F9FC).copy(alpha = baseAlpha),
            Color(0xFF1A1C22),
            Color(0xFF5B6478),
            Color(0xFF5B6BC0)
        )
        WidgetBgMode.LIGHT -> listOf(
            Color.White.copy(alpha = baseAlpha),
            Color(0xFF1A1C22),
            Color(0xFF5B6478),
            Color(0xFF5B6BC0)
        )
        WidgetBgMode.DARK -> listOf(
            Color(0xFF121318).copy(alpha = baseAlpha),
            Color(0xFFE4E6EE),
            Color(0xFFB0B6C4),
            Color(0xFFB8C2FF)
        )
        WidgetBgMode.TRANSPARENT -> listOf(
            Color.Transparent,
            Color(0xFF1A1C22),
            Color(0xFF5B6478),
            Color(0xFF5B6BC0)
        )
    }

    val scale = fontScale / 100f
    val headerSize = (13f * scale).sp
    val weekdaySize = (9f * scale).sp
    val daySize = (10f * scale).sp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity(openAppIntent))
            .padding(8.dp)
    ) {
        if (showHeader) {
            Text(
                text = calendar.title,
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = headerSize,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(4.dp))
        }

        // Дни недели
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            calendar.weekdays.forEach { wd ->
                Box(
                    modifier = GlanceModifier.defaultWeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = wd,
                        style = TextStyle(
                            color = ColorProvider(subColor),
                            fontSize = weekdaySize,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(GlanceModifier.height(2.dp))

        // Сетка дней
        val rows = calendar.days.chunked(7)
        rows.forEach { week ->
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 1.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                week.forEach { day ->
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .padding(1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day == null) {
                            Text(text = "", style = TextStyle(fontSize = daySize))
                        } else {
                            val bg = when {
                                day.isToday -> accentColor
                                day.hasEvents -> accentColor.copy(alpha = 0.25f)
                                else -> Color.Transparent
                            }
                            val fg = if (day.isToday) Color.White else textColor

                            Box(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .background(bg)
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.day.toString(),
                                    style = TextStyle(
                                        color = ColorProvider(fg),
                                        fontSize = daySize,
                                        fontWeight = if (day.isToday || day.hasEvents)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class EventsWidgetMiniReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EventsWidgetMini()
}
