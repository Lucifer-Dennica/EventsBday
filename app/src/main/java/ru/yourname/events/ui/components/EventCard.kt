package ru.yourname.events.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.model.cardColorAt
import ru.yourname.events.data.settings.CardBorderColor
import ru.yourname.events.data.settings.CardGlowStyle
import ru.yourname.events.data.settings.DaysFormat
import ru.yourname.events.data.settings.ListDensity
import ru.yourname.events.data.settings.NeonMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventCard(
    event: Event,
    isSelected: Boolean,
    selectionMode: Boolean,
    showNotificationsIcon: Boolean,
    showPinIcon: Boolean,
    animationsAllowed: Boolean = true,
    borderWidth: Int = 2,
    borderColorPref: CardBorderColor = CardBorderColor.AUTO,
    borderGlow: Boolean = false, // deprecated — теперь design.glowStyle
    neonBorder: Boolean = false, // deprecated — теперь design.neonMode
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleNotifications: () -> Unit,
    onTogglePin: () -> Unit
) {
    val design = LocalDesignConfig.current
    val today = LocalDate.now()
    val days = event.daysUntil(today)
    val accent = MaterialTheme.colorScheme.primary

    // Обратная совместимость со старыми параметрами
    val effectiveGlowStyle = when {
        design.glowStyle != CardGlowStyle.NONE -> design.glowStyle
        borderGlow -> CardGlowStyle.GLOW
        else -> CardGlowStyle.NONE
    }
    val effectiveNeonMode = when {
        design.neonMode != NeonMode.OFF -> design.neonMode
        neonBorder -> NeonMode.RUNNING
        else -> NeonMode.OFF
    }
    val glowIntensity = (design.glowIntensity / 100f).coerceIn(0f, 1f)

    val hasCustomColor = !event.customColorHex.isNullOrBlank()
    val hasCustomBg = event.cardColor != 0
    val playAnimation = animationsAllowed && event.animation.name != "NONE"
    val animOverride = if (playAnimation) event.animation.backgroundOverride else null
    val hasAnimOverride = animOverride != null

    val customBrush = if (hasCustomColor) customColorBrush(event.customColorHex) else null
    val customPrimary = if (hasCustomColor) customColorPrimary(event.customColorHex) else null

    val solidBgColor = when {
        hasAnimOverride -> Color(animOverride!!)
        hasCustomColor && customPrimary != null -> customPrimary
        hasCustomBg -> cardColorAt(event.cardColor)
        else -> MaterialTheme.colorScheme.surface
    }

    val isDarkBg = when {
        hasAnimOverride -> true
        hasCustomColor && customPrimary != null -> !customPrimary.isLight()
        hasCustomBg -> false
        else -> MaterialTheme.colorScheme.surface.isLight().not()
    }

    val textColor = when {
        hasAnimOverride -> Color(0xFFF2F3FF)
        hasCustomColor -> if (isDarkBg) Color(0xFFF2F3FF) else Color(0xFF1A1C22)
        hasCustomBg -> Color(0xFF1A1C22)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val subTextColor = when {
        hasAnimOverride -> Color(0xFFB0B5CC)
        hasCustomColor -> if (isDarkBg) Color(0xFFB0B5CC) else Color(0xFF5B6478)
        hasCustomBg -> Color(0xFF5B6478)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val accentOnCard = when {
        hasAnimOverride -> Color(0xFFB8C2FF)
        hasCustomColor -> if (isDarkBg) Color(0xFFD0D8FF) else Color(0xFF394099)
        hasCustomBg -> Color(0xFF394099)
        else -> accent
    }

    // Цвет обводки: приоритет per-card, иначе глобальный
    val cardBorderColor: Color = if (!event.borderColorHex.isNullOrBlank()) {
        runCatching { Color(android.graphics.Color.parseColor(event.borderColorHex)) }
            .getOrElse { accent }
    } else when (borderColorPref) {
        CardBorderColor.AUTO -> when {
            isSelected -> accent
            event.isPinned -> accent.copy(alpha = 0.6f)
            hasAnimOverride || (hasCustomColor && isDarkBg) -> Color(0x33FFFFFF)
            hasCustomBg || hasCustomColor -> Color(0xFFB8BEC9)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        }
        CardBorderColor.ACCENT -> accent
        else -> borderColorPref.hex?.let { Color(it) } ?: accent
    }

    val actualBorderWidth = when {
        isSelected || event.isPinned -> 2.dp
        else -> borderWidth.dp
    }

    val cornerRadiusDp = design.cornerRadius.dp
    val cardShape = RoundedCornerShape(cornerRadiusDp)

    val needShadow = effectiveGlowStyle == CardGlowStyle.SHADOW || effectiveGlowStyle == CardGlowStyle.GLOW_SHADOW
    val needGlow = effectiveGlowStyle == CardGlowStyle.GLOW || effectiveGlowStyle == CardGlowStyle.GLOW_SHADOW

    // Анимация для неона
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val borderPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "borderPhase"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = cardShape,
        border = if (effectiveNeonMode == NeonMode.OFF)
            BorderStroke(width = actualBorderWidth, color = cardBorderColor)
        else null,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (needShadow) {
                    Modifier.shadow(
                        elevation = (8 + 8 * glowIntensity).dp,
                        shape = cardShape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.35f * glowIntensity),
                        spotColor = Color.Black.copy(alpha = 0.45f * glowIntensity)
                    )
                } else Modifier
            )
            .then(
                if (needGlow) {
                    Modifier.drawWithContent {
                        // Сначала рисуем свечение (под содержимым)
                        drawGlowLayers(cardBorderColor, cornerRadiusDp.toPx(), glowIntensity)
                        drawContent()
                    }
                } else Modifier
            )
            .then(
                if (effectiveNeonMode != NeonMode.OFF) {
                    Modifier.drawWithContent {
                        drawContent()
                        drawNeonBorder(
                            mode = effectiveNeonMode,
                            baseColor = cardBorderColor,
                            borderWidth = actualBorderWidth,
                            cornerDp = cornerRadiusDp,
                            phase = borderPhase
                        )
                    }
                } else Modifier
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        val innerPadding = when (design.density) {
            ListDensity.COMPACT -> 10.dp
            ListDensity.NORMAL -> 14.dp
            ListDensity.SPACIOUS -> 18.dp
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = customBrush ?: androidx.compose.ui.graphics.SolidColor(solidBgColor),
                    shape = cardShape
                )
        ) {
            if (playAnimation) {
                AnimatedBackground(
                    type = event.animation.name,
                    tint = accentOnCard,
                    modifier = Modifier.matchParentSize().clip(cardShape)
                )
            }

            Row(
                modifier = Modifier.padding(innerPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectionMode) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (isSelected) accent else subTextColor, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                }

                EventAvatar(event, accentColor = accentOnCard)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (event.isPinned) {
                            Icon(Icons.Default.PushPin, contentDescription = "Закреплено", tint = accentOnCard, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            text = event.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = design.titleFontSize.sp.sp),
                            color = textColor
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = buildDateString(event, today, design.showDayOfWeek),
                        style = MaterialTheme.typography.bodyMedium,
                        color = subTextColor
                    )
                    if (event.group.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        GroupChip(event.group, accentOnCard)
                    }
                }
                Spacer(Modifier.width(6.dp))
                Column(horizontalAlignment = Alignment.End) {
                    if (days == 0L) {
                        Text(
                            text = "сегодня",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentOnCard
                        )
                    } else {
                        Text(
                            text = formatDays(days, design.daysFormat),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (days <= 7) accentOnCard else textColor
                        )
                    }
                    if (showNotificationsIcon || showPinIcon) {
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (showNotificationsIcon) {
                                SmallIconTap(
                                    icon = if (event.notificationsEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                    contentDescription = if (event.notificationsEnabled) "Уведомления включены" else "Уведомления выключены",
                                    tint = if (event.notificationsEnabled) accentOnCard else subTextColor,
                                    onClick = onToggleNotifications
                                )
                            }
                            if (showPinIcon) {
                                SmallIconTap(
                                    icon = Icons.Default.PushPin,
                                    contentDescription = if (event.isPinned) "Открепить" else "Закрепить",
                                    tint = if (event.isPinned) accentOnCard else subTextColor,
                                    onClick = onTogglePin
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallIconTap(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, tint: Color, onClick: () -> Unit) {
    Box(modifier = Modifier.size(30.dp).clip(CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun GroupChip(group: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.18f)) {
        Text(text = group, style = MaterialTheme.typography.labelSmall, color = color, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
    }
}

private fun buildDateString(event: Event, today: LocalDate, showDayOfWeek: Boolean): String {
    val next = event.nextOccurrence(today)
    val datePart = next.format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru")))
    val base = if (showDayOfWeek) {
        val dow = next.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru"))
            .replaceFirstChar { it.uppercase() }
        "$dow, $datePart"
    } else datePart
    val age = event.ageAtNext(today)
    return if (age != null) "$base • $age лет" else base
}

private fun formatDays(days: Long, format: DaysFormat): String {
    val plural = when {
        days % 10L == 1L && days % 100L != 11L -> "день"
        days % 10L in 2L..4L && (days % 100L < 10L || days % 100L >= 20L) -> "дня"
        else -> "дней"
    }
    return when (format) {
        DaysFormat.SHORT -> "$days дн."
        DaysFormat.FULL -> "$days $plural"
        DaysFormat.PREFIX -> "через $days"
    }
}

// === Усиленное свечение вокруг карточки ===
private fun DrawScope.drawGlowLayers(color: Color, cornerPx: Float, intensity: Float) {
    if (intensity <= 0f) return
    val steps = 6
    val maxExtra = 18.dp.toPx()
    for (i in steps downTo 1) {
        val fraction = i.toFloat() / steps
        val extra = maxExtra * fraction
        val alpha = 0.10f * intensity * (1f - fraction * 0.6f)
        drawRoundRect(
            color = color.copy(alpha = alpha.coerceAtLeast(0f)),
            topLeft = Offset(-extra, -extra),
            size = Size(size.width + extra * 2, size.height + extra * 2),
            cornerRadius = CornerRadius(cornerPx + extra * 0.5f),
            style = Stroke(width = extra * 0.9f)
        )
    }
}

// === 4 режима неоновой обводки ===
private fun DrawScope.drawNeonBorder(
    mode: NeonMode,
    baseColor: Color,
    borderWidth: Dp,
    cornerDp: Dp,
    phase: Float
) {
    val strokePx = borderWidth.value.dp.toPx().coerceAtLeast(2.dp.toPx())
    val cornerPx = cornerDp.toPx()

    // Палитра неона. Для анимированной — цвета интерполируются по фазе
    val staticColors = listOf(
        Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFFFFEB3B),
        Color(0xFF4CAF50), Color(0xFF00BCD4), Color(0xFF3F51B5),
        Color(0xFF9C27B0), Color(0xFFE91E63)
    )

    when (mode) {
        NeonMode.OFF -> {}

        NeonMode.RUNNING -> {
            val inset = strokePx / 2f
            val innerSize = Size(size.width - strokePx, size.height - strokePx)
            val innerTopLeft = Offset(inset, inset)

            val brush = Brush.sweepGradient(
                colors = staticColors,
                center = Offset(size.width / 2f, size.height / 2f)
            )

            // Базовая тусклая обводка (чтобы не было пустоты)
            drawRoundRect(
                color = baseColor.copy(alpha = 0.18f),
                topLeft = innerTopLeft,
                size = innerSize,
                cornerRadius = CornerRadius(cornerPx),
                style = Stroke(width = strokePx)
            )

            // Бегущий сегмент
            val rect = Rect(inset, inset, size.width - inset, size.height - inset)
            val borderPath = Path().apply {
                addRoundRect(RoundRect(rect, CornerRadius(cornerPx)))
            }
            val measure = PathMeasure()
            measure.setPath(borderPath, false)
            val totalLen = measure.length
            if (totalLen > 0f) {
                val dashLen = totalLen * 0.28f
                val head = phase * totalLen
                val tail = head - dashLen
                val segment = Path()
                if (tail >= 0f) {
                    measure.getSegment(tail, head, segment, true)
                } else {
                    measure.getSegment(totalLen + tail, totalLen, segment, true)
                    measure.getSegment(0f, head, segment, true)
                }
                drawPath(
                    path = segment,
                    brush = brush,
                    alpha = 0.40f,
                    style = Stroke(width = strokePx + 8.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = segment,
                    brush = brush,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        NeonMode.FULL_STATIC -> {
            val brush = Brush.sweepGradient(
                colors = staticColors,
                center = Offset(size.width / 2f, size.height / 2f)
            )
            val inset = strokePx / 2f
            val innerSize = Size(size.width - strokePx, size.height - strokePx)
            val innerTopLeft = Offset(inset, inset)

            // Внешнее свечение
            drawRoundRect(
                brush = brush,
                topLeft = innerTopLeft,
                size = innerSize,
                cornerRadius = CornerRadius(cornerPx),
                alpha = 0.40f,
                style = Stroke(width = strokePx + 8.dp.toPx())
            )
            // Чёткая линия
            drawRoundRect(
                brush = brush,
                topLeft = innerTopLeft,
                size = innerSize,
                cornerRadius = CornerRadius(cornerPx),
                style = Stroke(width = strokePx)
            )
        }

        NeonMode.FULL_ANIMATED -> {
            // Плавная интерполяция цветов по фазе — эффект «переливания»
            val animatedColors = animatedSweepColors(phase)
            val brush = Brush.sweepGradient(
                colors = animatedColors,
                center = Offset(size.width / 2f, size.height / 2f)
            )
            val inset = strokePx / 2f
            val innerSize = Size(size.width - strokePx, size.height - strokePx)
            val innerTopLeft = Offset(inset, inset)

            drawRoundRect(
                brush = brush,
                topLeft = innerTopLeft,
                size = innerSize,
                cornerRadius = CornerRadius(cornerPx),
                alpha = 0.40f,
                style = Stroke(width = strokePx + 8.dp.toPx())
            )
            drawRoundRect(
                brush = brush,
                topLeft = innerTopLeft,
                size = innerSize,
                cornerRadius = CornerRadius(cornerPx),
                style = Stroke(width = strokePx)
            )
        }
    }
}

// Плавный циклический сдвиг цветов для анимации градиента
private fun animatedSweepColors(phase: Float): List<Color> {
    val base = listOf(
        Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFFFFEB3B),
        Color(0xFF4CAF50), Color(0xFF00BCD4), Color(0xFF3F51B5),
        Color(0xFF9C27B0)
    )
    val n = base.size
    val t = phase * n
    val idx = t.toInt() % n
    val frac = t - t.toInt()
    val result = (0 until n).map { i ->
        val c1 = base[(i + idx) % n]
        val c2 = base[(i + idx + 1) % n]
        Color(
            red = c1.red + (c2.red - c1.red) * frac,
            green = c1.green + (c2.green - c1.green) * frac,
            blue = c1.blue + (c2.blue - c1.blue) * frac,
            alpha = 1f
        )
    }
    return result + result[0]
}
