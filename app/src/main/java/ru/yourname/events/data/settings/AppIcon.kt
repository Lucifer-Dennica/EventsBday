package ru.yourname.events.data.settings

enum class AppIcon(val label: String, val alias: String) {
    MAIN("Торт", "ru.eventsbday.app.MainActivityDefault"),
    CAKE("Свечи", "ru.eventsbday.app.MainActivityCake"),
    CALENDAR("Календарь", "ru.eventsbday.app.MainActivityCalendar"),
    GIFT("Подарок", "ru.eventsbday.app.MainActivityGift");

    companion object {
        fun allAliases(): List<String> = entries.map { it.alias }
    }
}
