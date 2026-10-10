package ru.yourname.events.ads

import android.app.Activity
import android.content.Context
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object InterstitialManager {

    private var ad: InterstitialAd? = null
    private var isLoading = false

    /**
     * Предзагрузка рекламы. Вызывается при старте приложения и после показа.
     * loadAd — suspend-функция, поэтому запускаем в корутине.
     */
    fun preload(context: Context) {
        if (ad != null || isLoading) return
        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loader = InterstitialAdLoader(context)
                val request = AdRequest.Builder(AdsConfig.INTERSTITIAL_ID).build()
                val loadedAd = loader.loadAd(request)
                withContext(Dispatchers.Main) {
                    ad = loadedAd
                    isLoading = false
                    loadedAd.setAdEventListener(object : InterstitialAdEventListener {
                        override fun onAdDismissed() {
                            ad = null
                            preload(context)
                        }
                        override fun onAdFailedToShow(adError: AdError) {
                            ad = null
                            preload(context)
                        }
                        override fun onAdShown() {}
                        override fun onAdImpression(impressionData: ImpressionData?) {}
                        override fun onAdClicked() {}
                    })
                }
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    /** Показать рекламу, если загружена. */
    fun showIfReady(activity: Activity) {
        ad?.show(activity)
    }
}
