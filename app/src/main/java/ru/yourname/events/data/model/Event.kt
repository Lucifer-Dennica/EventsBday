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
    val animation: CardAnimation = CardAnimation.NONE,
    val customColorHex: String? = null,
    val checklist: List<ChecklistItem> = emptyList(),
    val repeatYearly: Boolean = true,
    val borderColorHex: String? = null,
    // "COLOR" | "NEON_RUNNING" | "NEON_FULL_STATIC" | "NEON_FULL_ANIMATED" | null
    val borderStyle: String? = null
) {
    fun nextOccurrence(from: LocalDate): LocalDate {
        var date = try {
            LocalDate.of(from.year, month, dayOfMonth)
        } catch (e: java.time.DateTimeException) {
            LocalDate.of(from.year, 2, 28)
        }
        if (date.isBefore(from)) {
            if (repeatYearly) {
                date = try {
                    LocalDate.of(from.year + 1, month, dayOfMonth)
                } catch (e: java.time.DateTimeException) {
                    LocalDate.of(from.year + 1, 2, 28)
                }
            } else {
                return date
            }
        }
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
