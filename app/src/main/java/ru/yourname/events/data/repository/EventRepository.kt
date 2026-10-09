package ru.yourname.events.data.repository

import kotlinx.coroutines.flow.map
import ru.yourname.events.data.db.EventDao
import ru.yourname.events.data.db.EventEntity
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.model.GroupType

class EventRepository(private val dao: EventDao) {

    val events = dao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun upsert(event: Event): Long =
        dao.upsert(event.toEntity())

    suspend fun upsertAll(events: List<Event>) =
        dao.upsertAll(events.map { it.toEntity() })

    suspend fun delete(event: Event) = dao.delete(event.toEntity())

    private fun EventEntity.toModel() = Event(
        id, name, dayOfMonth, month, year,
        runCatching { GroupType.valueOf(group) }.getOrDefault(GroupType.OTHER),
        note, photoUri, contactId, isFromContacts
    )

    private fun Event.toEntity() = EventEntity(
        id, name, dayOfMonth, month, year,
        group.name, note, photoUri, contactId, isFromContacts
    )
}
