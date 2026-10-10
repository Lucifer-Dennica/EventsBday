package ru.yourname.events

import android.app.Application
import android.util.Log
import com.yandex.mobile.ads.common.YandexAds
import ru.yourname.events.notif.NotificationHelper

class EventsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        Log.d("AdsDebug", "YandexAds.initialize() called")
        YandexAds.initialize(this) {
            Log.d("AdsDebug", "YandexAds SDK initialized successfully")
        }
    }
}
