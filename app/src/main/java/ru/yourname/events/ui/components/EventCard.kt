package ru.yourname.events.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.model.cardColorAt
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleNotifications: () -> Unit,
    onTogglePin: () -> Unit
) {
    val today = LocalDate.now()
    val days = event.daysUntil(today)
    val accent = MaterialTheme.colorScheme.primary

    val hasCustomColor = !event.customColorHex.isNullOrBlank()
    val hasCustomBg = event.cardColor != 0
    val playAnimation = animationsAllowed && event.animation.name != "NONE"
    val animOverride = if (playAnimation) event.animation.backgroundOverride else null
    val hasAnimOverride = animOverride != null

    // Верхний приоритет: анимация с override > пользовательский цвет > preset-цвет > тема
    val customBrush = if (hasCustomColor) customColorBrush(event.customColorHex) else null
    val customPrimary = if (hasCustomColor) customColorPrimary(event.customColorHex) else null

    val solidBgColor = when {
        hasAnimOverride -> Color(animOverride!!)
        hasCustomColor && customPrimary != null -> customPrimary
        hasCustomBg -> cardColorAt(event.cardColor)
        else -> MaterialTheme.colorScheme.surface
    }

    // Текст: если фон тёмный — светлый, иначе тёмный/по теме
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
    val borderColor = when {
        isSelected -> accent
        event.isPinned -> accent.copy(alpha = 0.6f)
        hasAnimOverride || (hasCustomColor && isDarkBg) -> Color(0x33FFFFFF)
        hasCustomBg || hasCustomColor -> Color(0xFFB8BEC9)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    val cardShape = RoundedCornerShape(18.dp)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(width = if (isSelected || event.isPinned) 2.dp else 1.dp, color = borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
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
                    modifier = Modifier
                        .matchParentSize()
                        .clip(cardShape)
                )
            }

            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectionMode) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isSelected) accent else subTextColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                }

                EventAvatar(event, size = 54.dp, accentColor = accentOnCard)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (event.isPinned) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = "Закреплено",
                                tint = accentOnCard,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(event.name, style = MaterialTheme.typography.titleMedium, color = textColor)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = buildDateString(event, today),
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
                    Text(
                        text = if (days == 0L) "сегодня" else "$days",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (days <= 7) accentOnCard else textColor
                    )
                    if (days > 0L) {
                        Text("дн.", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                    }
                    if (showNotificationsIcon || showPinIcon) {
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (showNotificationsIcon) {
                                SmallIconTap(
                                    icon = if (event.notificationsEnabled)
                                        Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                    contentDescription = if (event.notificationsEnabled)
                                        "Уведомления включены" else "Уведомления выключены",
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
private fun SmallIconTap(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun GroupChip(group: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.18f)
    ) {
        Text(
            text = group,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

private fun buildDateString(event: Event, today: LocalDate): String {
    val fmt = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
    val base = event.nextOccurrence(today).format(fmt)
    val age = event.ageAtNext(today)
    return if (age != null) "$base • $age лет" else base
}
