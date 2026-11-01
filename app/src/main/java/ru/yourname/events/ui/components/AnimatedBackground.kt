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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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

    val duration = when (type) {
        "FIRE" -> 2200
        "STARS" -> 10000
        "FISH" -> 8000
        "BUTTERFLIES" -> 9000
        "JELLYFISH" -> 18000
        "WAVES", "WAVES_FOAM" -> 5000
        "CONFETTI" -> 8000
        "HEARTS" -> 10000
        "FIREWORKS" -> 6000
        "BALLOONS" -> 12000
        "SPARKS" -> 5000
        "STREAMERS" -> 9000
        "CRYSTALS" -> 9000
        "COINS" -> 7000
        else -> 9000
    }

    val transition = rememberInfiniteTransition(label = "bg")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val rnd = remember { Random(42) }
    val smallParticles = remember {
        List(20) {
            Particle(
                x = rnd.nextFloat(), y = rnd.nextFloat(),
                size = 5f + rnd.nextFloat() * 8f,
                phase = rnd.nextFloat() * 2f * PI.toFloat(),
                speed = 0.8f + rnd.nextFloat() * 0.6f,
                rotation = rnd.nextFloat() * 360f,
                rotSpeed = 20f + rnd.nextFloat() * 60f
            )
        }
    }
    val bigParticles = remember {
        List(6) {
            Particle(
                x = rnd.nextFloat(), y = rnd.nextFloat(),
                size = 16f + rnd.nextFloat() * 10f,
                phase = rnd.nextFloat() * 2f * PI.toFloat(),
                speed = 0.7f + rnd.nextFloat() * 0.5f,
                rotation = rnd.nextFloat() * 360f,
                rotSpeed = 15f + rnd.nextFloat() * 30f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        when (type) {
            "BUBBLES" -> drawBubbles(smallParticles, progress, tint)
            "FIRE" -> drawFireRealistic(progress)
            "SNOW" -> drawSnowVariety(smallParticles + bigParticles, progress)
            "STARS" -> drawStars(smallParticles, progress)
            "LEAVES" -> drawLeaves(smallParticles + bigParticles, progress)
            "FLOWERS" -> drawFlowers(smallParticles + bigParticles, progress)
            "FISH" -> drawFish(smallParticles, progress)
            "BUTTERFLIES" -> drawButterflies(smallParticles, progress)
            "JELLYFISH" -> drawJellyfish(bigParticles, progress, tint)
            "WAVES" -> drawWavesSimple(progress, tint)
            "WAVES_FOAM" -> drawWavesFoam(progress, tint)
            "CONFETTI" -> drawConfetti(smallParticles + bigParticles, progress)
            "HEARTS" -> drawHearts(smallParticles + bigParticles, progress)
            "FIREWORKS" -> drawFireworks(progress, tint)
            "BALLOONS" -> drawBalloons(bigParticles, progress)
            "SPARKS" -> drawSparks(smallParticles, progress)
            "STREAMERS" -> drawStreamers(smallParticles + bigParticles, progress)
            "CRYSTALS" -> drawCrystals(smallParticles, progress)
            "COINS" -> drawCoins(smallParticles, progress)
        }
    }
}

private data class Particle(
    val x: Float, val y: Float, val size: Float,
    val phase: Float, val speed: Float,
    val rotation: Float, val rotSpeed: Float
)

// ======================= ПУЗЫРЬКИ =======================
private fun DrawScope.drawBubbles(particles: List<Particle>, progress: Float, tint: Color) {
    particles.forEach { p ->
        val raw = p.y + progress * p.speed
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = (1f - yNorm) * size.height
            if (y < -40f || y > size.height + 40f) continue
            val x = p.x * size.width + sin(progress * 6.28f + p.phase) * 14f
            drawCircle(tint.copy(alpha = 0.18f), p.size, Offset(x, y), style = Stroke(width = 2f))
            drawCircle(Color.White.copy(alpha = 0.15f), p.size * 0.35f, Offset(x - p.size * 0.3f, y - p.size * 0.3f))
        }
    }
}

