package ru.yourname.events.widget

import android.content.Context
import kotlinx.coroutines.flow.first
import ru.yourname.events.data.db.AppDatabase
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.repository.EventRepository
import java.time.LocalDate
import java.time.YearMonth

data class WidgetEvent(
    val id: Long,
    val name: String,
    val dayMonth: String,
    val daysLeft: Long,
    val age: Int?,
    val group: String,
    val photoUri: String?,
    val presetAvatar: Int?
)

data class WidgetCalendarDay(
    val day: Int,
    val isToday: Boolean,
    val hasEvents: Boolean
)

data class WidgetCalendarMonth(
    val title: String,
    val weekdays: List<String>,
    val days: List<WidgetCalendarDay?>
)

object WidgetDataProvider {

    suspend fun loadUpcoming(ctx: Context, limit: Int): List<WidgetEvent> {
        val repo = EventRepository(AppDatabase.get(ctx).eventDao())
        val all = repo.events.first()
        val today = LocalDate.now()
        return all
            .sortedBy { it.daysUntil(today) }
            .take(limit)
            .map { it.toWidgetEvent(today) }
    }

    suspend fun loadCalendar(ctx: Context): WidgetCalendarMonth {
        val repo = EventRepository(AppDatabase.get(ctx).eventDao())
        val all = repo.events.first()
        val today = LocalDate.now()
        val current = YearMonth.now()

        val eventDays = all.mapNotNull { e ->
            runCatching {
                val d = LocalDate.of(current.year, e.month, e.dayOfMonth)
                if (d.month == current.month) d.dayOfMonth else null
            }.getOrNull()
        }.toSet()

        // Русские названия месяцев
        val monthName = current.month.getDisplayName(
            java.time.format.TextStyle.FULL_STANDALONE,
            java.util.Locale("ru")
        ).replaceFirstChar { it.uppercase() }

        val firstOfMonth = current.atDay(1)
        val firstDayOfWeek = firstOfMonth.dayOfWeek.value // 1 = Пн
        val daysInMonth = current.lengthOfMonth()
        val totalCells = firstDayOfWeek - 1 + daysInMonth
        val rows = (totalCells + 6) / 7
        val totalSlots = rows * 7

        val days = (0 until totalSlots).map { idx ->
            val dayNum = idx - (firstDayOfWeek - 1) + 1
            if (dayNum in 1..daysInMonth) {
                WidgetCalendarDay(
                    day = dayNum,
                    isToday = (dayNum == today.dayOfMonth &&
                            current.month == today.month &&
                            current.year == today.year),
                    hasEvents = dayNum in eventDays
                )
            } else null
        }

        return WidgetCalendarMonth(
            title = "$monthName ${current.year}",
            weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"),
            days = days
        )
    }

    private fun Event.toWidgetEvent(today: LocalDate): WidgetEvent {
        val next = nextOccurrence(today)
        val monthShort = next.month.getDisplayName(
            java.time.format.TextStyle.SHORT,
            java.util.Locale("ru")
        )
        return WidgetEvent(
            id = id,
            name = name,
            dayMonth = "${next.dayOfMonth} $monthShort",
            daysLeft = daysUntil(today),
            age = ageAtNext(today),
            group = group,
            photoUri = photoUri,
            presetAvatar = presetAvatar
        )
    }
}
