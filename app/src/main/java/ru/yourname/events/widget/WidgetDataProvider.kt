package ru.yourname.events.widget

import android.content.Context
import kotlinx.coroutines.flow.first
import ru.yourname.events.data.db.AppDatabase
import ru.yourname.events.data.model.CardAnimation
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.repository.EventRepository
import java.time.LocalDate

data class WidgetEvent(
    val name: String,
    val dayMonth: String,
    val daysLeft: Long,
    val age: Int?,
    val group: String,
    val photoUri: String?,
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

    suspend fun loadNearest(ctx: Context): WidgetEvent? {
        return loadUpcoming(ctx, 1).firstOrNull()
    }

    private fun Event.toWidgetEvent(today: LocalDate): WidgetEvent {
        val next = nextOccurrence(today)
        return WidgetEvent(
            name = name,
            dayMonth = "${next.dayOfMonth} ${
                next.month.getDisplayName(
                    java.time.format.TextStyle.SHORT,
                    java.util.Locale("ru")
                )
            }",
            daysLeft = daysUntil(today),
            age = ageAtNext(today),
            group = group,
            photoUri = photoUri,
            presetAvatar = presetAvatar
        )
    }
}

private fun LocalDate.monthDisplayName(): String =
    month.getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, java.util.Locale("ru"))
