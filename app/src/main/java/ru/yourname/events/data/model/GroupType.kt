package ru.yourname.events.data.model

enum class GroupType(val label: String, val emoji: String) {
    FAMILY("Семья", "👨‍👩‍👧"),
    FRIENDS("Друзья", "🤝"),
    RELATIVES("Родственники", "🎊"),
    ACQUAINTANCES("Знакомые", "💫"),
    WORK("Работа", "💼"),
    OTHER("Прочее", "🔮")
}
