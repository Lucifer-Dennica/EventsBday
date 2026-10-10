package ru.yourname.events.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
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
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val today = LocalDate.now()

    val eventsByDate: Map<LocalDate, List<Event>> = remember(events, currentMonth) {
        val result = mutableMapOf<LocalDate, MutableList<Event>>()
        events.forEach { e ->
            val thisYear = runCatching {
                LocalDate.of(currentMonth.year, e.month, e.dayOfMonth)
            }.getOrNull()
            val date = when {
                thisYear == null -> null
                thisYear.isBefore(today) -> runCatching {
                    LocalDate.of(currentMonth.year + 1, e.month, e.dayOfMonth)
                }.getOrNull()
                else -> thisYear
            } ?: return@forEach
            if (date.year == currentMonth.year && date.monthValue == currentMonth.monthValue) {
                result.getOrPut(date) { mutableListOf() }.add(e)
            }
        }
        result
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // === Шапка ===
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

        Spacer(Modifier.height(4.dp))

        // === Дни недели ===
        Row(Modifier.fillMaxWidth()) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
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

        Spacer(Modifier.height(6.dp))

        // === Сетка с свайпом для смены месяца ===
        val firstOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstOfMonth.dayOfWeek.value // 1=Пн
        val daysInMonth = currentMonth.lengthOfMonth()
        val totalCells = firstDayOfWeek - 1 + daysInMonth
        val rows = (totalCells + 6) / 7

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
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                for (row in 0 until rows) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (col in 0 until 7) {
                            val idx = row * 7 + col
                            val dayNumber = idx - (firstDayOfWeek - 1) + 1
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            ) {
                                if (dayNumber in 1..daysInMonth) {
                                    val date = currentMonth.atDay(dayNumber)
                                    DayCell(
                                        date = date,
                                        dayNumber = dayNumber,
                                        isToday = date == today,
                                        isSelected = date == selectedDate,
                                        eventsForDay = eventsByDate[date].orEmpty(),
                                        markStyle = markStyle,
                                        onClick = { onDayClick(date) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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
    val hasEvents = eventsForDay.isNotEmpty()
    val eventColor = eventsForDay.firstOrNull()?.let {
        if (it.cardColor == 0) MaterialTheme.colorScheme.primary else cardColorAt(it.cardColor)
    } ?: MaterialTheme.colorScheme.primary

    val shape = when (markStyle) {
        CalendarMarkStyle.FILLED_CIRCLE -> CircleShape
        else -> RoundedCornerShape(12.dp)
    }

    // Определяем цвета и обводку
    val bgColor: Color
    val textColor: Color
    val borderColor: Color
    val borderWidth: androidx.compose.ui.unit.Dp
    val fontWeight: FontWeight

    when {
        // Выбранный день — плотная заливка primary
        isSelected -> {
            bgColor = MaterialTheme.colorScheme.primary
            textColor = MaterialTheme.colorScheme.onPrimary
            borderColor = Color.Transparent
            borderWidth = 0.dp
            fontWeight = FontWeight.Bold
        }
        // Сегодня + есть событие — заливка цветом события + обводка
        isToday && hasEvents && markStyle != CalendarMarkStyle.NONE -> {
            bgColor = eventColor
            textColor = MaterialTheme.colorScheme.onSurface
            borderColor = MaterialTheme.colorScheme.primary
            borderWidth = 2.dp
            fontWeight = FontWeight.Bold
        }
        // Просто сегодня — обводка без заливки
        isToday -> {
            bgColor = Color.Transparent
            textColor = MaterialTheme.colorScheme.onSurface
            borderColor = MaterialTheme.colorScheme.primary
            borderWidth = 2.dp
            fontWeight = FontWeight.Bold
        }
        // День с событием — по стилю
        hasEvents && markStyle != CalendarMarkStyle.NONE -> {
            when (markStyle) {
                CalendarMarkStyle.FILLED_SQUARE,
                CalendarMarkStyle.FILLED_CIRCLE -> {
                    bgColor = eventColor
                    textColor = MaterialTheme.colorScheme.onSurface
                    borderColor = Color.Transparent
                    borderWidth = 0.dp
                }
                CalendarMarkStyle.OUTLINE -> {
                    bgColor = Color.Transparent
                    textColor = MaterialTheme.colorScheme.onSurface
                    borderColor = eventColor
                    borderWidth = 2.dp
                }
                CalendarMarkStyle.NONE -> {
                    bgColor = Color.Transparent
                    textColor = MaterialTheme.colorScheme.onSurface
                    borderColor = Color.Transparent
                    borderWidth = 0.dp
                }
            }
            fontWeight = FontWeight.Medium
        }
        // Обычный день
        else -> {
            bgColor = Color.Transparent
            textColor = MaterialTheme.colorScheme.onSurface
            borderColor = Color.Transparent
            borderWidth = 0.dp
            fontWeight = FontWeight.Normal
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(bgColor)
            .then(
                if (borderWidth > 0.dp)
                    Modifier.border(borderWidth, borderColor, shape)
                else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            dayNumber.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = fontWeight
        )
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
        "Янв", "Фев", "Мар",
        "Апр", "Май", "Июн",
        "Июл", "Авг", "Сен",
        "Окт", "Ноя", "Дек"
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

                Spacer(Modifier.height(16.dp))

                // Сетка 3x4
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (rowStart in 0..11 step 3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in rowStart until rowStart + 3) {
                                val isSelected = (i + 1) == month
                                Surface(
                                    onClick = { month = i + 1 },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            monthNames[i],
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
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
