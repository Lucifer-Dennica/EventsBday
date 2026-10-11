package ru.yourname.events.data.model

enum class CardAnimation(
    val label: String,
    val emoji: String,
    val backgroundOverride: Long? = null
) {
    NONE("Без анимации", "🚫", null),
    BUBBLES("Пузырьки", "🫧", null),
    FIRE("Огонь", "🔥", 0xFF000000),
    FIRE_CANDLES("Свечи", "🕯️", 0xFF1A0A00),
    SNOW("Снег", "❄️", null),
    STARS("Звёзды", "✨", 0xFF0A0F2C),
    LEAVES("Осенние листья", "🍂", null),
    FLOWERS("Цветы", "🌸", null),
    FISH("Рыбки", "🐠", 0xFF001A33),
    BUTTERFLIES("Бабочки", "🦋", null),
    WAVES("Волны", "🌊", null),
    WAVES_FOAM("Волны с пеной", "🌊", null);

    companion object {
        fun fromName(n: String): CardAnimation =
            entries.find { it.name == n } ?: NONE
    }
}
