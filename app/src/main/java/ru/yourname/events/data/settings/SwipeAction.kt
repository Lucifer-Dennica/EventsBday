package ru.yourname.events.data.settings

enum class SwipeAction(val label: String) {
    NOTHING("Ничего"),
    DELETE("Удалить"),
    PIN("Закрепить / открепить"),
    NOTIFICATIONS("Уведомления вкл / выкл"),
    EDIT("Редактировать"),
    CALL("Позвонить")
}
