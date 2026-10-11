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
        "FIRE_CANDLES" -> 3000
        "STARS" -> 10000
        "WAVES", "WAVES_FOAM" -> 5000
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
        List(24) {
            Particle(
                x = rnd.nextFloat(),
                y = rnd.nextFloat(),
                size = 4f + rnd.nextFloat() * 8f,
                phase = rnd.nextFloat() * 2f * PI.toFloat(),
                speed = 0.5f + rnd.nextFloat() * 0.7f,
                rotation = rnd.nextFloat() * 360f,
                rotSpeed = 20f + rnd.nextFloat() * 60f
            )
        }
    }

    val bigParticles = remember {
        List(6) {
            Particle(
                x = rnd.nextFloat(),
                y = rnd.nextFloat(),
                size = 14f + rnd.nextFloat() * 10f,
                phase = rnd.nextFloat() * 2f * PI.toFloat(),
                speed = 0.4f + rnd.nextFloat() * 0.5f,
                rotation = rnd.nextFloat() * 360f,
                rotSpeed = 15f + rnd.nextFloat() * 30f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        when (type) {
            "BUBBLES" -> drawBubbles(smallParticles, progress, tint)
            "FIRE" -> drawFireWall(progress)
            "FIRE_CANDLES" -> drawFireCandles(progress)
            "SNOW" -> drawSnow(smallParticles + bigParticles, progress, tint)
            "STARS" -> drawStars(smallParticles, progress)
            "LEAVES" -> drawLeaves(smallParticles + bigParticles, progress)
            "FLOWERS" -> drawFlowers(smallParticles + bigParticles, progress)
            "WAVES" -> drawWavesSimple(progress, tint)
            "WAVES_FOAM" -> drawWavesFoam(progress, tint)
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val phase: Float,
    val speed: Float,
    val rotation: Float,
    val rotSpeed: Float
)

// ======================= ПУЗЫРЬКИ =======================
private fun DrawScope.drawBubbles(particles: List<Particle>, progress: Float, tint: Color) {
    particles.forEach { p ->
        val y = ((p.y + 1f - progress * p.speed) % 1f) * size.height
        val x = p.x * size.width + sin(progress * 6.28f + p.phase) * 14f
        drawCircle(
            color = tint.copy(alpha = 0.18f),
            radius = p.size,
            center = Offset(x, y),
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.15f),
            radius = p.size * 0.35f,
            center = Offset(x - p.size * 0.3f, y - p.size * 0.3f)
        )
    }
}

// ======================= ОГОНЬ — единое полотно =======================
private fun DrawScope.drawFireWall(progress: Float) {
    val baseY = size.height
    val fireAreaHeight = size.height

    // Общее свечение над пламенем
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x22FF2200),
                Color(0x55FF4400)
            ),
            startY = baseY - fireAreaHeight * 0.9f,
            endY = baseY
        ),
        topLeft = Offset(0f, baseY - fireAreaHeight * 0.9f),
        size = Size(size.width, fireAreaHeight * 0.9f)
    )

    // Слой 1 — внешний
    val outerFlames = 14
    for (i in 0 until outerFlames) {
        val xFrac = i.toFloat() / (outerFlames - 1)
        val cx = xFrac * size.width
        val phase = i * 0.7f
        val flicker = sin(progress * 6.28f * 2.5f + phase)
        val wave = sin(progress * 6.28f * 1.8f + phase * 1.3f) * 14f
        val heightVar = when {
            i % 5 == 0 -> 0.85f
            i % 3 == 0 -> 0.65f
            else -> 0.45f + (i % 4) * 0.08f
        }
        val height = fireAreaHeight * heightVar * (0.92f + 0.08f * flicker)
        val halfWidth = size.width / (outerFlames * 0.9f)

        val path = Path().apply {
            moveTo(cx - halfWidth, baseY)
            cubicTo(
                cx - halfWidth * 0.8f + wave, baseY - height * 0.4f,
                cx - halfWidth * 0.4f + wave, baseY - height * 0.75f,
                cx + wave * 0.8f, baseY - height
            )
            cubicTo(
                cx + halfWidth * 0.4f + wave, baseY - height * 0.75f,
                cx + halfWidth * 0.8f + wave, baseY - height * 0.4f,
                cx + halfWidth, baseY
            )
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x00FF2200),
                    Color(0x88FF3300),
                    Color(0xEEFF6600)
                ),
                startY = baseY - height,
                endY = baseY
            )
        )
    }

    // Слой 2 — средний
    val midFlames = 10
    for (i in 0 until midFlames) {
        val xFrac = i.toFloat() / (midFlames - 1)
        val cx = xFrac * size.width
        val phase = i * 0.9f
        val flicker = sin(progress * 6.28f * 3f + phase)
        val wave = sin(progress * 6.28f * 2.2f + phase * 1.5f) * 10f
        val heightVar = 0.35f + (i % 3) * 0.15f
        val height = fireAreaHeight * heightVar * (0.9f + 0.1f * flicker)
        val halfWidth = size.width / (midFlames * 0.85f)

        val path = Path().apply {
            moveTo(cx - halfWidth, baseY)
            cubicTo(
                cx - halfWidth * 0.7f + wave, baseY - height * 0.4f,
                cx - halfWidth * 0.3f + wave, baseY - height * 0.75f,
                cx + wave * 0.7f, baseY - height
            )
            cubicTo(
                cx + halfWidth * 0.3f + wave, baseY - height * 0.75f,
                cx + halfWidth * 0.7f + wave, baseY - height * 0.4f,
                cx + halfWidth, baseY
            )
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x00FF5500),
                    Color(0xBBFF8800),
                    Color(0xFFFFAA00)
                ),
                startY = baseY - height,
                endY = baseY
            )
        )
    }

    // Слой 3 — внутренний
    val innerFlames = 8
    for (i in 0 until innerFlames) {
        val xFrac = i.toFloat() / (innerFlames - 1)
        val cx = xFrac * size.width
        val phase = i * 1.1f
        val flicker = sin(progress * 6.28f * 3.5f + phase)
        val wave = sin(progress * 6.28f * 2.5f + phase * 1.4f) * 8f
        val heightVar = 0.20f + (i % 2) * 0.10f
        val height = fireAreaHeight * heightVar * (0.88f + 0.12f * flicker)
        val halfWidth = size.width / (innerFlames * 0.8f)

        val path = Path().apply {
            moveTo(cx - halfWidth, baseY)
            cubicTo(
                cx - halfWidth * 0.6f + wave, baseY - height * 0.5f,
                cx - halfWidth * 0.3f + wave, baseY - height * 0.8f,
                cx + wave * 0.6f, baseY - height
            )
            cubicTo(
                cx + halfWidth * 0.3f + wave, baseY - height * 0.8f,
                cx + halfWidth * 0.6f + wave, baseY - height * 0.5f,
                cx + halfWidth, baseY
            )
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x00FFD700),
                    Color(0xCCFFCC00),
                    Color(0xFFFFEE88)
                ),
                startY = baseY - height,
                endY = baseY
            )
        )
    }

    // Искры
    val sparks = 10
    for (i in 0 until sparks) {
        val xFrac = (i + 0.5f) / sparks
        val sx = xFrac * size.width
        val phase = i * 1.3f
        val bounce = sin(progress * 6.28f * 4f + phase)
        val sy = baseY - 8f - 4f * bounce.coerceAtLeast(0f)
        val alpha = 0.4f + 0.5f * bounce.coerceAtLeast(0f)
        drawCircle(
            color = Color(0xFFFFEE66).copy(alpha = alpha),
            radius = 1.8f + bounce.coerceAtLeast(0f),
            center = Offset(sx, sy)
        )
    }
}