// ======================= ОГОНЬ =======================
private fun DrawScope.drawFireRealistic(progress: Float) {
    val baseY = size.height
    val flameCount = 14
    for (i in 0 until flameCount) {
        val xFrac = i.toFloat() / (flameCount - 1)
        val cx = xFrac * size.width
        val phase = i * 0.7f
        val flicker = sin(progress * 6.28f * 2.5f + phase)
        val wave = sin(progress * 6.28f * 1.8f + phase * 1.3f) * 14f
        val heightVar = when {
            i % 5 == 0 -> 0.85f
            i % 3 == 0 -> 0.65f
            else -> 0.45f + (i % 4) * 0.08f
        }
        val height = size.height * heightVar * (0.92f + 0.08f * flicker)
        val halfWidth = size.width / (flameCount * 0.9f)
        val path = Path().apply {
            moveTo(cx - halfWidth, baseY)
            cubicTo(cx - halfWidth * 0.8f + wave, baseY - height * 0.4f, cx - halfWidth * 0.4f + wave, baseY - height * 0.75f, cx + wave * 0.8f, baseY - height)
            cubicTo(cx + halfWidth * 0.4f + wave, baseY - height * 0.75f, cx + halfWidth * 0.8f + wave, baseY - height * 0.4f, cx + halfWidth, baseY)
            close()
        }
        drawPath(path, brush = Brush.verticalGradient(
            listOf(Color(0x00FF2200), Color(0x88FF3300), Color(0xEEFF6600)),
            startY = baseY - height, endY = baseY
        ))
    }
}

// ======================= СНЕГ =======================
private fun DrawScope.drawSnowVariety(particles: List<Particle>, progress: Float) {
    particles.forEach { p ->
        val raw = p.y + progress * p.speed
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = yNorm * size.height
            if (y < -40f || y > size.height + 40f) continue
            val x = p.x * size.width + sin(progress * 4f + p.phase) * 14f
            val rot = (p.rotation + progress * p.rotSpeed) % 360f
            drawSnowflake6(x, y, p.size, rot, Color.White.copy(alpha = 0.85f))
        }
    }
}

private fun DrawScope.drawSnowflake6(cx: Float, cy: Float, radius: Float, rotation: Float, color: Color) {
    rotate(rotation, Offset(cx, cy)) {
        val strokeW = (radius / 8f).coerceAtLeast(1.2f)
        for (i in 0 until 6) {
            rotate(i * 60f, Offset(cx, cy)) {
                drawLine(color, Offset(cx, cy), Offset(cx, cy - radius), strokeW)
                val branchStartY = cy - radius * 0.7f
                val branchLen = radius * 0.3f
                drawLine(color, Offset(cx, branchStartY), Offset(cx - branchLen, branchStartY - branchLen), strokeW * 0.8f)
                drawLine(color, Offset(cx, branchStartY), Offset(cx + branchLen, branchStartY - branchLen), strokeW * 0.8f)
            }
        }
        drawCircle(color, strokeW * 1.5f, Offset(cx, cy))
    }
}

// ======================= ЗВЁЗДЫ =======================
private fun DrawScope.drawStars(particles: List<Particle>, progress: Float) {
    particles.forEach { p ->
        val fast = sin(progress * 6.28f * 5f + p.phase)
        val slow = sin(progress * 6.28f * 2f + p.phase * 0.7f)
        val twinkle = 0.3f + 0.7f * (0.5f + 0.5f * (fast * 0.6f + slow * 0.4f))
        val r = p.size * 0.28f
        drawCircle(Color(0xFFFFE082).copy(alpha = twinkle * 0.4f), r * 2.5f, Offset(p.x * size.width, p.y * size.height))
        drawCircle(Color(0xFFFFF8D0).copy(alpha = twinkle), r * (0.8f + 0.4f * twinkle), Offset(p.x * size.width, p.y * size.height))
    }
}

// ======================= ОСЕННИЕ ЛИСТЬЯ =======================
private fun DrawScope.drawLeaves(particles: List<Particle>, progress: Float) {
    val autumnColors = listOf(Color(0xFFE65100), Color(0xFFFF8F00), Color(0xFFD84315), Color(0xFFFFB300), Color(0xFFBF360C), Color(0xFFF57C00))
    particles.forEachIndexed { idx, p ->
        val raw = p.y + progress * p.speed
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = yNorm * size.height
            if (y < -60f || y > size.height + 60f) continue
            val x = p.x * size.width + cos(progress * 5f + p.phase) * 26f
            val rot = (p.rotation + progress * p.rotSpeed) % 360f
            drawLeaf(x, y, p.size, rot, autumnColors[idx % autumnColors.size].copy(alpha = 0.9f))
        }
    }
}

