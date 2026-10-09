package ru.yourname.events.ads

import android.app.Activity
import android.content.Context
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener

/**
 * Управляет загрузкой и показом межстраничной рекламы.
 * Загружаем заранее, показываем, когда счётчик открытий карточки достиг N.
 */
object InterstitialManager {

    private var ad: InterstitialAd? = null
    private var loading = false

    /** Вызывать при старте приложения и после каждого показа — предзагрузка. */
    fun preload(context: Context) {
        if (ad != null || loading) return
        loading = true
        InterstitialAd.load(
            context,
            AdsConfig.INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadListener {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    ad = interstitialAd
                    loading = false
                    interstitialAd.setAdEventListener(object : InterstitialAdEventListener {
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

                override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                    ad = null
                    loading = false
                }
            }
        )
    }

    /** Показать рекламу, если загружена. Если нет — просто пропускаем. */
    fun showIfReady(activity: Activity) {
        ad?.show(activity)
    }
}
