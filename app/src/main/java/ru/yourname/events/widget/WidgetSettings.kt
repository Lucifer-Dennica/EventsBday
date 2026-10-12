package ru.yourname.events.widget

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

private val Context.widgetStore: DataStore<Preferences> by preferencesDataStore("widget_settings")

enum class WidgetBgMode(val label: String) {
    SYSTEM("Как в теме"),
    LIGHT("Светлый"),
    DARK("Тёмный"),
    TRANSPARENT("Прозрачный")
}

object WidgetSettings {

    private val KEY_FONT_SCALE = intPreferencesKey("widget_font_scale")
    private val KEY_OPACITY = intPreferencesKey("widget_opacity")
    private val KEY_PHOTO_SIZE = intPreferencesKey("widget_photo_size")
    private val KEY_SHOW_HEADER = booleanPreferencesKey("widget_show_header")
    private val KEY_SHOW_DATE = booleanPreferencesKey("widget_show_date")
    private val KEY_SHOW_GROUP = booleanPreferencesKey("widget_show_group")
    private val KEY_SHOW_AGE = booleanPreferencesKey("widget_show_age")
    private val KEY_BG_MODE = stringPreferencesKey("widget_bg_mode")
    private val KEY_MINI_FONT_SCALE = intPreferencesKey("widget_mini_font_scale")
    private val KEY_MINI_SHOW_HEADER = booleanPreferencesKey("widget_mini_show_header")

    // Большой виджет
    fun fontScaleFlow(ctx: Context): Flow<Int> =
        ctx.widgetStore.data.map { it[KEY_FONT_SCALE] ?: 100 }
    suspend fun setFontScale(ctx: Context, v: Int) {
        ctx.widgetStore.edit { it[KEY_FONT_SCALE] = v.coerceIn(80, 140) }
    }

    fun opacityFlow(ctx: Context): Flow<Int> =
        ctx.widgetStore.data.map { it[KEY_OPACITY] ?: 100 }
    suspend fun setOpacity(ctx: Context, v: Int) {
        ctx.widgetStore.edit { it[KEY_OPACITY] = v.coerceIn(20, 100) }
    }

    fun photoSizeFlow(ctx: Context): Flow<Int> =
        ctx.widgetStore.data.map { it[KEY_PHOTO_SIZE] ?: 48 }
    suspend fun setPhotoSize(ctx: Context, v: Int) {
        ctx.widgetStore.edit { it[KEY_PHOTO_SIZE] = v.coerceIn(32, 72) }
    }

    fun showHeaderFlow(ctx: Context): Flow<Boolean> =
        ctx.widgetStore.data.map { it[KEY_SHOW_HEADER] ?: true }
    suspend fun setShowHeader(ctx: Context, v: Boolean) {
        ctx.widgetStore.edit { it[KEY_SHOW_HEADER] = v }
    }

    fun showDateFlow(ctx: Context): Flow<Boolean> =
        ctx.widgetStore.data.map { it[KEY_SHOW_DATE] ?: true }
    suspend fun setShowDate(ctx: Context, v: Boolean) {
        ctx.widgetStore.edit { it[KEY_SHOW_DATE] = v }
    }

    fun showGroupFlow(ctx: Context): Flow<Boolean> =
        ctx.widgetStore.data.map { it[KEY_SHOW_GROUP] ?: true }
    suspend fun setShowGroup(ctx: Context, v: Boolean) {
        ctx.widgetStore.edit { it[KEY_SHOW_GROUP] = v }
    }

    fun showAgeFlow(ctx: Context): Flow<Boolean> =
        ctx.widgetStore.data.map { it[KEY_SHOW_AGE] ?: true }
    suspend fun setShowAge(ctx: Context, v: Boolean) {
        ctx.widgetStore.edit { it[KEY_SHOW_AGE] = v }
    }

    fun bgModeFlow(ctx: Context): Flow<WidgetBgMode> = ctx.widgetStore.data.map { prefs ->
        prefs[KEY_BG_MODE]?.let { runCatching { WidgetBgMode.valueOf(it) }.getOrNull() }
            ?: WidgetBgMode.SYSTEM
    }
    suspend fun setBgMode(ctx: Context, mode: WidgetBgMode) {
        ctx.widgetStore.edit { it[KEY_BG_MODE] = mode.name }
    }

    // Мини-календарь
    fun miniFontScaleFlow(ctx: Context): Flow<Int> =
        ctx.widgetStore.data.map { it[KEY_MINI_FONT_SCALE] ?: 100 }
    suspend fun setMiniFontScale(ctx: Context, v: Int) {
        ctx.widgetStore.edit { it[KEY_MINI_FONT_SCALE] = v.coerceIn(80, 140) }
    }

    fun miniShowHeaderFlow(ctx: Context): Flow<Boolean> =
        ctx.widgetStore.data.map { it[KEY_MINI_SHOW_HEADER] ?: true }
    suspend fun setMiniShowHeader(ctx: Context, v: Boolean) {
        ctx.widgetStore.edit { it[KEY_MINI_SHOW_HEADER] = v }
    }
}