private fun DrawScope.drawLeaf(cx: Float, cy: Float, size: Float, rotation: Float, color: Color) {
    rotate(rotation, Offset(cx, cy)) {
        val w = size * 0.85f
        val h = size * 1.4f
        val path = Path().apply {
            moveTo(cx, cy - h)
            cubicTo(cx + w, cy - h * 0.5f, cx + w * 0.9f, cy + h * 0.3f, cx + w * 0.3f, cy + h * 0.8f)
            quadraticBezierTo(cx, cy + h, cx - w * 0.3f, cy + h * 0.8f)
            cubicTo(cx - w * 0.9f, cy + h * 0.3f, cx - w, cy - h * 0.5f, cx, cy - h)
            close()
        }
        drawPath(path, color)
    }
}

// ======================= ЦВЕТЫ =======================
private fun DrawScope.drawFlowers(particles: List<Particle>, progress: Float) {
    val flowerColors = listOf(Color(0xFFE91E63), Color(0xFFF06292), Color(0xFF9C27B0), Color(0xFFFF5722), Color(0xFFE1BEE7), Color(0xFFFF7043))
    particles.forEachIndexed { idx, p ->
        val raw = p.y + progress * p.speed
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = yNorm * size.height
            if (y < -60f || y > size.height + 60f) continue
            val x = p.x * size.width + cos(progress * 5f + p.phase) * 26f
            val rot = (p.rotation + progress * p.rotSpeed * 0.5f) % 360f
            drawFlowerRealistic(x, y, p.size, rot, flowerColors[idx % flowerColors.size].copy(alpha = 0.9f))
        }
    }
}

private fun DrawScope.drawFlowerRealistic(cx: Float, cy: Float, size: Float, rotation: Float, petalColor: Color) {
    rotate(rotation, Offset(cx, cy)) {
        val petalCount = 6
        val petalDistance = size * 0.65f
        val petalRadius = size * 0.55f
        for (i in 0 until petalCount) {
            val angle = i * (360f / petalCount) * PI.toFloat() / 180f
            drawCircle(petalColor, petalRadius, Offset(cx + cos(angle) * petalDistance, cy + sin(angle) * petalDistance))
        }
        drawCircle(Color(0xFFFFEB3B), size * 0.42f, Offset(cx, cy))
    }
}

// ======================= РЫБКИ =======================
private fun DrawScope.drawFish(particles: List<Particle>, progress: Float) {
    particles.forEachIndexed { idx, p ->
        val startOffset = p.phase / 6.28f
        val rawX = startOffset + progress * p.speed
        val spanW = size.width + 120f
        for (k in 0..1) {
            val xNorm = rawX - k.toFloat()
            val x = xNorm * spanW - 60f
            if (x < -100f || x > size.width + 100f) continue
            val y = p.y * size.height + sin(progress * 6.28f * 2 + p.phase) * 8f
            val color = when (idx % 4) { 0 -> Color(0xFFFF7043); 1 -> Color(0xFFFFCA28); 2 -> Color(0xFF42A5F5); else -> Color(0xFFAB47BC) }
            drawFishShape(x, y, p.size * 2.2f, color.copy(alpha = 0.85f))
        }
    }
}

private fun DrawScope.drawFishShape(cx: Float, cy: Float, size: Float, color: Color) {
    val bodyW = size * 1.6f
    val bodyH = size * 0.9f
    drawOval(color, topLeft = Offset(cx - bodyW * 0.5f, cy - bodyH * 0.5f), size = Size(bodyW, bodyH))
    val path = Path().apply {
        moveTo(cx - bodyW * 0.5f, cy)
        lineTo(cx - bodyW * 0.5f - size * 0.7f, cy - size * 0.5f)
        lineTo(cx - bodyW * 0.5f - size * 0.7f, cy + size * 0.5f)
        close()
    }
    drawPath(path, color)
    drawCircle(Color.White, size * 0.14f, Offset(cx + bodyW * 0.25f, cy - size * 0.1f))
}

