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
import androidx.glance.text.TextAlign
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
        val textColorPref = WidgetSettings.textColorFlow(context).first()

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
                textColorPref = textColorPref,
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
    textColorPref: WidgetTextColor,
    openAppIntent: Intent
) {
    val baseAlpha = opacity / 100f

    val bgColor: Color
    val autoTextColor: Color
    val accentColor: Color
    val cardColor: Color

    when (bgMode) {
        WidgetBgMode.LIGHT -> {
            bgColor = Color(0xFFF8F9FC).copy(alpha = baseAlpha)
            autoTextColor = Color(0xFF1A1C22)
            accentColor = Color(0xFF5B6BC0)
            cardColor = Color(0xFFFFFFFF).copy(alpha = baseAlpha)
        }
        WidgetBgMode.DARK -> {
            bgColor = Color(0xFF121318).copy(alpha = baseAlpha)
            autoTextColor = Color(0xFFE4E6EE)
            accentColor = Color(0xFFB8C2FF)
            cardColor = Color(0xFF1C1E24).copy(alpha = baseAlpha)
        }
        WidgetBgMode.TRANSPARENT -> {
            bgColor = Color.Transparent
            autoTextColor = Color(0xFF1A1C22)
            accentColor = Color(0xFF5B6BC0)
            cardColor = Color(0x40FFFFFF)
        }
    }

    val textColor = when (textColorPref) {
        WidgetTextColor.AUTO -> autoTextColor
        WidgetTextColor.WHITE -> Color.White
        WidgetTextColor.BLACK -> Color.Black
        WidgetTextColor.BLUE -> Color(0xFF5B6BC0)
    }
    val daysColor = when (textColorPref) {
        WidgetTextColor.AUTO -> accentColor
        WidgetTextColor.WHITE -> Color.White
        WidgetTextColor.BLACK -> Color.Black
        WidgetTextColor.BLUE -> Color(0xFF5B6BC0)
    }

    val scale = fontScale / 100f
    val headerSize = (13f * scale).sp
    val nameSize = (10f * scale).sp
    val daysSize = (15f * scale).sp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity(openAppIntent))
            .padding(8.dp)
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
            Spacer(GlanceModifier.height(6.dp))
        }

        if (events.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет событий",
                    style = TextStyle(
                        color = ColorProvider(textColor),
                        fontSize = nameSize
                    )
                )
            }
        } else {
            val rows = events.chunked(2)
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rows.forEach { rowEvents ->
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowEvents.forEach { ev ->
                            MiniTile(
                                ev = ev,
                                cardColor = cardColor,
                                textColor = textColor,
                                daysColor = daysColor,
                                nameSize = nameSize,
                                daysSize = daysSize,
                                daysOnly = daysOnly,
                                modifier = GlanceModifier.defaultWeight().fillMaxHeight()
                            )
                        }
                        if (rowEvents.size == 1) {
                            Spacer(GlanceModifier.defaultWeight())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniTile(
    ev: WidgetEvent,
    cardColor: Color,
    textColor: Color,
    daysColor: Color,
    nameSize: androidx.compose.ui.unit.TextUnit,
    daysSize: androidx.compose.ui.unit.TextUnit,
    daysOnly: Boolean,
    modifier: GlanceModifier
) {
    Box(
        modifier = modifier.background(cardColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val photoRes = ev.presetAvatar?.let { presetAvatarDrawable(it) }
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
                        text = ev.name.firstOrNull()?.uppercase() ?: "?",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF394099)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(GlanceModifier.height(4.dp))

            Text(
                text = ev.name,
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = nameSize,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(GlanceModifier.height(2.dp))

            Text(
                text = when {
                    ev.daysLeft == 0L -> "сегодня"
                    daysOnly -> "${ev.daysLeft} дн."
                    else -> "${ev.daysLeft} дн."
                },
                style = TextStyle(
                    color = ColorProvider(daysColor),
                    fontSize = daysSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
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

class EventsWidgetMiniReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EventsWidgetMini()
}
