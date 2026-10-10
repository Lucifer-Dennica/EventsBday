package ru.yourname.events.data.settings

enum class SortMode(val label: String) {
    BY_DATE("По дате (ближайшие вверху)"),
    BY_NAME("По имени (А-Я)"),
    BY_GROUP("По группе")
}

enum class ViewMode {
    LIST, CALENDAR
}

enum class CalendarMarkStyle(val label: String) {
    FILLED_SQUARE("Заливка квадратом"),
    FILLED_CIRCLE("Заливка кругом"),
    OUTLINE("Обводка цветом"),
    NONE("Без отметок")
}
