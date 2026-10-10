package ru.yourname.events.data.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.yourname.events.data.db.AppDatabase
import ru.yourname.events.data.db.EventEntity
import java.io.BufferedReader
import java.io.InputStreamReader

object BackupManager {

    private const val FORMAT_VERSION = 1

    /** Сохраняет все события в файл по указанному URI. Возвращает количество сохранённых. */
    suspend fun export(ctx: Context, uri: Uri): Int = withContext(Dispatchers.IO) {
        val dao = AppDatabase.get(ctx).eventDao()
        val events: List<EventEntity> = dao.observeAll().first()

        val root = JSONObject().apply {
            put("format_version", FORMAT_VERSION)
            put("exported_at", java.time.LocalDateTime.now().toString())
            put("events_count", events.size)
            put("events", JSONArray().apply {
                events.forEach { e ->
                    put(JSONObject().apply {
                        put("name", e.name)
                        put("day_of_month", e.dayOfMonth)
                        put("month", e.month)
                        put("year", e.year)
                        put("group", e.group)
                        put("note", e.note)
                        put("photo_uri", e.photoUri)
                        put("preset_avatar", e.presetAvatar)
                        put("contact_id", e.contactId)
                        put("is_from_contacts", e.isFromContacts)
                        put("social_links", e.socialLinksRaw)
                        put("notifications_enabled", e.notificationsEnabled)
                        put("card_color", e.cardColor)
                    })
                }
            })
        }

        ctx.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(root.toString(2).toByteArray(Charsets.UTF_8))
            out.flush()
        }

        events.size
    }

    /** Загружает события из файла по URI. Возвращает количество добавленных. */
    suspend fun import(ctx: Context, uri: Uri): Int = withContext(Dispatchers.IO) {
        val text = ctx.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
        } ?: return@withContext 0

        val root = JSONObject(text)
        val array = root.optJSONArray("events") ?: return@withContext 0
        val dao = AppDatabase.get(ctx).eventDao()

        val toInsert = mutableListOf<EventEntity>()
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            toInsert.add(
                EventEntity(
                    name = o.getString("name"),
                    dayOfMonth = o.getInt("day_of_month"),
                    month = o.getInt("month"),
                    year = if (o.isNull("year")) null else o.getInt("year"),
                    group = o.optString("group", "Знакомые"),
                    note = o.optString("note", ""),
                    photoUri = if (o.isNull("photo_uri")) null else o.optString("photo_uri"),
                    presetAvatar = if (o.isNull("preset_avatar")) null else o.optInt("preset_avatar"),
                    contactId = if (o.isNull("contact_id")) null else o.optLong("contact_id"),
                    isFromContacts = o.optBoolean("is_from_contacts", false),
                    socialLinksRaw = o.optString("social_links", ""),
                    notificationsEnabled = o.optBoolean("notifications_enabled", true),
                    cardColor = o.optInt("card_color", 0)
                )
            )
        }

        dao.upsertAll(toInsert)
        toInsert.size
    }
}
