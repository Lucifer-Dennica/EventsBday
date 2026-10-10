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

    // Анимации
    private val KEY_ANIM_ENABLED = booleanPreferencesKey("anim_enabled")
    private val KEY_ANIM_BATTERY_SAVER = booleanPreferencesKey("anim_battery_saver")
    private val KEY_ANIM_RESPECT_SYSTEM = booleanPreferencesKey("anim_respect_system")
    private val KEY_ANIM_MODE = stringPreferencesKey("anim_mode")

    private val KEY_NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
    private val KEY_NOTIF_MODE = stringPreferencesKey("notif_mode")
    private val KEY_NOTIF_FREQ = stringPreferencesKey("notif_frequency")
    private val KEY_NOTIF_DAYS_BEFORE = intPreferencesKey("notif_days_before")
    private val KEY_NOTIF_HOUR = intPreferencesKey("notif_hour")
    private val KEY_NOTIF_MINUTE = intPreferencesKey("notif_minute")

    private val KEY_FIRST_LAUNCH = stringPreferencesKey("first_launch_iso")
    private val KEY_ACHIEVEMENTS_SHOWN = stringPreferencesKey("achievements_shown")

    // === ТЕМА ===
    fun themeFlow(ctx: Context): Flow<AppTheme> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM
    }
    suspend fun setTheme(ctx: Context, theme: AppTheme) {
        ctx.settingsStore.edit { it[KEY_THEME] = theme.name }
    }

    // === ИКОНКА ===
    fun appIconFlow(ctx: Context): Flow<AppIcon> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_APP_ICON]?.let { runCatching { AppIcon.valueOf(it) }.getOrNull() } ?: AppIcon.MAIN
    }
    suspend fun setAppIcon(ctx: Context, icon: AppIcon) {
        ctx.settingsStore.edit { it[KEY_APP_ICON] = icon.name }
    }

    // === АНИМАЦИИ ===
    fun animEnabledFlow(ctx: Context): Flow<Boolean> =
        ctx.settingsStore.data.map { it[KEY_ANIM_ENABLED] ?: true }
    suspend fun setAnimEnabled(ctx: Context, v: Boolean) {
        ctx.settingsStore.edit { it[KEY_ANIM_ENABLED] = v }
    }

    fun animBatterySaverFlow(ctx: Context): Flow<Boolean> =
        ctx.settingsStore.data.map { it[KEY_ANIM_BATTERY_SAVER] ?: false }
    suspend fun setAnimBatterySaver(ctx: Context, v: Boolean) {
        ctx.settingsStore.edit { it[KEY_ANIM_BATTERY_SAVER] = v }
    }

    fun animRespectSystemFlow(ctx: Context): Flow<Boolean> =
        ctx.settingsStore.data.map { it[KEY_ANIM_RESPECT_SYSTEM] ?: true }
    suspend fun setAnimRespectSystem(ctx: Context, v: Boolean) {
        ctx.settingsStore.edit { it[KEY_ANIM_RESPECT_SYSTEM] = v }
    }

    fun animModeFlow(ctx: Context): Flow<AnimationMode> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_ANIM_MODE]?.let { runCatching { AnimationMode.valueOf(it) }.getOrNull() }
            ?: AnimationMode.ALL_VISIBLE
    }
    suspend fun setAnimMode(ctx: Context, mode: AnimationMode) {
        ctx.settingsStore.edit { it[KEY_ANIM_MODE] = mode.name }
    }

    // === ПОВЕДЕНИЕ ===
    fun swipesEnabledFlow(ctx: Context): Flow<Boolean> =
        ctx.settingsStore.data.map { it[KEY_SWIPES] ?: true }
    suspend fun setSwipesEnabled(ctx: Context, enabled: Boolean) {
        ctx.settingsStore.edit { it[KEY_SWIPES] = enabled }
    }

    fun swipeToSettingsEnabledFlow(ctx: Context): Flow<Boolean> =
        ctx.settingsStore.data.map { it[KEY_SWIPE_TO_SETTINGS] ?: true }
    suspend fun setSwipeToSettingsEnabled(ctx: Context, enabled: Boolean) {
        ctx.settingsStore.edit { it[KEY_SWIPE_TO_SETTINGS] = enabled }
    }

    fun swipeLeftActionFlow(ctx: Context): Flow<SwipeAction> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_SWIPE_LEFT]?.let { runCatching { SwipeAction.valueOf(it) }.getOrNull() }
            ?: SwipeAction.DELETE
    }
    suspend fun setSwipeLeftAction(ctx: Context, action: SwipeAction) {
        ctx.settingsStore.edit { it[KEY_SWIPE_LEFT] = action.name }
    }

    fun swipeRightActionFlow(ctx: Context): Flow<SwipeAction> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_SWIPE_RIGHT]?.let { runCatching { SwipeAction.valueOf(it) }.getOrNull() }
            ?: SwipeAction.NOTHING
    }
    suspend fun setSwipeRightAction(ctx: Context, action: SwipeAction) {
        ctx.settingsStore.edit { it[KEY_SWIPE_RIGHT] = action.name }
    }

    // === СОРТИРОВКА ===
    fun sortModeFlow(ctx: Context): Flow<SortMode> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_SORT]?.let { runCatching { SortMode.valueOf(it) }.getOrNull() } ?: SortMode.BY_DATE
    }
    suspend fun setSortMode(ctx: Context, mode: SortMode) {
        ctx.settingsStore.edit { it[KEY_SORT] = mode.name }
    }

    // === ВИД ===
    fun viewModeFlow(ctx: Context): Flow<ViewMode> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_VIEW_MODE]?.let { runCatching { ViewMode.valueOf(it) }.getOrNull() } ?: ViewMode.LIST
    }
    suspend fun setViewMode(ctx: Context, mode: ViewMode) {
        ctx.settingsStore.edit { it[KEY_VIEW_MODE] = mode.name }
    }

    // === ОТМЕТКИ КАЛЕНДАРЯ ===
    fun calendarMarkStyleFlow(ctx: Context): Flow<CalendarMarkStyle> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_CAL_MARK]?.let { runCatching { CalendarMarkStyle.valueOf(it) }.getOrNull() }
            ?: CalendarMarkStyle.FILLED_SQUARE
    }
    suspend fun setCalendarMarkStyle(ctx: Context, style: CalendarMarkStyle) {
        ctx.settingsStore.edit { it[KEY_CAL_MARK] = style.name }
    }

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

    fun eventsCreatedFlow(ctx: Context): Flow<Int> =
        ctx.settingsStore.data.map { it[KEY_EVENTS_CREATED] ?: 0 }

    // === УВЕДОМЛЕНИЯ ===
    fun notifEnabledFlow(ctx: Context): Flow<Boolean> =
        ctx.settingsStore.data.map { it[KEY_NOTIF_ENABLED] ?: true }
    suspend fun setNotifEnabled(ctx: Context, enabled: Boolean) {
        ctx.settingsStore.edit { it[KEY_NOTIF_ENABLED] = enabled }
    }

    fun notifModeFlow(ctx: Context): Flow<NotificationMode> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_NOTIF_MODE]?.let { runCatching { NotificationMode.valueOf(it) }.getOrNull() }
            ?: NotificationMode.ALL
    }
    suspend fun setNotifMode(ctx: Context, mode: NotificationMode) {
        ctx.settingsStore.edit { it[KEY_NOTIF_MODE] = mode.name }
    }

    fun notifFrequencyFlow(ctx: Context): Flow<NotificationFrequency> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_NOTIF_FREQ]?.let { runCatching { NotificationFrequency.valueOf(it) }.getOrNull() }
            ?: NotificationFrequency.EVERY_DAY
    }
    suspend fun setNotifFrequency(ctx: Context, f: NotificationFrequency) {
        ctx.settingsStore.edit { it[KEY_NOTIF_FREQ] = f.name }
    }

    fun notifDaysBeforeFlow(ctx: Context): Flow<Int> =
        ctx.settingsStore.data.map { it[KEY_NOTIF_DAYS_BEFORE] ?: 7 }
    suspend fun setNotifDaysBefore(ctx: Context, days: Int) {
        ctx.settingsStore.edit { it[KEY_NOTIF_DAYS_BEFORE] = days }
    }

    fun notifTimeFlow(ctx: Context): Flow<Pair<Int, Int>> = ctx.settingsStore.data.map { prefs ->
        (prefs[KEY_NOTIF_HOUR] ?: 9) to (prefs[KEY_NOTIF_MINUTE] ?: 0)
    }
    suspend fun setNotifTime(ctx: Context, hour: Int, minute: Int) {
        ctx.settingsStore.edit {
            it[KEY_NOTIF_HOUR] = hour
            it[KEY_NOTIF_MINUTE] = minute
        }
    }

    // === ПЕРВЫЙ ЗАПУСК ===
    fun firstLaunchFlow(ctx: Context): Flow<String?> =
        ctx.settingsStore.data.map { it[KEY_FIRST_LAUNCH] }

    suspend fun ensureFirstLaunch(ctx: Context): String {
        var iso = ""
        ctx.settingsStore.edit { prefs ->
            val existing = prefs[KEY_FIRST_LAUNCH]
            if (existing != null) {
                iso = existing
            } else {
                iso = java.time.LocalDate.now().toString()
                prefs[KEY_FIRST_LAUNCH] = iso
            }
        }
        return iso
    }

    // === ДОСТИЖЕНИЯ ===
    fun achievementsShownFlow(ctx: Context): Flow<Set<String>> = ctx.settingsStore.data.map { prefs ->
        (prefs[KEY_ACHIEVEMENTS_SHOWN] ?: "").split(",").filter { it.isNotBlank() }.toSet()
    }

    suspend fun markAchievementShown(ctx: Context, key: String) {
        ctx.settingsStore.edit { prefs ->
            val current = (prefs[KEY_ACHIEVEMENTS_SHOWN] ?: "")
                .split(",").filter { it.isNotBlank() }.toMutableSet()
            current.add(key)
            prefs[KEY_ACHIEVEMENTS_SHOWN] = current.joinToString(",")
        }
    }
}

enum class AppTheme(val label: String) {
    SYSTEM("Системная"),
    LIGHT("Светлая"),
    DARK("Тёмная"),
    AMOLED("AMOLED (чёрная)"),
    BLUE_NIGHT("Синяя ночь"),
    WARM_SUNSET("Тёплый закат"),
    FOREST("Лес"),
    BERRY("Ягодный милкшейк"),
    MINIMAL("Минимал"),
    HIGH_CONTRAST("Высокий контраст")
}

enum class AnimationMode(val label: String) {
    ALL_VISIBLE("Все видимые карточки"),
    FIRST_ONLY("Только первая видимая"),
    OFF("Отключить анимации")
}
