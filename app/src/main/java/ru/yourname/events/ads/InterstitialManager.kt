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
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

/**
 * Управляет загрузкой и показом межстраничной рекламы (SDK 8).
 */
object InterstitialManager {

    private var ad: InterstitialAd? = null
    private var loader: InterstitialAdLoader? = null

    /** Вызывать при старте приложения и после каждого показа — предзагрузка. */
    fun preload(context: Context) {
        if (ad != null) return
        val loader = InterstitialAdLoader(context).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    ad = interstitialAd
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
                }
            })
        }
        this.loader = loader
        loader.loadAd(AdRequest.Builder(AdsConfig.INTERSTITIAL_ID).build())
    }

    /** Показать рекламу, если загружена. Если нет — просто пропускаем. */
    fun showIfReady(activity: Activity) {
        ad?.show(activity)
    }
}