// ======================= БАБОЧКИ =======================
private fun DrawScope.drawButterflies(particles: List<Particle>, progress: Float) {
    val butterflyColors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFFFF9800), Color(0xFF3F51B5), Color(0xFF00BCD4), Color(0xFFFFC107))
    particles.forEachIndexed { idx, p ->
        val baseX = p.x * size.width
        val baseY = p.y * size.height
        val angle = progress * 6.28f * p.speed + p.phase
        val x = baseX + cos(angle) * (40f + p.size * 2f)
        val y = baseY + sin(angle * 1.3f) * (25f + p.size * 1.5f)
        val flap = (sin(progress * 6.28f * 6f + p.phase) + 1f) / 2f
        drawButterfly(x, y, p.size * 1.8f, flap, butterflyColors[idx % butterflyColors.size].copy(alpha = 0.9f))
    }
}

private fun DrawScope.drawButterfly(cx: Float, cy: Float, size: Float, flap: Float, color: Color) {
    val wingScale = 1f - flap * 0.7f
    val wingH = size * 1.2f * wingScale
    val wingW = size * 1.0f
    drawOval(color, topLeft = Offset(cx - wingW * 0.9f, cy - wingH * 0.6f), size = Size(wingW, wingH * 0.9f))
    drawOval(color, topLeft = Offset(cx - wingW * 0.1f, cy - wingH * 0.6f), size = Size(wingW, wingH * 0.9f))
}

// ======================= МЕДУЗЫ =======================
private fun DrawScope.drawJellyfish(particles: List<Particle>, progress: Float, tint: Color) {
    particles.forEach { p ->
        val raw = p.y + progress * p.speed
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = yNorm * size.height
            if (y < -160f || y > size.height + 160f) continue
            val wobble = sin(progress * 6.28f * 2f + p.phase)
            val x = p.x * size.width + wobble * 24f
            val js = p.size * 1.5f

            val bell = Path().apply {
                moveTo(x - js, y)
                cubicTo(x - js, y - js * 1.5f, x + js, y - js * 1.5f, x + js, y)
                cubicTo(x + js * 0.5f, y + js * 0.35f, x + js * 0.2f, y - js * 0.3f, x, y)
                cubicTo(x - js * 0.2f, y - js * 0.3f, x - js * 0.5f, y + js * 0.35f, x - js, y)
                close()
            }
            drawPath(bell, tint.copy(alpha = 0.6f))
            drawPath(bell, Color.White.copy(alpha = 0.35f), style = Stroke(width = 2.5f))

            val tentacleCount = 6
            for (t in 0 until tentacleCount) {
                val tx = x - js * 0.7f + (t * js * 0.28f)
                val w1 = sin(progress * 6.28f * 3f + t * 0.7f)
                val w2 = sin(progress * 6.28f * 3f + t * 0.7f + 1.5f)
                val tentacle = Path().apply {
                    moveTo(tx, y + js * 0.05f)
                    cubicTo(tx + w1 * 18f, y + js * 1.1f, tx - w2 * 24f, y + js * 2.0f, tx + w1 * 14f, y + js * 3.0f)
                }
                drawPath(tentacle, tint.copy(alpha = 0.5f), style = Stroke(width = 2.5f))
            }
        }
    }
}