// ======================= ОГОНЬ — свечи (старый вариант) =======================
private fun DrawScope.drawFireCandles(progress: Float) {
    val flameCount = 4
    val baseY = size.height

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x33FF4400),
                Color(0x66FF2200)
            ),
            startY = baseY - size.height * 0.7f,
            endY = baseY
        ),
        topLeft = Offset(0f, baseY - size.height * 0.7f),
        size = Size(size.width, size.height * 0.7f)
    )

    for (i in 0 until flameCount) {
        val cx = size.width * (i + 0.5f) / flameCount
        val phase = i * 1.3f
        val flicker = sin(progress * 6.28f * 3 + phase)
        val wave = sin(progress * 6.28f * 2 + phase) * 12f
        val height = size.height * (0.45f + 0.18f * flicker)

        val outerPath = Path().apply {
            moveTo(cx - 22f, baseY)
            cubicTo(
                cx - 30f + wave, baseY - height * 0.35f,
                cx - 12f + wave, baseY - height * 0.7f,
                cx + wave, baseY - height
            )
            cubicTo(
                cx + 12f + wave, baseY - height * 0.7f,
                cx + 30f + wave, baseY - height * 0.35f,
                cx + 22f, baseY
            )
            close()
        }
        drawPath(
            path = outerPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x00FF2200),
                    Color(0xAAFF3300),
                    Color(0xEEFF5500)
                ),
                startY = baseY - height,
                endY = baseY
            )
        )

        val innerPath = Path().apply {
            moveTo(cx - 12f, baseY)
            cubicTo(
                cx - 16f + wave * 0.7f, baseY - height * 0.35f,
                cx - 6f + wave * 0.7f, baseY - height * 0.6f,
                cx + wave * 0.7f, baseY - height * 0.8f
            )
            cubicTo(
                cx + 6f + wave * 0.7f, baseY - height * 0.6f,
                cx + 16f + wave * 0.7f, baseY - height * 0.35f,
                cx + 12f, baseY
            )
            close()
        }
        drawPath(
            path = innerPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x00FFD700),
                    Color(0xBBFFAA00),
                    Color(0xFFFFDD33)
                ),
                startY = baseY - height * 0.8f,
                endY = baseY
            )
        )

        val corePath = Path().apply {
            moveTo(cx - 5f, baseY)
            cubicTo(
                cx - 7f + wave * 0.4f, baseY - height * 0.25f,
                cx - 2f + wave * 0.4f, baseY - height * 0.4f,
                cx + wave * 0.4f, baseY - height * 0.5f
            )
            cubicTo(
                cx + 2f + wave * 0.4f, baseY - height * 0.4f,
                cx + 7f + wave * 0.4f, baseY - height * 0.25f,
                cx + 5f, baseY
            )
            close()
        }
        drawPath(
            path = corePath,
            color = Color(0xFFFFEE88).copy(alpha = 0.7f)
        )
    }
}

