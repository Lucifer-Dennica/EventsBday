package ru.yourname.events.data.repository

import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import ru.yourname.events.data.db.EventDao
import ru.yourname.events.data.db.EventEntity
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.model.SocialLink
import ru.yourname.events.data.model.SocialPlatform

class EventRepository(private val dao: EventDao) {

    val events = dao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun upsert(event: Event): Long = dao.upsert(event.toEntity())
    suspend fun upsertAll(events: List<Event>) = dao.upsertAll(events.map { it.toEntity() })
    suspend fun delete(event: Event) = dao.delete(event.toEntity())

    private fun EventEntity.toModel() = Event(
        id = id, name = name, dayOfMonth = dayOfMonth, month = month, year = year,
        group = group, note = note, photoUri = photoUri, presetAvatar = presetAvatar,
        contactId = contactId, isFromContacts = isFromContacts,
        socialLinks = parseSocials(socialLinksRaw),
        notificationsEnabled = notificationsEnabled,
        cardColor = cardColor
    )

    private fun Event.toEntity() = EventEntity(
        id = id, name = name, dayOfMonth = dayOfMonth, month = month, year = year,
        group = group, note = note, photoUri = photoUri, presetAvatar = presetAvatar,
        contactId = contactId, isFromContacts = isFromContacts,
        socialLinksRaw = serializeSocials(socialLinks),
        notificationsEnabled = notificationsEnabled,
        cardColor = cardColor
    )

    private fun parseSocials(raw: String): List<SocialLink> {
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                SocialLink(
                    platform = SocialPlatform.fromKey(o.getString("p")),
                    value = o.getString("v")
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun serializeSocials(list: List<SocialLink>): String {
        if (list.isEmpty()) return ""
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("p", it.platform.key)
                put("v", it.value)
            })
        }
        return arr.toString()
    }
}