// ======================= ВОЛНЫ =======================
private fun DrawScope.drawWavesSimple(progress: Float, tint: Color) {
    for (layer in 0..2) {
        val amplitude = 8f + layer * 5f
        val yBase = size.height - (size.height * 0.20f) + layer * 12f
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

private fun DrawScope.drawWavesFoam(progress: Float, tint: Color) {
    for (layer in 0..2) {
        val amplitude = 10f + layer * 6f
        val yBase = size.height - (size.height * 0.20f) + layer * 12f
        val alpha = 0.18f - layer * 0.04f
        val path = Path()
        path.moveTo(0f, yBase)
        var x = 0f
        while (x <= size.width) {
            val y = yBase + sin((x / size.width) * 4f * PI.toFloat() + progress * 6.28f * (1f - layer * 0.2f) + layer) * amplitude
            path.lineTo(x, y)
            x += 6f
        }
        path.lineTo(size.width, size.height)
        path.lineTo(0f, size.height)
        path.close()
        drawPath(path = path, color = tint.copy(alpha = alpha.coerceAtLeast(0.06f)))
    }
    val foamCount = 12
    for (i in 0 until foamCount) {
        val fx = (i + 0.5f) / foamCount * size.width
        val phase = i * 0.8f
        val baseY = size.height - (size.height * 0.20f)
        val y = baseY + sin((fx / size.width) * 4f * PI.toFloat() + progress * 6.28f) * 10f
        val visible = sin(progress * 6.28f * 2f + phase)
        if (visible > 0.3f) {
            val alpha = (visible - 0.3f) / 0.7f
            drawCircle(Color.White.copy(alpha = alpha * 0.6f), 1.5f + alpha * 1.5f, Offset(fx, y - 2f))
        }
    }
}

// ======================= 🎉 КОНФЕТТИ =======================
private fun DrawScope.drawConfetti(particles: List<Particle>, progress: Float) {
    val colors = listOf(
        Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFFFFEB3B),
        Color(0xFF4CAF50), Color(0xFF00BCD4), Color(0xFF3F51B5),
        Color(0xFF9C27B0), Color(0xFFF44336)
    )
    particles.forEachIndexed { idx, p ->
        val raw = p.y + progress * p.speed
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = yNorm * size.height
            if (y < -30f || y > size.height + 30f) continue
            val x = p.x * size.width + sin(progress * 6.28f * 2f + p.phase) * 30f
            val rot = (p.rotation + progress * p.rotSpeed * 2f) % 360f
            val color = colors[idx % colors.size]
            rotate(rot, Offset(x, y)) {
                val w = p.size * 0.6f
                val h = p.size * 1.2f
                drawRoundRect(
                    color = color.copy(alpha = 0.9f),
                    topLeft = Offset(x - w / 2, y - h / 2),
                    size = Size(w, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.2f)
                )
            }
        }
    }
}

// ======================= 💗 СЕРДЕЧКИ =======================
private fun DrawScope.drawHearts(particles: List<Particle>, progress: Float) {
    val colors = listOf(Color(0xFFE91E63), Color(0xFFF06292), Color(0xFFEC407A), Color(0xFFC2185B), Color(0xFFFF4081))
    particles.forEachIndexed { idx, p ->
        val raw = p.y + progress * p.speed
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            // Сердечки поднимаются снизу вверх
            val y = (1f - yNorm) * size.height
            if (y < -40f || y > size.height + 40f) continue
            val x = p.x * size.width + sin(progress * 5f + p.phase) * 20f
            val pulse = 0.85f + 0.15f * sin(progress * 6.28f * 4f + p.phase)
            val sz = p.size * 1.2f * pulse
            val color = colors[idx % colors.size]
            drawHeart(x, y, sz, color.copy(alpha = 0.85f))
        }
    }
}

private fun DrawScope.drawHeart(cx: Float, cy: Float, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(cx, cy + size * 0.3f)
        cubicTo(cx - size * 1.2f, cy - size * 0.4f, cx - size * 0.5f, cy - size * 1.0f, cx, cy - size * 0.3f)
        cubicTo(cx + size * 0.5f, cy - size * 1.0f, cx + size * 1.2f, cy - size * 0.4f, cx, cy + size * 0.3f)
        close()
    }
    drawPath(path, color)
}

// ======================= 🎆 САЛЮТ =======================
private fun DrawScope.drawFireworks(progress: Float, tint: Color) {
    // 5 «запусков» с разным фазовым сдвигом
    val bursts = 5
    val colors = listOf(
        Color(0xFFFF5252), Color(0xFFFFD740), Color(0xFF69F0AE),
        Color(0xFF40C4FF), Color(0xFFE040FB)
    )
    for (b in 0 until bursts) {
        // Каждый запуск начинается в свой момент — суммируем и берём по модулю
        val burstPhase = ((progress + b * 0.2f) % 1f)
        // «Вспышка» — первые 70% времени, затем затухает
        val intensity = when {
            burstPhase < 0.15f -> burstPhase / 0.15f          // разгорается
            burstPhase < 0.6f -> 1f                          // горит
            else -> (1f - (burstPhase - 0.6f) / 0.4f).coerceAtLeast(0f) // тухнет
        }
        if (intensity <= 0.01f) continue

        // Позиция: по псевдослучайной сетке из номера burst
        val cx = size.width * ((b * 0.37f + 0.15f) % 1f)
        val cy = size.height * ((b * 0.53f + 0.2f) % 0.7f + 0.15f)
        val baseColor = colors[b % colors.size]

        val rayCount = 12
        val maxRadius = 55f + (b % 3) * 15f
        val radius = maxRadius * intensity

        // Ядро
        drawCircle(
            color = Color.White.copy(alpha = intensity * 0.9f),
            radius = 4f + intensity * 4f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = baseColor.copy(alpha = intensity * 0.4f),
            radius = radius * 0.5f,
            center = Offset(cx, cy)
        )

        // Лучи
        for (r in 0 until rayCount) {
            val angle = r * (360f / rayCount) * PI.toFloat() / 180f + (b * 0.3f)
            val rInner = radius * 0.4f
            val rOuter = radius
            val x1 = cx + cos(angle) * rInner
            val y1 = cy + sin(angle) * rInner
            val x2 = cx + cos(angle) * rOuter
            val y2 = cy + sin(angle) * rOuter
            drawLine(
                color = baseColor.copy(alpha = intensity * 0.9f),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2.5f
            )
        }
    }
}

