package ru.yourname.events.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

object SettingsRepository {

    private val KEY_THEME = stringPreferencesKey("theme")
    private val KEY_SWIPES = booleanPreferencesKey("swipes_enabled")
    private val KEY_SWIPE_TO_SETTINGS = booleanPreferencesKey("swipe_to_settings_enabled")
    private val KEY_CARD_OPENS = intPreferencesKey("card_opens_count")
    private val KEY_EVENTS_CREATED = intPreferencesKey("events_created_count")

    private val KEY_SWIPE_LEFT = stringPreferencesKey("swipe_left_action")
    private val KEY_SWIPE_RIGHT = stringPreferencesKey("swipe_right_action")
    private val KEY_SORT = stringPreferencesKey("sort_mode")
    private val KEY_VIEW_MODE = stringPreferencesKey("view_mode")
    private val KEY_CAL_MARK = stringPreferencesKey("calendar_mark_style")
    private val KEY_APP_ICON = stringPreferencesKey("app_icon")

    private val KEY_AVATAR_GLOW = booleanPreferencesKey("avatar_glow")
    private val KEY_AVATAR_SIZE = intPreferencesKey("avatar_size")
    private val KEY_AVATAR_SHAPE = stringPreferencesKey("avatar_shape")

    private val KEY_CARD_BORDER_WIDTH = intPreferencesKey("card_border_width")
    private val KEY_CARD_BORDER_COLOR = stringPreferencesKey("card_border_color")
    private val KEY_CARD_BORDER_GLOW = booleanPreferencesKey("card_border_glow") // legacy
    private val KEY_NEON_BORDER = booleanPreferencesKey("neon_border")
    private val KEY_NEON_BORDER_FULL = booleanPreferencesKey("neon_border_full")
    private val KEY_NEON_BORDER_ANIMATED = booleanPreferencesKey("neon_border_animated") // НОВОЕ
    private val KEY_CARD_GLOW_STYLE = stringPreferencesKey("card_glow_style")           // НОВОЕ
    private val KEY_CARD_GLOW_INTENSITY = intPreferencesKey("card_glow_intensity")      // НОВОЕ

    private val KEY_CARD_CORNER_RADIUS = intPreferencesKey("card_corner_radius")
    private val KEY_LIST_DENSITY = stringPreferencesKey("list_density")
    private val KEY_SHOW_ZODIAC = booleanPreferencesKey("show_zodiac")
    private val KEY_TITLE_FONT_SIZE = stringPreferencesKey("title_font_size")
    private val KEY_DAYS_FORMAT = stringPreferencesKey("days_format")
    private val KEY_SHOW_DAY_OF_WEEK = booleanPreferencesKey("show_day_of_week")

    private val KEY_ANIM_ENABLED = booleanPreferencesKey("anim_enabled")
    private val KEY_ANIM_BATTERY_SAVER = booleanPreferencesKey("anim_battery_saver")
    private val KEY_ANIM_RESPECT_SYSTEM = booleanPreferencesKey("anim_respect_system")
    private val KEY_ANIM_MODE = stringPreferencesKey("anim_mode")

    private val KEY_RECENT_COLORS = stringPreferencesKey("recent_colors")

    private val KEY_NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
    private val KEY_NOTIF_MODE = stringPreferencesKey("notif_mode")
    private val KEY_NOTIF_FREQ = stringPreferencesKey("notif_frequency")
    private val KEY_NOTIF_DAYS_BEFORE = intPreferencesKey("notif_days_before")
    private val KEY_NOTIF_HOUR = intPreferencesKey("notif_hour")
    private val KEY_NOTIF_MINUTE = intPreferencesKey("notif_minute")

    private val KEY_FIRST_LAUNCH = stringPreferencesKey("first_launch_iso")
    private val KEY_ACHIEVEMENTS_SHOWN = stringPreferencesKey("achievements_shown")

