package ru.yourname.events.ads

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    Box(
        modifier = modifier.fillMaxWidth().height(64.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    val adView = BannerAdView(ctx).apply {
                        setAdSize(BannerAdSize.sticky(ctx, 320))
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
                            override fun onLeftApplication() {}
                            override fun onReturnedToApplication() {}
                            override fun onImpression(impressionData: ImpressionData?) {}
                        })
                        loadAd(AdRequest.Builder(adUnitId).build())
                    }
                    addView(adView)
                }
            }
        )
    }
}
