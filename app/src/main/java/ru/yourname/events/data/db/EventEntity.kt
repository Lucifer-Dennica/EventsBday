package ru.yourname.events.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dayOfMonth: Int,
    val month: Int,
    val year: Int?,
    val group: String,
    val note: String,
    val photoUri: String?,
    val presetAvatar: Int?,
    val contactId: Long?,
    val isFromContacts: Boolean,
    val socialLinksRaw: String,
    val notificationsEnabled: Boolean,
    val cardColor: Int,
    val isPinned: Boolean = false,
    val animation: String = "NONE",
    val customColorHex: String? = null
)
