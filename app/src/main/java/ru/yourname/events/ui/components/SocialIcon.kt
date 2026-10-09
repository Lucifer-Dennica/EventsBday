package ru.yourname.events.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.yourname.events.data.model.SocialPlatform

/** Если есть PNG в drawable/ic_social_<key>.png — показывает её. Иначе — эмодзи. */
@Composable
fun SocialIcon(platform: SocialPlatform, size: Dp = 24.dp) {
    val ctx = LocalContext.current
    val resId = platform.iconResId(ctx)
    if (resId != 0) {
        Icon(
            painter = painterResource(resId),
            contentDescription = platform.label,
            modifier = Modifier.size(size),
            tint = androidx.compose.ui.graphics.Color.Unspecified
        )
    } else {
        Text(platform.emoji, style = MaterialTheme.typography.titleMedium)
    }
}
