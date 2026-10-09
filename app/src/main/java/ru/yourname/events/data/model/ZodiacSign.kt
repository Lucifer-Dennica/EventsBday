package ru.yourname.events.data.model

enum class WesternZodiac(
    val label: String, val symbol: String,
    val startMonth: Int, val startDay: Int,
    val endMonth: Int, val endDay: Int,
    val element: String
) {
    ARIES("Овен", "♈", 3, 21, 4, 19, "Огонь"),
    TAURUS("Телец", "♉", 4, 20, 5, 20, "Земля"),
    GEMINI("Близнецы", "♊", 5, 21, 6, 20, "Воздух"),
    CANCER("Рак", "♋", 6, 21, 7, 22, "Вода"),
    LEO("Лев", "♌", 7, 23, 8, 22, "Огонь"),
    VIRGO("Дева", "♍", 8, 23, 9, 22, "Земля"),
    LIBRA("Весы", "♎", 9, 23, 10, 22, "Воздух"),
    SCORPIO("Скорпион", "♏", 10, 23, 11, 21, "Вода"),
    SAGITTARIUS("Стрелец", "♐", 11, 22, 12, 21, "Огонь"),
    CAPRICORN("Козерог", "♑", 12, 22, 1, 19, "Земля"),
    AQUARIUS("Водолей", "♒", 1, 20, 2, 18, "Воздух"),
    PISCES("Рыбы", "♓", 2, 19, 3, 20, "Вода");

    companion object {
        fun fromDate(day: Int, month: Int): WesternZodiac {
            val v = month * 100 + day
            return when {
                v in 321..419 -> ARIES
                v in 420..520 -> TAURUS
                v in 521..620 -> GEMINI
                v in 621..722 -> CANCER
                v in 723..822 -> LEO
                v in 823..922 -> VIRGO
                v in 923..1022 -> LIBRA
                v in 1023..1121 -> SCORPIO
                v in 1122..1221 -> SAGITTARIUS
                v >= 1222 || v <= 119 -> CAPRICORN
                v in 120..218 -> AQUARIUS
                else -> PISCES
            }
        }
    }
}

enum class EasternZodiac(val label: String, val symbol: String) {
    RAT("Крыса", "🐀"), OX("Бык", "🐂"), TIGER("Тигр", "🐅"), RABBIT("Кролик", "🐇"),
    DRAGON("Дракон", "🐉"), SNAKE("Змея", "🐍"), HORSE("Лошадь", "🐎"), GOAT("Коза", "🐐"),
    MONKEY("Обезьяна", "🐒"), ROOSTER("Петух", "🐓"), DOG("Собака", "🐕"), PIG("Свинья", "🐖");

    companion object {
        fun fromYear(year: Int): EasternZodiac = entries[((year - 4) % 12 + 12) % 12]
    }
}

enum class ChineseElement(val label: String, val symbol: String) {
    WOOD("Дерево", "🌳"), FIRE("Огонь", "🔥"), EARTH("Земля", "⛰️"),
    METAL("Металл", "⚙️"), WATER("Вода", "💧");

    companion object {
        fun fromYear(year: Int): ChineseElement {
            val idx = ((year - 4) % 10 + 10) % 10 / 2
            return entries[idx]
        }
    }
}
