package ru.yourname.events.data.settings

/**
 * Alias-и должны совпадать с реальными именами компонентов в AndroidManifest.xml.
 * Так как в манифесте они объявлены относительными (.MainActivityXxx),
 * Android резолвит их через namespace, а НЕ через applicationId.
 * Поэтому префикс = namespace = "ru.yourname.events".
 */
enum class AppIcon(val label: String, val alias: String) {
    MAIN("Торт", "ru.yourname.events.MainActivityDefault"),
    CAKE("Свечи", "ru.yourname.events.MainActivityCake"),
    CALENDAR("Календарь", "ru.yourname.events.MainActivityCalendar"),
    GIFT("Подарок", "ru.yourname.events.MainActivityGift"),
    BALLOONS("Шарики", "ru.yourname.events.MainActivityBalloons"),
    HEART("Сердце", "ru.yourname.events.MainActivityHeart"),
    BEAR("Мишка", "ru.yourname.events.MainActivityBear"),
    PRESENT("Праздник", "ru.yourname.events.MainActivityPresent");

    companion object {
        fun allAliases(): List<String> = entries.map { it.alias }
    }
}