    fun themeFlow(ctx: Context): Flow<AppTheme> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM
    }
    suspend fun setTheme(ctx: Context, theme: AppTheme) { ctx.settingsStore.edit { it[KEY_THEME] = theme.name } }

    fun appIconFlow(ctx: Context): Flow<AppIcon> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_APP_ICON]?.let { runCatching { AppIcon.valueOf(it) }.getOrNull() } ?: AppIcon.MAIN
    }
    suspend fun setAppIcon(ctx: Context, icon: AppIcon) { ctx.settingsStore.edit { it[KEY_APP_ICON] = icon.name } }

    fun avatarGlowFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { it[KEY_AVATAR_GLOW] ?: true }
    suspend fun setAvatarGlow(ctx: Context, v: Boolean) { ctx.settingsStore.edit { it[KEY_AVATAR_GLOW] = v } }

    fun avatarSizeFlow(ctx: Context): Flow<Int> = ctx.settingsStore.data.map { it[KEY_AVATAR_SIZE] ?: 54 }
    suspend fun setAvatarSize(ctx: Context, v: Int) { ctx.settingsStore.edit { it[KEY_AVATAR_SIZE] = v.coerceIn(44, 80) } }

    fun avatarShapeFlow(ctx: Context): Flow<AvatarShape> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_AVATAR_SHAPE]?.let { runCatching { AvatarShape.valueOf(it) }.getOrNull() } ?: AvatarShape.CIRCLE
    }
    suspend fun setAvatarShape(ctx: Context, shape: AvatarShape) { ctx.settingsStore.edit { it[KEY_AVATAR_SHAPE] = shape.name } }

    fun cardBorderWidthFlow(ctx: Context): Flow<Int> = ctx.settingsStore.data.map { it[KEY_CARD_BORDER_WIDTH] ?: 2 }
    suspend fun setCardBorderWidth(ctx: Context, v: Int) { ctx.settingsStore.edit { it[KEY_CARD_BORDER_WIDTH] = v.coerceIn(0, 4) } }

    fun cardBorderColorFlow(ctx: Context): Flow<CardBorderColor> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_CARD_BORDER_COLOR]?.let { runCatching { CardBorderColor.valueOf(it) }.getOrNull() } ?: CardBorderColor.AUTO
    }
    suspend fun setCardBorderColor(ctx: Context, color: CardBorderColor) { ctx.settingsStore.edit { it[KEY_CARD_BORDER_COLOR] = color.name } }

    // Устаревший флаг, оставлен для совместимости. Используй cardGlowStyleFlow.
    fun cardBorderGlowFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { it[KEY_CARD_BORDER_GLOW] ?: false }
    suspend fun setCardBorderGlow(ctx: Context, v: Boolean) { ctx.settingsStore.edit { it[KEY_CARD_BORDER_GLOW] = v } }

    fun neonBorderFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { it[KEY_NEON_BORDER] ?: false }
    suspend fun setNeonBorder(ctx: Context, v: Boolean) { ctx.settingsStore.edit { it[KEY_NEON_BORDER] = v } }

    fun neonBorderFullFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { it[KEY_NEON_BORDER_FULL] ?: false }
    suspend fun setNeonBorderFull(ctx: Context, v: Boolean) { ctx.settingsStore.edit { it[KEY_NEON_BORDER_FULL] = v } }

    fun neonBorderAnimatedFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { it[KEY_NEON_BORDER_ANIMATED] ?: true }
    suspend fun setNeonBorderAnimated(ctx: Context, v: Boolean) { ctx.settingsStore.edit { it[KEY_NEON_BORDER_ANIMATED] = v } }

    // Вспомогательный метод — единый источник истины о режиме неона
    fun neonModeFlow(ctx: Context): Flow<NeonMode> = ctx.settingsStore.data.map { prefs ->
        val active = prefs[KEY_NEON_BORDER] ?: false
        if (!active) return@map NeonMode.OFF
        val full = prefs[KEY_NEON_BORDER_FULL] ?: false
        if (!full) return@map NeonMode.RUNNING
        val animated = prefs[KEY_NEON_BORDER_ANIMATED] ?: true
        if (animated) NeonMode.FULL_ANIMATED else NeonMode.FULL_STATIC
    }
    suspend fun setNeonMode(ctx: Context, mode: NeonMode) {
        ctx.settingsStore.edit { prefs ->
            when (mode) {
                NeonMode.OFF -> {
                    prefs[KEY_NEON_BORDER] = false
                    prefs[KEY_NEON_BORDER_FULL] = false
                }
                NeonMode.RUNNING -> {
                    prefs[KEY_NEON_BORDER] = true
                    prefs[KEY_NEON_BORDER_FULL] = false
                }
                NeonMode.FULL_STATIC -> {
                    prefs[KEY_NEON_BORDER] = true
                    prefs[KEY_NEON_BORDER_FULL] = true
                    prefs[KEY_NEON_BORDER_ANIMATED] = false
                }
                NeonMode.FULL_ANIMATED -> {
                    prefs[KEY_NEON_BORDER] = true
                    prefs[KEY_NEON_BORDER_FULL] = true
                    prefs[KEY_NEON_BORDER_ANIMATED] = true
                }
            }
        }
    }

    fun cardGlowStyleFlow(ctx: Context): Flow<CardGlowStyle> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_CARD_GLOW_STYLE]?.let { runCatching { CardGlowStyle.valueOf(it) }.getOrNull() }
            ?: (if (prefs[KEY_CARD_BORDER_GLOW] == true) CardGlowStyle.GLOW else CardGlowStyle.NONE)
    }
    suspend fun setCardGlowStyle(ctx: Context, style: CardGlowStyle) {
        ctx.settingsStore.edit { it[KEY_CARD_GLOW_STYLE] = style.name }
    }

    fun cardGlowIntensityFlow(ctx: Context): Flow<Int> = ctx.settingsStore.data.map { it[KEY_CARD_GLOW_INTENSITY] ?: 60 }
    suspend fun setCardGlowIntensity(ctx: Context, v: Int) {
        ctx.settingsStore.edit { it[KEY_CARD_GLOW_INTENSITY] = v.coerceIn(0, 100) }
    }

    fun cardCornerRadiusFlow(ctx: Context): Flow<Int> = ctx.settingsStore.data.map { it[KEY_CARD_CORNER_RADIUS] ?: 18 }
    suspend fun setCardCornerRadius(ctx: Context, v: Int) { ctx.settingsStore.edit { it[KEY_CARD_CORNER_RADIUS] = v.coerceIn(0, 24) } }

    fun listDensityFlow(ctx: Context): Flow<ListDensity> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_LIST_DENSITY]?.let { runCatching { ListDensity.valueOf(it) }.getOrNull() } ?: ListDensity.NORMAL
    }
    suspend fun setListDensity(ctx: Context, v: ListDensity) { ctx.settingsStore.edit { it[KEY_LIST_DENSITY] = v.name } }

    fun showZodiacFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { it[KEY_SHOW_ZODIAC] ?: true }
    suspend fun setShowZodiac(ctx: Context, v: Boolean) { ctx.settingsStore.edit { it[KEY_SHOW_ZODIAC] = v } }

    fun titleFontSizeFlow(ctx: Context): Flow<TitleFontSize> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_TITLE_FONT_SIZE]?.let { runCatching { TitleFontSize.valueOf(it) }.getOrNull() } ?: TitleFontSize.MEDIUM
    }
    suspend fun setTitleFontSize(ctx: Context, v: TitleFontSize) { ctx.settingsStore.edit { it[KEY_TITLE_FONT_SIZE] = v.name } }

    fun daysFormatFlow(ctx: Context): Flow<DaysFormat> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_DAYS_FORMAT]?.let { runCatching { DaysFormat.valueOf(it) }.getOrNull() } ?: DaysFormat.SHORT
    }
    suspend fun setDaysFormat(ctx: Context, v: DaysFormat) { ctx.settingsStore.edit { it[KEY_DAYS_FORMAT] = v.name } }

    fun showDayOfWeekFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { it[KEY_SHOW_DAY_OF_WEEK] ?: false }
    suspend fun setShowDayOfWeek(ctx: Context, v: Boolean) { ctx.settingsStore.edit { it[KEY_SHOW_DAY_OF_WEEK] = v } }

    fun animEnabledFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { it[KEY_ANIM_ENABLED] ?: true }
    suspend fun setAnimEnabled(ctx: Context, v: Boolean) { ctx.settingsStore.edit { it[KEY_ANIM_ENABLED] = v } }
    fun animBatterySaverFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { it[KEY_ANIM_BATTERY_SAVER] ?: false }
    suspend fun setAnimBatterySaver(ctx: Context, v: Boolean) { ctx.settingsStore.edit { it[KEY_ANIM_BATTERY_SAVER] = v } }
    fun animRespectSystemFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { it[KEY_ANIM_RESPECT_SYSTEM] ?: true }
    suspend fun setAnimRespectSystem(ctx: Context, v: Boolean) { ctx.settingsStore.edit { it[KEY_ANIM_RESPECT_SYSTEM] = v } }
    fun animModeFlow(ctx: Context): Flow<AnimationMode> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_ANIM_MODE]?.let { runCatching { AnimationMode.valueOf(it) }.getOrNull() } ?: AnimationMode.ALL_VISIBLE
    }
    suspend fun setAnimMode(ctx: Context, mode: AnimationMode) { ctx.settingsStore.edit { it[KEY_ANIM_MODE] = mode.name } }

    fun recentColorsFlow(ctx: Context): Flow<List<String>> = ctx.settingsStore.data.map { prefs ->
        (prefs[KEY_RECENT_COLORS] ?: "").split("|").filter { it.isNotBlank() }
    }
    suspend fun addRecentColor(ctx: Context, hex: String) {
        ctx.settingsStore.edit { prefs ->
            val current = (prefs[KEY_RECENT_COLORS] ?: "").split("|").filter { it.isNotBlank() }.toMutableList()
            current.remove(hex); current.add(0, hex)
            while (current.size > 8) current.removeAt(current.size - 1)
            prefs[KEY_RECENT_COLORS] = current.joinToString("|")
        }
    }

    fun swipesEnabledFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { it[KEY_SWIPES] ?: true }
    suspend fun setSwipesEnabled(ctx: Context, enabled: Boolean) { ctx.settingsStore.edit { it[KEY_SWIPES] = enabled } }
    fun swipeToSettingsEnabledFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { it[KEY_SWIPE_TO_SETTINGS] ?: true }
    suspend fun setSwipeToSettingsEnabled(ctx: Context, enabled: Boolean) { ctx.settingsStore.edit { it[KEY_SWIPE_TO_SETTINGS] = enabled } }
    fun swipeLeftActionFlow(ctx: Context): Flow<SwipeAction> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_SWIPE_LEFT]?.let { runCatching { SwipeAction.valueOf(it) }.getOrNull() } ?: SwipeAction.DELETE
    }
    suspend fun setSwipeLeftAction(ctx: Context, action: SwipeAction) { ctx.settingsStore.edit { it[KEY_SWIPE_LEFT] = action.name } }
    fun swipeRightActionFlow(ctx: Context): Flow<SwipeAction> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_SWIPE_RIGHT]?.let { runCatching { SwipeAction.valueOf(it) }.getOrNull() } ?: SwipeAction.NOTHING
    }
    suspend fun setSwipeRightAction(ctx: Context, action: SwipeAction) { ctx.settingsStore.edit { it[KEY_SWIPE_RIGHT] = action.name } }

    fun sortModeFlow(ctx: Context): Flow<SortMode> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_SORT]?.let { runCatching { SortMode.valueOf(it) }.getOrNull() } ?: SortMode.BY_DATE
    }
    suspend fun setSortMode(ctx: Context, mode: SortMode) { ctx.settingsStore.edit { it[KEY_SORT] = mode.name } }

    fun viewModeFlow(ctx: Context): Flow<ViewMode> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_VIEW_MODE]?.let { runCatching { ViewMode.valueOf(it) }.getOrNull() } ?: ViewMode.LIST
    }
    suspend fun setViewMode(ctx: Context, mode: ViewMode) { ctx.settingsStore.edit { it[KEY_VIEW_MODE] = mode.name } }

    fun calendarMarkStyleFlow(ctx: Context): Flow<CalendarMarkStyle> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_CAL_MARK]?.let { runCatching { CalendarMarkStyle.valueOf(it) }.getOrNull() } ?: CalendarMarkStyle.FILLED_SQUARE
    }
    suspend fun setCalendarMarkStyle(ctx: Context, style: CalendarMarkStyle) { ctx.settingsStore.edit { it[KEY_CAL_MARK] = style.name } }

    suspend fun incrementCardOpens(ctx: Context): Int {
        var newValue = 0
        ctx.settingsStore.edit { prefs ->
            val current = prefs[KEY_CARD_OPENS] ?: 0
            newValue = current + 1
            prefs[KEY_CARD_OPENS] = newValue
        }
        return newValue
    }
    suspend fun incrementEventsCreated(ctx: Context): Int {
        var newValue = 0
        ctx.settingsStore.edit { prefs ->
            val current = prefs[KEY_EVENTS_CREATED] ?: 0
            newValue = current + 1
            prefs[KEY_EVENTS_CREATED] = newValue
        }
        return newValue
    }
    fun eventsCreatedFlow(ctx: Context): Flow<Int> = ctx.settingsStore.data.map { it[KEY_EVENTS_CREATED] ?: 0 }

    fun notifEnabledFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { it[KEY_NOTIF_ENABLED] ?: true }
    suspend fun setNotifEnabled(ctx: Context, enabled: Boolean) { ctx.settingsStore.edit { it[KEY_NOTIF_ENABLED] = enabled } }
    fun notifModeFlow(ctx: Context): Flow<NotificationMode> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_NOTIF_MODE]?.let { runCatching { NotificationMode.valueOf(it) }.getOrNull() } ?: NotificationMode.ALL
    }
    suspend fun setNotifMode(ctx: Context, mode: NotificationMode) { ctx.settingsStore.edit { it[KEY_NOTIF_MODE] = mode.name } }
    fun notifFrequencyFlow(ctx: Context): Flow<NotificationFrequency> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_NOTIF_FREQ]?.let { runCatching { NotificationFrequency.valueOf(it) }.getOrNull() } ?: NotificationFrequency.EVERY_DAY
    }
    suspend fun setNotifFrequency(ctx: Context, f: NotificationFrequency) { ctx.settingsStore.edit { it[KEY_NOTIF_FREQ] = f.name } }
    fun notifDaysBeforeFlow(ctx: Context): Flow<Int> = ctx.settingsStore.data.map { it[KEY_NOTIF_DAYS_BEFORE] ?: 7 }
    suspend fun setNotifDaysBefore(ctx: Context, days: Int) { ctx.settingsStore.edit { it[KEY_NOTIF_DAYS_BEFORE] = days } }
    fun notifTimeFlow(ctx: Context): Flow<Pair<Int, Int>> = ctx.settingsStore.data.map { prefs ->
        (prefs[KEY_NOTIF_HOUR] ?: 9) to (prefs[KEY_NOTIF_MINUTE] ?: 0)
    }
    suspend fun setNotifTime(ctx: Context, hour: Int, minute: Int) {
        ctx.settingsStore.edit { it[KEY_NOTIF_HOUR] = hour; it[KEY_NOTIF_MINUTE] = minute }
    }

    fun firstLaunchFlow(ctx: Context): Flow<String?> = ctx.settingsStore.data.map { it[KEY_FIRST_LAUNCH] }
    suspend fun ensureFirstLaunch(ctx: Context): String {
        var iso = ""
        ctx.settingsStore.edit { prefs ->
            val existing = prefs[KEY_FIRST_LAUNCH]
            if (existing != null) iso = existing else {
                iso = java.time.LocalDate.now().toString()
                prefs[KEY_FIRST_LAUNCH] = iso
            }
        }
        return iso
    }

    fun achievementsShownFlow(ctx: Context): Flow<Set<String>> = ctx.settingsStore.data.map { prefs ->
        (prefs[KEY_ACHIEVEMENTS_SHOWN] ?: "").split(",").filter { it.isNotBlank() }.toSet()
    }
    suspend fun markAchievementShown(ctx: Context, key: String) {
        ctx.settingsStore.edit { prefs ->
            val current = (prefs[KEY_ACHIEVEMENTS_SHOWN] ?: "").split(",").filter { it.isNotBlank() }.toMutableSet()
            current.add(key)
            prefs[KEY_ACHIEVEMENTS_SHOWN] = current.joinToString(",")
        }
    }
}

