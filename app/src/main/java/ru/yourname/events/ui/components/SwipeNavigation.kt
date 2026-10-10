package ru.yourname.events.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Обёртка, реагирующая на горизонтальный свайп:
 * - [onSwipeLeft] — палец ведётся влево (свайп справа налево).
 * - [onSwipeRight] — палец ведётся вправо (свайп слева направо).
 *
 * Порог срабатывания — [thresholdPx] пикселей.
 */
@Composable
fun SwipeNavigationBox(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    thresholdPx: Float = 150f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(onSwipeLeft, onSwipeRight) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (totalDrag < -thresholdPx) {
                            onSwipeLeft?.invoke()
                        } else if (totalDrag > thresholdPx) {
                            onSwipeRight?.invoke()
                        }
                    }
                ) { _, dragAmount ->
                    totalDrag += dragAmount
                }
            }
    ) {
        content()
    }
}