// ======================= СНЕГ =======================
private fun DrawScope.drawSnow(particles: List<Particle>, progress: Float, tint: Color) {
    particles.forEach { p ->
        val y = ((p.y + progress * p.speed) % 1f) * size.height
        val x = p.x * size.width + sin(progress * 4f + p.phase) * 14f
        val rot = (p.rotation + progress * p.rotSpeed) % 360f
        drawSnowflake(
            cx = x,
            cy = y,
            radius = p.size,
            rotation = rot,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

private fun DrawScope.drawSnowflake(cx: Float, cy: Float, radius: Float, rotation: Float, color: Color) {
    rotate(rotation, Offset(cx, cy)) {
        val strokeW = (radius / 8f).coerceAtLeast(1.2f)
        for (i in 0 until 6) {
            val angle = i * 60f
            rotate(angle, Offset(cx, cy)) {
                val topY = cy - radius
                drawLine(
                    color = color,
                    start = Offset(cx, cy),
                    end = Offset(cx, topY),
                    strokeWidth = strokeW
                )
                val branchStartY = cy - radius * 0.7f
                val branchLen = radius * 0.3f
                drawLine(
                    color = color,
                    start = Offset(cx, branchStartY),
                    end = Offset(cx - branchLen, branchStartY - branchLen),
                    strokeWidth = strokeW * 0.8f
                )
                drawLine(
                    color = color,
                    start = Offset(cx, branchStartY),
                    end = Offset(cx + branchLen, branchStartY - branchLen),
                    strokeWidth = strokeW * 0.8f
                )
            }
        }
        drawCircle(color = color, radius = strokeW * 1.5f, center = Offset(cx, cy))
    }
}

// ======================= ЗВЁЗДЫ =======================
private fun DrawScope.drawStars(particles: List<Particle>, progress: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0F2C),
                Color(0xFF050818),
                Color(0xFF000000)
            )
        )
    )

    particles.forEach { p ->
        val fast = sin(progress * 6.28f * 5f + p.phase)
        val slow = sin(progress * 6.28f * 2f + p.phase * 0.7f)
        val twinkle = 0.3f + 0.7f * (0.5f + 0.5f * (fast * 0.6f + slow * 0.4f))
        val r = p.size * 0.28f

        drawCircle(
            color = Color(0xFFFFE082).copy(alpha = twinkle * 0.4f),
            radius = r * 2.5f,
            center = Offset(p.x * size.width, p.y * size.height)
        )
        drawCircle(
            color = Color(0xFFFFF8D0).copy(alpha = twinkle),
            radius = r * (0.8f + 0.4f * twinkle),
            center = Offset(p.x * size.width, p.y * size.height)
        )
    }

    val bigStars = listOf(
        Triple(0.15f, 0.25f, 0.9f),
        Triple(0.78f, 0.15f, 1.2f),
        Triple(0.55f, 0.7f, 1.5f)
    )
    bigStars.forEach { (x, y, phase) ->
        val cx = x * size.width
        val cy = y * size.height
        val twinkle = 0.5f + 0.5f * sin(progress * 6.28f * 3f + phase)
        val rayLen = 14f + 6f * twinkle
        val alpha = 0.55f + 0.45f * twinkle
        drawLine(
            color = Color(0xFFFFFDE7).copy(alpha = alpha),
            start = Offset(cx - rayLen, cy),
            end = Offset(cx + rayLen, cy),
            strokeWidth = 2.2f
        )
        drawLine(
            color = Color(0xFFFFFDE7).copy(alpha = alpha),
            start = Offset(cx, cy - rayLen),
            end = Offset(cx, cy + rayLen),
            strokeWidth = 2.2f
        )
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = 3.5f,
            center = Offset(cx, cy)
        )
    }

    val shootingStars = listOf(
        ShootingStar(0.05f, 0.25f, 0.05f, 0.10f, 0.85f, 0.55f),
        ShootingStar(0.40f, 0.55f, 0.95f, 0.15f, 0.25f, 0.65f),
        ShootingStar(0.70f, 0.85f, 0.10f, 0.20f, 0.90f, 0.50f)
    )

    shootingStars.forEach { ss ->
        if (progress in ss.startT..ss.endT) {
            val t = (progress - ss.startT) / (ss.endT - ss.startT)
            val startX = ss.x1 * size.width
            val startY = ss.y1 * size.height
            val endX = ss.x2 * size.width
            val endY = ss.y2 * size.height
            val x = startX + (endX - startX) * t
            val y = startY + (endY - startY) * t

            val tailFrac = 0.35f
            val tailX = x - (endX - startX) * tailFrac
            val tailY = y - (endY - startY) * tailFrac

            val alpha = if (t < 0.2f) t / 0.2f else if (t > 0.8f) (1f - t) / 0.2f else 1f

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xAAFFF8D0).copy(alpha = alpha),
                        Color.White.copy(alpha = alpha)
                    ),
                    start = Offset(tailX, tailY),
                    end = Offset(x, y)
                ),
                start = Offset(tailX, tailY),
                end = Offset(x, y),
                strokeWidth = 3.5f
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 4.5f,
                center = Offset(x, y)
            )
            drawCircle(
                color = Color(0xFFFFF8D0).copy(alpha = 0.4f * alpha),
                radius = 9f,
                center = Offset(x, y)
            )
        }
    }
}

