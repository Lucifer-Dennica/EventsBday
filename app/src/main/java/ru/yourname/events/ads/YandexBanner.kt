package ru.yourname.events.ads

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData

@Composable
fun YandexBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdsConfig.BANNER_ID
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            val adWidthPixels = ctx.resources.displayMetrics.widthPixels
            val adWidthDp = (adWidthPixels / ctx.resources.displayMetrics.density).toInt()

            BannerAdView(ctx).apply {
                setAdSize(BannerAdSize.sticky(ctx, adWidthDp))
                setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() {
                        Log.d("AdsDebug", "Banner loaded OK")
                    }
                    override fun onAdFailedToLoad(error: AdRequestError) {
                        Log.e("AdsDebug", "Banner failed: ${error.description} (code ${error.code})")
                    }
                    override fun onAdClicked() {
                        Log.d("AdsDebug", "Banner clicked")
                    }
                    override fun onImpression(impressionData: ImpressionData?) {}
                })
                loadAd(AdRequest.Builder(adUnitId).build())
            }
        }
    )
}
