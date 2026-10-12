package ru.yourname.events

import android.app.Application
import com.yandex.mobile.ads.common.YandexAds
import ru.yourname.events.notif.NotificationHelper

class EventsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        YandexAds.initialize(this) { }
    }
}
