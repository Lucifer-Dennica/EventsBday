package ru.yourname.events.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

object SettingsRepository {

    private val KEY_THEME = stringPreferencesKey("theme")
    private val KEY_SWIPES = booleanPreferencesKey("swipes_enabled")

    fun themeFlow(ctx: Context): Flow<AppTheme> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM
    }

    suspend fun setTheme(ctx: Context, theme: AppTheme) {
        ctx.settingsStore.edit { it[KEY_THEME] = theme.name }
    }

    fun swipesEnabledFlow(ctx: Context): Flow<Boolean> = ctx.settingsStore.data.map { prefs ->
        prefs[KEY_SWIPES] ?: true
    }

    suspend fun setSwipesEnabled(ctx: Context, enabled: Boolean) {
        ctx.settingsStore.edit { it[KEY_SWIPES] = enabled }
    }
}

enum class AppTheme(val label: String) {
    SYSTEM("Системная"),
    LIGHT("Светлая"),
    DARK("Тёмная"),
    AMOLED("AMOLED (чёрная)")
}
