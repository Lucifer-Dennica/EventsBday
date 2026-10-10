package ru.yourname.events.data.settings

enum class AppIcon(val label: String, val alias: String) {
    MAIN("Торт", "ru.yourname.events.MainActivityDefault"),
    CAKE("Тортик со свечами", "ru.yourname.events.MainActivityCake"),
    CALENDAR("Календарь", "ru.yourname.events.MainActivityCalendar"),
    GIFT("Подарок", "ru.yourname.events.MainActivityGift");

    companion object {
        fun allAliases(): List<String> = entries.map { it.alias }
    }
}
