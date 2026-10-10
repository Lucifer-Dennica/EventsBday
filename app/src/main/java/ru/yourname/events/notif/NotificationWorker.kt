package ru.yourname.events.notif

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ru.yourname.events.data.db.AppDatabase
import ru.yourname.events.data.model.SocialLink
import ru.yourname.events.data.model.SocialPlatform
import ru.yourname.events.data.settings.NotificationFrequency
import ru.yourname.events.data.settings.NotificationMode
import ru.yourname.events.data.settings.SettingsRepository
import java.time.LocalDate

class NotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext

        // Проверяем, включены ли уведомления вообще
        val enabled = SettingsRepository.notifEnabledFlow(ctx)
            .let { flow ->
                var v = true
                kotlinx.coroutines.flow.first(flow).let { v = it }
                v
            }
        if (!enabled) return Result.success()

        val mode = kotlinx.coroutines.flow.first(SettingsRepository.notifModeFlow(ctx))
        val freq = kotlinx.coroutines.flow.first(SettingsRepository.notifFrequencyFlow(ctx))
        val daysBefore = kotlinx.coroutines.flow.first(SettingsRepository.notifDaysBeforeFlow(ctx))

        val dao = AppDatabase.get(ctx).eventDao()
        val entities = kotlinx.coroutines.flow.first(dao.observeAll())
        val today = LocalDate.now()

        entities.forEach { entity ->
            // Пропускаем те, у которых уведомления выключены на карточке
            if (!entity.notificationsEnabled) return@forEach
            // В режиме "Выборочно" уведомляем только о тех, у кого тумблер включён
            if (mode == NotificationMode.CUSTOM && !entity.notificationsEnabled) return@forEach

            val event = ru.yourname.events.data.model.Event(
                id = entity.id,
                name = entity.name,
                dayOfMonth = entity.dayOfMonth,
                month = entity.month,
                year = entity.year,
                group = entity.group,
                note = entity.note,
                photoUri = entity.photoUri,
                presetAvatar = entity.presetAvatar,
                contactId = entity.contactId,
                isFromContacts = entity.isFromContacts,
                socialLinks = emptyList(),
                notificationsEnabled = entity.notificationsEnabled,
                cardColor = entity.cardColor
            )

            val days = event.daysUntil(today)
            if (days < 0 || days > daysBefore) return@forEach

            // Логика частоты: проверяем, нужно ли уведомлять именно сегодня
            val shouldNotify = when (freq) {
                NotificationFrequency.ONCE -> days == 0L
                NotificationFrequency.EVERY_DAY -> true
                NotificationFrequency.EVERY_2_DAYS -> (days % 2L) == 0L
                NotificationFrequency.EVERY_3_DAYS -> (days % 3L) == 0L
                NotificationFrequency.EVERY_WEEK -> (days % 7L) == 0L
            }
            if (!shouldNotify) return@forEach

            val title = when {
                days == 0L -> "🎉 Сегодня: ${event.name}"
                days == 1L -> "Завтра: ${event.name}"
                else -> "Через $days дн.: ${event.name}"
            }
            val age = event.ageAtNext(today)
            val text = buildString {
                if (age != null) append("Исполнится $age лет. ")
                if (event.note.isNotBlank()) append(event.note)
            }.trim().ifBlank { "Не забудьте поздравить!" }

            NotificationHelper.showEventNotification(
                ctx = ctx,
                notificationId = event.id.toInt(),
                title = title,
                text = text
            )
        }

        return Result.success()
    }
}