// ======================= 🎈 ВОЗДУШНЫЕ ШАРЫ =======================
private fun DrawScope.drawBalloons(particles: List<Particle>, progress: Float) {
    val colors = listOf(
        Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFFFFEB3B),
        Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0)
    )
    particles.forEachIndexed { idx, p ->
        val raw = p.y + progress * p.speed * 0.8f
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = (1f - yNorm) * size.height
            if (y < -80f || y > size.height + 80f) continue
            val x = p.x * size.width + sin(progress * 4f + p.phase) * 25f
            val color = colors[idx % colors.size]
            val sz = p.size * 2.0f
            drawBalloon(x, y, sz, color.copy(alpha = 0.85f))
        }
    }
}

private fun DrawScope.drawBalloon(cx: Float, cy: Float, size: Float, color: Color) {
    // Купол
    val bodyW = size * 1.0f
    val bodyH = size * 1.3f
    drawOval(color, topLeft = Offset(cx - bodyW / 2, cy - bodyH * 0.85f), size = Size(bodyW, bodyH))
    // Узелок
    val knot = Path().apply {
        moveTo(cx - 2.5f, cy + bodyH * 0.45f)
        lineTo(cx + 2.5f, cy + bodyH * 0.45f)
        lineTo(cx, cy + bodyH * 0.55f)
        close()
    }
    drawPath(knot, color)
    // Нитка
    val thread = Path().apply {
        moveTo(cx, cy + bodyH * 0.55f)
        cubicTo(
            cx + 4f, cy + bodyH * 1.0f,
            cx - 4f, cy + bodyH * 1.5f,
            cx, cy + bodyH * 2.0f
        )
    }
    drawPath(thread, Color.White.copy(alpha = 0.4f), style = Stroke(width = 1.2f))
    // Блик
    drawCircle(Color.White.copy(alpha = 0.35f), bodyW * 0.15f, Offset(cx - bodyW * 0.2f, cy - bodyH * 0.35f))
}

// ======================= ✨ ИСКРЫ =======================
private fun DrawScope.drawSparks(particles: List<Particle>, progress: Float) {
    particles.forEach { p ->
        val raw = p.y + progress * p.speed
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = (1f - yNorm) * size.height
            if (y < -20f || y > size.height + 20f) continue
            val x = p.x * size.width + sin(progress * 8f + p.phase) * 12f
            val flicker = (sin(progress * 6.28f * 6f + p.phase) + 1f) / 2f
            val r = p.size * 0.3f * (0.6f + 0.4f * flicker)
            // Свечение
            drawCircle(Color(0xFFFFB74D).copy(alpha = 0.35f * flicker), r * 3f, Offset(x, y))
            // Ядро
            drawCircle(Color(0xFFFFE082).copy(alpha = 0.9f * flicker), r, Offset(x, y))
            drawCircle(Color.White.copy(alpha = 0.8f * flicker), r * 0.4f, Offset(x, y))
        }
    }
}

