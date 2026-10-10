package ru.yourname.events.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.yourname.events.data.model.Event
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarView(
    events: List<Event>,
    onDayClick: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // Карта: дата → список событий в этом году (или следующем, если уже прошло)
    val today = LocalDate.now()
    val eventsByDate: Map<LocalDate, List<Event>> = remember(events, currentMonth) {
        val result = mutableMapOf<LocalDate, MutableList<Event>>()
        events.forEach { e ->
            // Проверяем дату в текущем году и следующем
            for (yearOffset in 0..1) {
                val y = currentMonth.year + yearOffset
                val date = runCatching {
                    LocalDate.of(y, e.month, e.dayOfMonth)
                }.getOrNull() ?: continue
                if (date.isBefore(today)) continue
                result.getOrPut(date) { mutableListOf() }.add(e)
                break
            }
        }
        result
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        // === Шапка: месяц и стрелки ===
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий месяц")
            }
            Text(
                text = currentMonth.format(
                    DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
                ).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий месяц")
            }
        }

        Spacer(Modifier.height(12.dp))

        // === Дни недели ===
        val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        Row(Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // === Сетка ===
        val firstOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstOfMonth.dayOfWeek.value // 1=Пн, 7=Вс
        val daysInMonth = currentMonth.lengthOfMonth()
        val totalCells = firstDayOfWeek - 1 + daysInMonth
        val rows = (totalCells + 6) / 7
        val totalSlots = rows * 7

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(totalSlots) { idx ->
                val dayNumber = idx - (firstDayOfWeek - 1) + 1
                if (dayNumber in 1..daysInMonth) {
                    val date = currentMonth.atDay(dayNumber)
                    val eventsForDay = eventsByDate[date].orEmpty()
                    DayCell(
                        date = date,
                        dayNumber = dayNumber,
                        isToday = date == today,
                        hasEvents = eventsForDay.isNotEmpty(),
                        onClick = { onDayClick(date) }
                    )
                } else {
                    Spacer(Modifier.height(44.dp))
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    dayNumber: Int,
    isToday: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                dayNumber.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = fg,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            if (hasEvents) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            if (isToday) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}
