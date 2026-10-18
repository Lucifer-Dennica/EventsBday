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
    LIGHT("Светлый"),
    DARK("Тёмный"),
    TRANSPARENT("Прозрачный")
}

enum class WidgetTextColor(val label: String) {
    AUTO("Автоматически"),
    WHITE("Белый"),
    BLACK("Чёрный"),
    BLUE("Синий"),
    PINK("Розовый"),
    GREEN("Зелёный"),
    PURPLE("Фиолетовый"),
    ORANGE("Оранжевый")
}

object WidgetSettings {

    private val KEY_FONT_SCALE = intPreferencesKey("widget_font_scale")
    private val KEY_OPACITY = intPreferencesKey("widget_opacity")
    private val KEY_PHOTO_SIZE = intPreferencesKey("widget_photo_size")
    private val KEY_SHOW_HEADER = booleanPreferencesKey("widget_show_header")
    private val KEY_SHOW_DATE = booleanPreferencesKey("widget_show_date")
    private val KEY_BG_MODE = stringPreferencesKey("widget_bg_mode")
    private val KEY_TEXT_COLOR = stringPreferencesKey("widget_text_color")
    private val KEY_MINI_FONT_SCALE = intPreferencesKey("widget_mini_font_scale")
    private val KEY_MINI_SHOW_HEADER = booleanPreferencesKey("widget_mini_show_header")
    private val KEY_MINI_DAYS_ONLY = booleanPreferencesKey("widget_mini_days_only")

    // Большой виджет
    fun fontScaleFlow(ctx: Context): Flow<Int> =
        ctx.widgetStore.data.map { it[KEY_FONT_SCALE] ?: 100 }
    suspend fun setFontScale(ctx: Context, v: Int) {
        ctx.widgetStore.edit { it[KEY_FONT_SCALE] = v.coerceIn(80, 140) }
    }

    fun opacityFlow(ctx: Context): Flow<Int> =
        ctx.widgetStore.data.map { it[KEY_OPACITY] ?: 100 }
    suspend fun setOpacity(ctx: Context, v: Int) {
        ctx.widgetStore.edit { it[KEY_OPACITY] = v.coerceIn(40, 100) }
    }

    fun photoSizeFlow(ctx: Context): Flow<Int> =
        ctx.widgetStore.data.map { it[KEY_PHOTO_SIZE] ?: 44 }
    suspend fun setPhotoSize(ctx: Context, v: Int) {
        ctx.widgetStore.edit { it[KEY_PHOTO_SIZE] = v.coerceIn(32, 64) }
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

    fun bgModeFlow(ctx: Context): Flow<WidgetBgMode> = ctx.widgetStore.data.map { prefs ->
        prefs[KEY_BG_MODE]?.let { runCatching { WidgetBgMode.valueOf(it) }.getOrNull() }
            ?: WidgetBgMode.LIGHT
    }
    suspend fun setBgMode(ctx: Context, mode: WidgetBgMode) {
        ctx.widgetStore.edit { it[KEY_BG_MODE] = mode.name }
    }

    fun textColorFlow(ctx: Context): Flow<WidgetTextColor> = ctx.widgetStore.data.map { prefs ->
        prefs[KEY_TEXT_COLOR]?.let { runCatching { WidgetTextColor.valueOf(it) }.getOrNull() }
            ?: WidgetTextColor.AUTO
    }
    suspend fun setTextColor(ctx: Context, color: WidgetTextColor) {
        ctx.widgetStore.edit { it[KEY_TEXT_COLOR] = color.name }
    }

    // Мини-виджет
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

    fun miniDaysOnlyFlow(ctx: Context): Flow<Boolean> =
        ctx.widgetStore.data.map { it[KEY_MINI_DAYS_ONLY] ?: true }
    suspend fun setMiniDaysOnly(ctx: Context, v: Boolean) {
        ctx.widgetStore.edit { it[KEY_MINI_DAYS_ONLY] = v }
    }
}
