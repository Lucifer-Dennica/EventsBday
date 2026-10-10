package ru.yourname.events.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.model.cardColorAt
import ru.yourname.events.data.settings.CalendarMarkStyle
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarView(
    events: List<Event>,
    selectedDate: LocalDate?,
    markStyle: CalendarMarkStyle,
    onDayClick: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val today = LocalDate.now()

    // События по датам в текущем отображаемом году + следующий год для уже прошедших
    val eventsByDate: Map<LocalDate, List<Event>> = remember(events, currentMonth) {
        val result = mutableMapOf<LocalDate, MutableList<Event>>()
        events.forEach { e ->
            // Дата в текущем году
            val thisYear = runCatching {
                LocalDate.of(currentMonth.year, e.month, e.dayOfMonth)
            }.getOrNull()
            // Если она в прошлом — берём следующий год
            val date = when {
                thisYear == null -> null
                thisYear.isBefore(today) -> runCatching {
                    LocalDate.of(currentMonth.year + 1, e.month, e.dayOfMonth)
                }.getOrNull()
                else -> thisYear
            } ?: return@forEach

            if (date.year == currentMonth.year && date.month == currentMonth.month) {
                result.getOrPut(date) { mutableListOf() }.add(e)
            }
        }
        result
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {

        // === Шапка: месяц и стрелки ===
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Предыдущий месяц")
            }
            TextButton(onClick = { showMonthPicker = true }) {
                Text(
                    text = currentMonth.format(
                        DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
                    ).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Следующий месяц")
            }
        }

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

        Spacer(Modifier.height(4.dp))

        // === Сетка с поддержкой свайпа для смены месяца ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onDragEnd = {
                            if (totalDrag < -100f) {
                                currentMonth = currentMonth.plusMonths(1)
                            } else if (totalDrag > 100f) {
                                currentMonth = currentMonth.minusMonths(1)
                            }
                        }
                    ) { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                }
        ) {
            val firstOfMonth = currentMonth.atDay(1)
            val firstDayOfWeek = firstOfMonth.dayOfWeek.value // 1=Пн
            val daysInMonth = currentMonth.lengthOfMonth()
            val totalCells = firstDayOfWeek - 1 + daysInMonth
            val rows = (totalCells + 6) / 7
            val totalSlots = rows * 7

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = false,
                verticalArrangement = Arrangement.spacedBy(2.dp)
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
                            isSelected = date == selectedDate,
                            eventsForDay = eventsForDay,
                            markStyle = markStyle,
                            onClick = { onDayClick(date) }
                        )
                    } else {
                        Spacer(Modifier.height(48.dp))
                    }
                }
            }
        }
    }

    // === Диалог выбора месяца и года ===
    if (showMonthPicker) {
        MonthYearPickerDialog(
            initial = currentMonth,
            onDismiss = { showMonthPicker = false },
            onPick = {
                currentMonth = it
                showMonthPicker = false
            }
        )
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    dayNumber: Int,
    isToday: Boolean,
    isSelected: Boolean,
    eventsForDay: List<Event>,
    markStyle: CalendarMarkStyle,
    onClick: () -> Unit
) {
    val bg = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    val fg = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderColor = when {
        isToday && !isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(
                width = if (isToday && !isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                dayNumber.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = fg,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (eventsForDay.isNotEmpty() && markStyle != CalendarMarkStyle.NONE) {
                Spacer(Modifier.height(3.dp))
                when (markStyle) {
                    CalendarMarkStyle.DOTS -> {
                        val dots = eventsForDay.take(3)
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            dots.forEach { ev ->
                                val color = if (ev.cardColor == 0) MaterialTheme.colorScheme.primary
                                            else cardColorAt(ev.cardColor)
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else color)
                                )
                            }
                        }
                    }
                    CalendarMarkStyle.BAR -> {
                        val color = eventsForDay.first().let {
                            if (it.cardColor == 0) MaterialTheme.colorScheme.primary else cardColorAt(it.cardColor)
                        }
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else color)
                        )
                    }
                    CalendarMarkStyle.SQUARE -> {
                        val color = eventsForDay.first().let {
                            if (it.cardColor == 0) MaterialTheme.colorScheme.primary else cardColorAt(it.cardColor)
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else color)
                        )
                    }
                    CalendarMarkStyle.NONE -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthYearPickerDialog(
    initial: YearMonth,
    onDismiss: () -> Unit,
    onPick: (YearMonth) -> Unit
) {
    var year by remember { mutableStateOf(initial.year) }
    var month by remember { mutableStateOf(initial.monthValue) }

    val monthNames = listOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выбор месяца и года") },
        text = {
            Column {
                // Год со стрелками
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { year -= 1 }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                    }
                    Text(
                        year.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(onClick = { year += 1 }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Сетка месяцев 4x3
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..11 step 4).forEach { rowStart ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (rowStart..rowStart + 3).forEach { idx ->
                                val isSelected = (idx + 1) == month
                                OutlinedButton(
                                    onClick = { month = idx + 1 },
                                    modifier = Modifier.weight(1f),
                                    colors = if (isSelected)
                                        ButtonDefaults.outlinedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    else ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text(
                                        monthNames[idx].take(3),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onPick(YearMonth.of(year, month)) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
