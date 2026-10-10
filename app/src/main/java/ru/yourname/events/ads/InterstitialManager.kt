package ru.yourname.events.ads

import android.app.Activity
import android.content.Context
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object InterstitialManager {

    private var ad: InterstitialAd? = null
    private var isLoading = false

    fun preload(context: Context) {
        if (ad != null || isLoading) return
        isLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loader = InterstitialAdLoader(context)
                val request = AdRequest.Builder(AdsConfig.INTERSTITIAL_ID).build()

                // loadAd — suspend-функция, возвращает InterstitialAdLoadResult
                when (val result = loader.loadAd(request)) {
                    is InterstitialAdLoadResult.Success -> {
                        // ✅ Правильное поле — result.ad (подтверждено документацией)
                        val loadedAd = result.ad
                        withContext(Dispatchers.Main) {
                            ad = loadedAd
                            isLoading = false
                            loadedAd.setAdEventListener(object : InterstitialAdEventListener {
                                override fun onAdShown() {}
                                override fun onAdFailedToShow(adError: AdError) {
                                    ad = null
                                    preload(context)
                                }
                                override fun onAdDismissed() {
                                    ad = null
                                    preload(context)
                                }
                                override fun onAdClicked() {}
                                override fun onAdImpression(impressionData: ImpressionData?) {}
                            })
                        }
                    }
                    is InterstitialAdLoadResult.Failure -> {
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    fun showIfReady(activity: Activity) {
        ad?.show(activity)
    }
}
