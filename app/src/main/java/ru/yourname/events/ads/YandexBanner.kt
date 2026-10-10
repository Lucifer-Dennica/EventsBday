package ru.yourname.events.ads

import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest

@Composable
fun YandexBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdsConfig.BANNER_ID
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            FrameLayout(ctx).apply {
                val adView = BannerAdView(ctx).apply {
                    setAdSize(BannerAdSize.sticky(ctx, 320))
                    loadAd(AdRequest.Builder(adUnitId).build())
                }
                addView(adView)
            }
        }
    )
}
