package ru.yourname.events.data.model

enum class CardAnimation(
    val label: String,
    val emoji: String,
    val backgroundOverride: Long? = null
) {
    NONE("Без анимации", "🚫", null),
    BUBBLES("Пузырьки", "🫧", null),
    FIRE("Свечи", "🕯️", 0xFF1A0A00),
    SNOW("Снег", "❄️", null),
    STARS("Звёзды", "✨", 0xFF0A0F2C),
    LEAVES("Осенние листья", "🍂", null),
    FLOWERS("Цветы", "🌸", null),
    FISH("Рыбки", "🐠", 0xFF001A33),
    BUTTERFLIES("Бабочки", "🦋", null),
    JELLYFISH("Медузы", "🪼", 0xFF001D3D),
    WAVES("Волны", "🌊", null),
    WAVES_FOAM("Волны с пеной", "🌊", null),
    // === НОВЫЕ ===
    CONFETTI("Конфетти", "🎉", null),
    HEARTS("Сердечки", "💗", null),
    FIREWORKS("Салют", "🎆", 0xFF0A0015),
    BALLOONS("Воздушные шары", "🎈", null),
    SPARKS("Искры", "✨", 0xFF1A0E00),
    STREAMERS("Серпантин", "🎊", null),
    CRYSTALS("Кристаллы", "💎", 0xFF0A1428),
    COINS("Монеты", "💰", null);

    companion object {
        fun fromName(n: String): CardAnimation =
            entries.find { it.name == n } ?: NONE
    }
}
