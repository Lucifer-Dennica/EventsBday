package ru.yourname.events.ads

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val ctx = LocalContext.current

    Box(
        modifier = modifier.fillMaxWidth().height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            factory = { c ->
                val dm = c.resources.displayMetrics
                val widthDp = (dm.widthPixels / dm.density).toInt() - 16  // минус padding
                Log.d("AdsDebug", "Creating banner, widthDp=$widthDp")

                BannerAdView(c).apply {
                    setAdSize(BannerAdSize.inline(c, widthDp, 50))
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
            },
            update = { /* ничего не меняем */ }
        )
    }

    // Просим SDK обновить баннер при пересоздании
    DisposableEffect(Unit) { onDispose { } }
}
