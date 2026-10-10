package ru.yourname.events.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class SocialLink(
    val platform: SocialPlatform,
    val value: String
)

data class Event(
    val id: Long = 0,
    val name: String,
    val dayOfMonth: Int,
    val month: Int,
    val year: Int? = null,
    val group: String = "Знакомые",
    val note: String = "",
    val photoUri: String? = null,
    val presetAvatar: Int? = null,
    val contactId: Long? = null,
    val isFromContacts: Boolean = false,
    val socialLinks: List<SocialLink> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val cardColor: Int = 0,
    val isPinned: Boolean = false,
    val animation: CardAnimation = CardAnimation.NONE
) {
    fun nextOccurrence(from: LocalDate): LocalDate {
        var date = LocalDate.of(from.year, month, dayOfMonth)
        if (date.isBefore(from)) date = date.plusYears(1)
        return date
    }

    fun daysUntil(from: LocalDate): Long =
        ChronoUnit.DAYS.between(from, nextOccurrence(from))

    fun ageAtNext(from: LocalDate): Int? {
        val y = year ?: return null
        return nextOccurrence(from).year - y
    }

    fun westernZodiac(): WesternZodiac = WesternZodiac.fromDate(dayOfMonth, month)
    fun easternZodiac(): EasternZodiac? = year?.let { EasternZodiac.fromYear(it) }
    fun chineseElement(): ChineseElement? = year?.let { ChineseElement.fromYear(it) }
}
