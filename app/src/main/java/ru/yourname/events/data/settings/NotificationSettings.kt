package ru.yourname.events.data.settings

/** Режим уведомлений: либо обо всех событиях, либо только о выбранных. */
enum class NotificationMode(val label: String) {
    ALL("Все события"),
    CUSTOM("Выборочно")
}

/** Частота повторных напоминаний до события. */
enum class NotificationFrequency(val label: String, val intervalDays: Int) {
    ONCE("Один раз, в день события", 0),
    EVERY_DAY("Каждый день", 1),
    EVERY_2_DAYS("Раз в 2 дня", 2),
    EVERY_3_DAYS("Раз в 3 дня", 3),
    EVERY_WEEK("Раз в неделю", 7)
}
