package ru.yourname.events.data.achievements

import android.content.Context
import kotlinx.coroutines.flow.first
import ru.yourname.events.data.settings.SettingsRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Пасхальные аватары, разблокируемые по достижениям. */
object Achievements {

    const val ASTRONAUT_KEY = "astronaut"
    const val CAT_KEY = "cat"

    const val ASTRONAUT_AVATAR_INDEX = 7  // ic_avatar_astronaut
    const val CAT_AVATAR_INDEX = 8        // ic_avatar_cat

    /** Возвращает true, если космонавт доступен (365 дней с первого запуска). */
    suspend fun isAstronautUnlocked(ctx: Context): Boolean {
        val iso = SettingsRepository.firstLaunchFlow(ctx).first() ?: return false
        val firstDate = runCatching { LocalDate.parse(iso) }.getOrNull() ?: return false
        val days = ChronoUnit.DAYS.between(firstDate, LocalDate.now())
        return days >= 365
    }

    /** Возвращает true, если кот доступен (50 созданных карточек). */
    suspend fun isCatUnlocked(ctx: Context): Boolean {
        val count = SettingsRepository.eventsCreatedFlow(ctx).first()
        return count >= 50
    }

    /**
     * Возвращает прогресс: (текущее, требуемое).
     * Для космонавта — дни с первого запуска / 365.
     * Для кота — созданных карточек / 50.
     */
    suspend fun astronautProgress(ctx: Context): Pair<Int, Int> {
        val iso = SettingsRepository.firstLaunchFlow(ctx).first() ?: return 0 to 365
        val firstDate = runCatching { LocalDate.parse(iso) }.getOrNull() ?: return 0 to 365
        val days = ChronoUnit.DAYS.between(firstDate, LocalDate.now()).toInt().coerceAtLeast(0)
        return days to 365
    }

    suspend fun catProgress(ctx: Context): Pair<Int, Int> {
        val count = SettingsRepository.eventsCreatedFlow(ctx).first()
        return count to 50
    }

    /** Проверка на разблокировку и возврат ключа для показа всплывашки (или null). */
    suspend fun checkAndGetNewlyUnlocked(ctx: Context): String? {
        val shown = SettingsRepository.achievementsShownFlow(ctx).first()
        if (ASTRONAUT_KEY !in shown && isAstronautUnlocked(ctx)) return ASTRONAUT_KEY
        if (CAT_KEY !in shown && isCatUnlocked(ctx)) return CAT_KEY
        return null
    }
}