private data class ShootingStar(
    val startT: Float,
    val endT: Float,
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
)

// ======================= ОСЕННИЕ ЛИСТЬЯ =======================
private fun DrawScope.drawLeaves(particles: List<Particle>, progress: Float) {
    val autumnColors = listOf(
        Color(0xFFE65100),
        Color(0xFFFF8F00),
        Color(0xFFD84315),
        Color(0xFFFFB300),
        Color(0xFFBF360C),
        Color(0xFFF57C00)
    )
    particles.forEachIndexed { idx, p ->
        val y = ((p.y + progress * p.speed) % 1f) * size.height
        val x = p.x * size.width + cos(progress * 5f + p.phase) * 26f
        val rot = (p.rotation + progress * p.rotSpeed) % 360f
        val color = autumnColors[idx % autumnColors.size]
        drawLeaf(
            cx = x,
            cy = y,
            size = p.size,
            rotation = rot,
            color = color.copy(alpha = 0.9f)
        )
    }
}

private fun DrawScope.drawLeaf(cx: Float, cy: Float, size: Float, rotation: Float, color: Color) {
    rotate(rotation, Offset(cx, cy)) {
        val w = size * 0.85f
        val h = size * 1.4f
        val path = Path().apply {
            moveTo(cx, cy - h)
            cubicTo(
                cx + w, cy - h * 0.5f,
                cx + w * 0.9f, cy + h * 0.3f,
                cx + w * 0.3f, cy + h * 0.8f
            )
            quadraticBezierTo(
                cx, cy + h,
                cx - w * 0.3f, cy + h * 0.8f
            )
            cubicTo(
                cx - w * 0.9f, cy + h * 0.3f,
                cx - w, cy - h * 0.5f,
                cx, cy - h
            )
            close()
        }
        drawPath(path = path, color = color)

        drawLine(
            color = Color(0x66FFFFFF),
            start = Offset(cx, cy - h * 0.85f),
            end = Offset(cx, cy + h * 0.85f),
            strokeWidth = 1.5f
        )
        for (i in 1..2) {
            val frac = i * 0.25f
            val py = cy - h * 0.5f + h * frac
            drawLine(
                color = Color(0x44FFFFFF),
                start = Offset(cx, py),
                end = Offset(cx - w * 0.5f, py + h * 0.15f),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0x44FFFFFF),
                start = Offset(cx, py),
                end = Offset(cx + w * 0.5f, py + h * 0.15f),
                strokeWidth = 1f
            )
        }
    }
}

