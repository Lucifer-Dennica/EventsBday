package ru.yourname.events.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.model.GroupType
import ru.yourname.events.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    val today = LocalDate.now()
    val next = event.nextOccurrence(today)
    val days = event.daysUntil(today)
    val groupColor = event.group.color()

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар-инициал
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(groupColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = event.name.trim().take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = groupColor
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(event.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = buildDateString(event, today),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                GroupChip(event.group, groupColor)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (days == 0L) "сегодня" else "$days дн.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (days <= 7) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun GroupChip(group: GroupType, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = "${group.emoji} ${group.label}",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun buildDateString(event: Event, today: LocalDate): String {
    val fmt = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
    val next = event.nextOccurrence(today)
    val base = next.format(fmt)
    val age = event.ageAtNext(today)
    return if (age != null) "$base • исполнится $age" else base
}

private fun GroupType.color(): Color = when (this) {
    GroupType.FAMILY -> GroupFamily
    GroupType.FRIENDS -> GroupFriends
    GroupType.RELATIVES -> GroupRelatives
    GroupType.ACQUAINTANCES -> GroupAcquaintances
    GroupType.WORK -> GroupWork
    GroupType.OTHER -> GroupOther
}