// ======================= 🎊 СЕРПАНТИН =======================
private fun DrawScope.drawStreamers(particles: List<Particle>, progress: Float) {
    val colors = listOf(
        Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFFFFEB3B),
        Color(0xFF4CAF50), Color(0xFF00BCD4), Color(0xFF9C27B0)
    )
    particles.forEachIndexed { idx, p ->
        val raw = p.y + progress * p.speed
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = yNorm * size.height
            if (y < -80f || y > size.height + 80f) continue
            val x = p.x * size.width + sin(progress * 4f + p.phase) * 30f
            val color = colors[idx % colors.size]
            val len = p.size * 3f
            // Волнистая лента из 4 сегментов
            val path = Path().apply {
                moveTo(x, y - len / 2)
                var lastX = x
                var lastY = y - len / 2
                for (s in 1..4) {
                    val segY = y - len / 2 + (len / 4f) * s
                    val wob = sin(progress * 6.28f * 2f + p.phase + s * 0.9f) * p.size * 0.6f
                    val newX = x + wob
                    cubicTo(
                        lastX + wob, lastY + (segY - lastY) * 0.3f,
                        newX - wob, segY - (segY - lastY) * 0.3f,
                        newX, segY
                    )
                    lastX = newX
                    lastY = segY
                }
            }
            drawPath(path, color.copy(alpha = 0.85f), style = Stroke(width = 3f))
        }
    }
}

// ======================= 💎 КРИСТАЛЛЫ =======================
private fun DrawScope.drawCrystals(particles: List<Particle>, progress: Float) {
    val colors = listOf(
        Color(0xFF80DEEA), Color(0xFF9FA8DA), Color(0xFFCE93D8),
        Color(0xFF80CBC4), Color(0xFF90CAF9), Color(0xFFB39DDB)
    )
    particles.forEachIndexed { idx, p ->
        val raw = p.y + progress * p.speed
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = yNorm * size.height
            if (y < -40f || y > size.height + 40f) continue
            val x = p.x * size.width + sin(progress * 5f + p.phase) * 20f
            val rot = (p.rotation + progress * p.rotSpeed) % 360f
            val color = colors[idx % colors.size]
            val s = p.size * 1.3f
            rotate(rot, Offset(x, y)) {
                // Ромбовидный кристалл: 6 граней через треугольники
                val top = Path().apply {
                    moveTo(x, y - s)
                    lineTo(x + s * 0.6f, y - s * 0.3f)
                    lineTo(x, y)
                    lineTo(x - s * 0.6f, y - s * 0.3f)
                    close()
                }
                drawPath(top, color.copy(alpha = 0.9f))

                val bottom = Path().apply {
                    moveTo(x, y + s)
                    lineTo(x + s * 0.6f, y + s * 0.3f)
                    lineTo(x, y)
                    lineTo(x - s * 0.6f, y + s * 0.3f)
                    close()
                }
                drawPath(bottom, color.copy(alpha = 0.6f))

                // Блик
                drawPath(top, Color.White.copy(alpha = 0.3f), style = Stroke(width = 1.5f))
            }
        }
    }
}

// ======================= 💰 МОНЕТЫ =======================
private fun DrawScope.drawCoins(particles: List<Particle>, progress: Float) {
    particles.forEach { p ->
        val raw = p.y + progress * p.speed
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = yNorm * size.height
            if (y < -30f || y > size.height + 30f) continue
            val x = p.x * size.width + sin(progress * 4f + p.phase) * 18f
            val rot = (p.rotation + progress * p.rotSpeed * 3f) % 360f
            // Вращение вокруг вертикальной оси (эффект «переворота»)
            val scaleX = cos(progress * 6.28f * 3f + p.phase).coerceAtLeast(0.15f)
            val s = p.size * 1.4f
            rotate(rot * 0.1f, Offset(x, y)) {
                // Внешний золотой круг
                drawOval(
                    color = Color(0xFFFFC107),
                    topLeft = Offset(x - s * scaleX, y - s),
                    size = Size(s * 2 * scaleX, s * 2)
                )
                // Внутренний тёмный круг
                drawOval(
                    color = Color(0xFFFFA000),
                    topLeft = Offset(x - s * 0.7f * scaleX, y - s * 0.7f),
                    size = Size(s * 1.4f * scaleX, s * 1.4f)
                )
                // Блик
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = s * 0.2f * scaleX.coerceAtMost(1f),
                    center = Offset(x - s * 0.2f * scaleX, y - s * 0.3f)
                )
            }
        }
    }
}
