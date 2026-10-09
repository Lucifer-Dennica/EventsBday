package ru.yourname.events.data.contacts

import android.content.ContentResolver
import android.provider.ContactsContract
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.model.GroupType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ContactsImporter {

    /**
     * Возвращает список событий из контактов.
     * ВАЖНО: учтены контакты, у которых день рождения записан в Events, а не только в
     * стандартных birthday-полях. Это исправляет баг конкурента.
     */
    fun import(resolver: ContentResolver): List<Event> {
        val result = mutableListOf<Event>()
        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Event.START_DATE,
            ContactsContract.CommonDataKinds.Event.TYPE
        )
        val selection = "${ContactsContract.Data.MIMETYPE} = ? AND " +
                "${ContactsContract.CommonDataKinds.Event.TYPE} = ?"
        val args = arrayOf(
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
        )

        resolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection, selection, args, null
        )?.use { c ->
            while (c.moveToNext()) {
                val contactId = c.getLong(0)
                val name = c.getString(1) ?: continue
                val rawDate = c.getString(2) ?: continue
                val parsed = parseDate(rawDate) ?: continue
                result.add(
                    Event(
                        name = name,
                        dayOfMonth = parsed.first,
                        month = parsed.second,
                        year = parsed.third,
                        group = GroupType.ACQUAINTANCES,
                        contactId = contactId,
                        isFromContacts = true
                    )
                )
            }
        }
        return result
    }

    /** Формат может быть "--MM-dd", "yyyy-MM-dd" или "yyyy-MM-dd'T'..." — обрабатываем все. */
    private fun parseDate(raw: String): Triple<Int, Int, Int?>? {
        return try {
            val cleaned = raw.substringBefore('T')
            val parts = cleaned.split('-')
            when {
                parts.size == 4 && parts[0].isEmpty() -> {  // --MM-dd
                    Triple(parts[2].toInt(), parts[1].toInt(), null)
                }
                parts.size == 3 -> {                          // yyyy-MM-dd
                    Triple(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
                }
                else -> {
                    val fmt = SimpleDateFormat("MM-dd", Locale.US)
                    val d = fmt.parse(cleaned) ?: return null
                    val cal = Calendar.getInstance().apply { time = d }
                    Triple(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, null)
                }
            }
        } catch (_: Exception) { null }
    }
}