enum class AppTheme(val label: String) {
    SYSTEM("Системная"), LIGHT("Светлая"), DARK("Тёмная"), AMOLED("AMOLED (чёрная)"),
    BLUE_NIGHT("Синяя ночь"), WARM_SUNSET("Тёплый закат"), FOREST("Лес"),
    BERRY("Ягодный милкшейк"), MINIMAL("Минимал"), HIGH_CONTRAST("Высокий контраст")
}

enum class AnimationMode(val label: String) {
    ALL_VISIBLE("Все видимые карточки"), FIRST_ONLY("Только первая видимая"), OFF("Отключить анимации")
}

enum class CardBorderColor(val label: String, val hex: Long?) {
    AUTO("Автоматически", null), ACCENT("В цвет акцента", null),
    WHITE("Белая", 0xFFFFFFFF), BLACK("Чёрная", 0xFF000000),
    GRAY("Серая", 0xFF9E9E9E), RED("Красная", 0xFFE53935),
    ORANGE("Оранжевая", 0xFFFB8C00), YELLOW("Жёлтая", 0xFFFDD835),
    GREEN("Зелёная", 0xFF43A047), TEAL("Бирюзовая", 0xFF00ACC1),
    BLUE("Синяя", 0xFF1E88E5), INDIGO("Индиго", 0xFF3949AB),
    PURPLE("Фиолетовая", 0xFF8E24AA), PINK("Розовая", 0xFFD81B60),
    BROWN("Коричневая", 0xFF6D4C41), GOLD("Золотая", 0xFFD4AF37)
}

