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

    // Тема
    private val KEY_THEME = stringPreferencesKey("theme")
    // Поведение
    private val KEY_SWIPES = booleanPreferencesKey("swipes_enabled")
    private val KEY_CARD_OPENS = intPreferencesKey("card_opens_count")

    // Уведомления
    private val KEY_NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
    private val KEY_NOTIF_MODE = stringPreferencesKey("notif_mode")
    private val KEY_NOTIF_FREQ = stringPreferencesKey("notif_frequency")
    private val KEY_NOTIF_DAYS_BEFORE = intPreferencesKey("notif_days_before")
    private val KEY_NOTIF_HOUR = intPreferencesKey("notif_hour")
    private val KEY_NOTIF_MINUTE = intPreferencesKey("notif_minute")
    private val KEY_FIRST_LAUNCH = stringPreferencesKey("first_launch_iso")

    // ============ ТЕМА ============

    fun themeFlow(ctx: Context): Flow<AppTheme> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM
    }

    suspend fun setTheme(ctx: Context, theme: AppTheme) {
        ctx.settingsStore.edit { it[KEY_THEME] = theme.name }
    }

    // ============ ПОВЕДЕНИЕ ============

    fun swipesEnabledFlow(ctx: Context): Flow<Boolean> =
        ctx.settingsStore.data.map { it[KEY_SWIPES] ?: true }

    suspend fun setSwipesEnabled(ctx: Context, enabled: Boolean) {
        ctx.settingsStore.edit { it[KEY_SWIPES] = enabled }
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

    // ============ УВЕДОМЛЕНИЯ ============

    fun notifEnabledFlow(ctx: Context): Flow<Boolean> =
        ctx.settingsStore.data.map { it[KEY_NOTIF_ENABLED] ?: true }

    suspend fun setNotifEnabled(ctx: Context, enabled: Boolean) {
        ctx.settingsStore.edit { it[KEY_NOTIF_ENABLED] = enabled }
    }

    fun notifModeFlow(ctx: Context): Flow<NotificationMode> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_NOTIF_MODE]?.let {
            runCatching { NotificationMode.valueOf(it) }.getOrNull()
        } ?: NotificationMode.ALL
    }

    suspend fun setNotifMode(ctx: Context, mode: NotificationMode) {
        ctx.settingsStore.edit { it[KEY_NOTIF_MODE] = mode.name }
    }

    fun notifFrequencyFlow(ctx: Context): Flow<NotificationFrequency> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_NOTIF_FREQ]?.let {
            runCatching { NotificationFrequency.valueOf(it) }.getOrNull()
        } ?: NotificationFrequency.EVERY_DAY
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

    // Первый запуск — для достижения "космонавт"
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
}

enum class AppTheme(val label: String) {
    SYSTEM("Системная"),
    LIGHT("Светлая"),
    DARK("Тёмная"),
    AMOLED("AMOLED (чёрная)")
}
