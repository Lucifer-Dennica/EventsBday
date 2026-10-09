package ru.yourname.events.data.model

import android.content.Context

enum class SocialPlatform(
    val key: String,
    val label: String,
    val emoji: String,
    val urlPattern: String,
    val hint: String
) {
    WHATSAPP("whatsapp", "WhatsApp", "🟢", "https://wa.me/%s", "+7 999 123-45-67"),
    TELEGRAM("telegram", "Telegram", "✈️", "https://t.me/%s", "@username"),
    VK("vk", "ВКонтакте", "🔵", "https://vk.com/%s", "username"),
    OK("ok", "Одноклассники", "🟠", "https://ok.ru/%s", "username"),
    VIBER("viber", "Viber", "🟣", "viber://chat?number=%s", "+79991234567"),
    MAX("max", "MAX", "⚫", "https://max.ru/%s", "username"),
    INSTAGRAM("instagram", "Instagram", "🟣", "https://instagram.com/%s", "username"),
    PHONE("phone", "Телефон", "📞", "tel:%s", "+7 999 123-45-67"),
    EMAIL("email", "Email", "✉️", "mailto:%s", "user@mail.com"),
    CUSTOM("custom", "Другое", "🔗", "%s", "https://...");

    /** Возвращает resId иконки из drawable/ic_social_<key>.png или 0, если её нет. */
    fun iconResId(ctx: Context): Int =
        ctx.resources.getIdentifier("ic_social_$key", "drawable", ctx.packageName)

    companion object {
        fun fromKey(k: String): SocialPlatform = entries.find { it.key == k } ?: CUSTOM
    }
}
