package ru.yourname.events.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

/**
 * Обёртка, реагирующая на горизонтальный свайп:
 * - если [onSwipeLeft] передан — свайп влево (движение пальца влево) вызывает его.
 * - если [onSwipeRight] передан — свайп вправо вызывает его.
 *
 * Порог срабатывания — [thresholdPx] пикселей. Использует awaitEachGesture для
 * надёжного захвата жеста даже если палец начал движение на скроллируемой области.
 */
@Composable
fun SwipeNavigationBox(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    thresholdPx: Float = 100f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(onSwipeLeft, onSwipeRight) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var totalX = 0f
                    var totalY = 0f
                    val pointerId = down.id
                    var isUp = false

                    while (!isUp) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId }
                        if (change == null) { isUp = true; continue }
                        totalX += change.positionChange().x
                        totalY += change.positionChange().y
                        if (change.changedToUp()) isUp = true
                    }

                    // Свайп считается горизонтальным, если X-смещение больше Y хотя бы в 1.5 раза
                    if (abs(totalX) > thresholdPx && abs(totalX) > abs(totalY) * 1.5f) {
                        if (totalX < 0f) {
                            onSwipeLeft?.invoke()
                        } else {
                            onSwipeRight?.invoke()
                        }
                    }
                }
            }
    ) {
        content()
    }
}
