package ru.yourname.events.widget

import android.content.Context
import kotlinx.coroutines.flow.first
import ru.yourname.events.data.db.AppDatabase
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.repository.EventRepository
import java.time.LocalDate

data class WidgetEvent(
    val id: Long,
    val name: String,
    val dayMonth: String,
    val daysLeft: Long,
    val group: String,
    val presetAvatar: Int?
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
            group = group,
            presetAvatar = presetAvatar
        )
    }
}