enum class ListDensity(val label: String, val verticalSpacingDp: Int, val horizontalPaddingDp: Int, val cardPaddingDp: Int) {
    COMPACT("Компактная", 6, 10, 10),
    NORMAL("Обычная", 10, 16, 14),
    SPACIOUS("Просторная", 16, 20, 18)
}

enum class AvatarShape(val label: String) {
    CIRCLE("Круг"), SQUARE("Квадрат"), ROUNDED_SQUARE("Скруглённый квадрат")
}

enum class TitleFontSize(val label: String, val sp: Int) {
    SMALL("S", 16), MEDIUM("M", 18), LARGE("L", 21)
}

enum class DaysFormat(val label: String) {
    SHORT("5 дн."), FULL("5 дней"), PREFIX("через 5")
}

/** Режим неоновой обводки карточек. */
enum class NeonMode(val label: String) {
    OFF("Выключена"),
    RUNNING("Бегущая (сегмент)"),
    FULL_STATIC("Полная (статичная)"),
    FULL_ANIMATED("Полная (анимированная)")
}

/** Стиль свечения/тени вокруг карточки. */
enum class CardGlowStyle(val label: String) {
    NONE("Без свечения"),
    GLOW("Свечение"),
    SHADOW("Тень"),
    GLOW_SHADOW("Свечение + тень")
}
