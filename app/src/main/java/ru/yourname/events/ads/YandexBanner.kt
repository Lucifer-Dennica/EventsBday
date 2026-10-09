package ru.yourname.events.ads

import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest

/**
 * Простой баннер Yandex Mobile Ads.
 * Рекомендуется вставлять в настройках, а не в основном списке.
 */
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
                    setAdUnitId(adUnitId)
                    setAdSize(AdSize.BANNER_320x50)
                    loadAd(AdRequest.Builder().build())
                }
                addView(adView)
            }
        }
    )
}
