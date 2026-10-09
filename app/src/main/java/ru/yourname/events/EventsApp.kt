package ru.yourname.events

import android.app.Application
import com.yandex.mobile.ads.common.MobileAds

class EventsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {
            // SDK готов к загрузке объявлений
        }
    }
}
