package ru.yourname.events.data.settings

/**
 * ⚠️ ВАЖНО: alias-имена должны совпадать с теми, что в AndroidManifest.xml.
 * Если твой applicationId НЕ "ru.yourname.events" — замени префикс в строках ниже.
 */
enum class AppIcon(val label: String, val alias: String) {
    MAIN("Торт", "ru.yourname.events.MainActivityDefault"),
    CAKE("Свечи", "ru.yourname.events.MainActivityCake"),
    CALENDAR("Календарь", "ru.yourname.events.MainActivityCalendar"),
    GIFT("Подарок", "ru.yourname.events.MainActivityGift");

    companion object {
        fun allAliases(): List<String> = entries.map { it.alias }
    }
}