// ======================= ЦВЕТЫ =======================
private fun DrawScope.drawFlowers(particles: List<Particle>, progress: Float) {
    val flowerColors = listOf(
        Color(0xFFE91E63),
        Color(0xFFF06292),
        Color(0xFF9C27B0),
        Color(0xFFFF5722),
        Color(0xFFE1BEE7),
        Color(0xFFFF7043)
    )
    particles.forEachIndexed { idx, p ->
        val y = ((p.y + progress * p.speed) % 1f) * size.height
        val x = p.x * size.width + cos(progress * 5f + p.phase) * 26f
        val rot = (p.rotation + progress * p.rotSpeed * 0.5f) % 360f
        val color = flowerColors[idx % flowerColors.size]
        drawFlower(
            cx = x,
            cy = y,
            size = p.size,
            rotation = rot,
            petalColor = color.copy(alpha = 0.9f)
        )
    }
}

private fun DrawScope.drawFlower(cx: Float, cy: Float, size: Float, rotation: Float, petalColor: Color) {
    rotate(rotation, Offset(cx, cy)) {
        val petalCount = 5
        val petalRadius = size * 0.6f
        for (i in 0 until petalCount) {
            val angle = i * (360f / petalCount)
            rotate(angle, Offset(cx, cy)) {
                drawOval(
                    color = petalColor,
                    topLeft = Offset(cx - petalRadius * 0.45f, cy - petalRadius * 1.5f),
                    size = Size(petalRadius * 0.9f, petalRadius * 0.9f)
                )
            }
        }
        drawCircle(
            color = Color(0xFFFFEB3B),
            radius = size * 0.22f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFFFFC107),
            radius = size * 0.10f,
            center = Offset(cx - size * 0.06f, cy - size * 0.05f)
        )
        drawCircle(
            color = Color(0xFFFFC107),
            radius = size * 0.08f,
            center = Offset(cx + size * 0.07f, cy + size * 0.06f)
        )
    }
}

// ======================= ВОЛНЫ (простой) =======================
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

// ======================= ВОЛНЫ С ПЕНОЙ =======================
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
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.6f),
                radius = 1.5f + alpha * 1.5f,
                center = Offset(fx, y - 2f)
            )
        }
    }
}
