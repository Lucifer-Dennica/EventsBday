package ru.yourname.events.data.model

enum class CardAnimation(val label: String, val emoji: String) {
    NONE("Без анимации", "🚫"),
    BUBBLES("Пузырьки", "🫧"),
    FIRE("Огонь", "🔥"),
    SNOW("Снег", "❄️"),
    STARS("Звёзды", "✨"),
    PETALS("Лепестки", "🌸"),
    WAVES("Волны", "🌊");

    companion object {
        fun fromName(n: String): CardAnimation =
            entries.find { it.name == n } ?: NONE
    }
}
