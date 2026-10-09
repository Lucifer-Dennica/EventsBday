package ru.yourname.events.data.social

import ru.yourname.events.data.model.SocialLink
import ru.yourname.events.data.model.SocialPlatform

object SocialUrlParser {

    /** Возвращает "человеческое" отображение: @ник, +7 999... или домен. */
    fun displayName(link: SocialLink): String {
        val v = link.value.trim()
        if (v.isEmpty()) return ""
        return when (link.platform) {
            SocialPlatform.WHATSAPP, SocialPlatform.PHONE, SocialPlatform.VIBER ->
                v.removePrefix("tel:").let { if (it.startsWith("+")) it else "+$it" }
            SocialPlatform.EMAIL -> v.removePrefix("mailto:")
            SocialPlatform.CUSTOM -> v.removePrefix("https://").removePrefix("http://").substringBefore("/")
            else -> {
                val handle = extractHandle(v)
                if (handle.isNotEmpty()) "@$handle" else v
            }
        }
    }

    /** Возвращает кликабельный URL. */
    fun buildUrl(link: SocialLink): String {
        val v = link.value.trim().removePrefix("@")
        val p = link.platform
        // Если пользователь вставил полный URL — используем как есть
        if (v.startsWith("http://") || v.startsWith("https://") ||
            v.startsWith("tel:") || v.startsWith("mailto:") || v.startsWith("viber://")
        ) return v

        return when (p) {
            SocialPlatform.WHATSAPP -> "https://wa.me/${v.filter { it.isDigit() }}"
            SocialPlatform.VIBER -> "viber://chat?number=${v.filter { it.isDigit() || it == '+' }}"
            SocialPlatform.PHONE -> "tel:${v.filter { it.isDigit() || it == '+' }}"
            SocialPlatform.EMAIL -> "mailto:$v"
            else -> p.urlPattern.format(v)
        }
    }

    private fun extractHandle(url: String): String {
        val cleaned = url.removePrefix("https://").removePrefix("http://").removePrefix("www.")
        val parts = cleaned.split("/")
        return when {
            parts.size >= 2 && parts[0].contains("t.me") -> parts[1]
            parts.size >= 2 -> parts[1].substringBefore("?").substringBefore("#")
            else -> ""
        }
    }
}
