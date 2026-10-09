package ru.yourname.events.data.model

data class Event(
    val id: Long = 0,
    val name: String,
    val dayOfMonth: Int,
    val month: Int,   // 1..12
    val year: Int? = null,
    val group: GroupType = GroupType.OTHER,
    val note: String = "",
    val photoUri: String? = null,
    val contactId: Long? = null,
    val isFromContacts: Boolean = false
) {
    fun nextOccurrence(from: java.time.LocalDate): java.time.LocalDate {
        var date = java.time.LocalDate.of(from.year, month, dayOfMonth)
        if (date.isBefore(from)) date = date.plusYears(1)
        return date
    }

    fun daysUntil(from: java.time.LocalDate): Long =
        java.time.temporal.ChronoUnit.DAYS.between(from, nextOccurrence(from))

    fun ageAtNext(from: java.time.LocalDate): Int? {
        val y = year ?: return null
        return nextOccurrence(from).year - y
    }
}
