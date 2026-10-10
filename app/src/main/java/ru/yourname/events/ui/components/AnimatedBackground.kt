package ru.yourname.events.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AnimatedBackground(
    type: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    if (type == "NONE") return

    val transition = rememberInfiniteTransition(label = "bg")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (type == "FIRE") 3000 else 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val rnd = remember { Random(42) }
    val particles = remember {
        List(18) {
            Particle(
                x = rnd.nextFloat(),
                y = rnd.nextFloat(),
                size = 6f + rnd.nextFloat() * 12f,
                phase = rnd.nextFloat() * 2f * PI.toFloat(),
                speed = 0.6f + rnd.nextFloat() * 0.8f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        when (type) {
            "BUBBLES" -> drawBubbles(particles, progress, tint)
            "FIRE" -> drawFire(particles, progress, tint)
            "SNOW" -> drawSnow(particles, progress, tint)
            "STARS" -> drawStars(particles, progress, tint)
            "PETALS" -> drawPetals(particles, progress, tint)
            "WAVES" -> drawWaves(progress, tint)
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val phase: Float,
    val speed: Float
)

private fun DrawScope.drawBubbles(particles: List<Particle>, progress: Float, tint: Color) {
    particles.forEach { p ->
        val y = ((p.y + 1f - progress * p.speed) % 1f) * size.height
        val x = p.x * size.width + sin(progress * 6.28f + p.phase) * 12f
        drawCircle(
            color = tint.copy(alpha = 0.18f),
            radius = p.size,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawFire(particles: List<Particle>, progress: Float, tint: Color) {
    particles.forEach { p ->
        val offsetY = sin(progress * 6.28f * p.speed + p.phase) * 8f
        val y = size.height - p.y * 30f + offsetY
        val x = p.x * size.width
        val alpha = 0.25f * (1f - p.y)
        drawCircle(
            color = tint.copy(alpha = alpha.coerceIn(0f, 0.35f)),
            radius = p.size,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawSnow(particles: List<Particle>, progress: Float, tint: Color) {
    particles.forEach { p ->
        val y = ((p.y + progress * p.speed) % 1f) * size.height
        val x = p.x * size.width + sin(progress * 4f + p.phase) * 10f
        drawCircle(
            color = tint.copy(alpha = 0.35f),
            radius = p.size * 0.5f,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawStars(particles: List<Particle>, progress: Float, tint: Color) {
    particles.forEach { p ->
        val twinkle = (0.5f + 0.5f * sin(progress * 6.28f * 2f + p.phase))
        drawCircle(
            color = tint.copy(alpha = 0.15f + 0.25f * twinkle),
            radius = p.size * 0.35f,
            center = Offset(p.x * size.width, p.y * size.height)
        )
    }
}

private fun DrawScope.drawPetals(particles: List<Particle>, progress: Float, tint: Color) {
    particles.forEach { p ->
        val y = ((p.y + progress * p.speed) % 1f) * size.height
        val x = p.x * size.width + cos(progress * 5f + p.phase) * 20f
        drawCircle(
            color = tint.copy(alpha = 0.28f),
            radius = p.size * 0.6f,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawWaves(progress: Float, tint: Color) {
    for (layer in 0..2) {
        val amplitude = 8f + layer * 4f
        val yBase = size.height - 10f - layer * 8f
        val alpha = 0.12f - layer * 0.03f
        val path = Path()
        path.moveTo(0f, yBase)
        var x = 0f
        while (x <= size.width) {
            val y = yBase + sin((x / size.width) * 4f * PI.toFloat() + progress * 6.28f + layer) * amplitude
            path.lineTo(x, y)
            x += 8f
        }
        path.lineTo(size.width, size.height)
        path.lineTo(0f, size.height)
        path.close()
        drawPath(path = path, color = tint.copy(alpha = alpha.coerceAtLeast(0.04f)))
    }
}
