package ru.yourname.events

import android.app.Application
import com.yandex.mobile.ads.common.YandexAds

class EventsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        YandexAds.initialize(this) {
            // SDK готов к загрузке объявлений
        }
    }
}
