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
        "FIRE" -> 2400
        "FIRE_CANDLES" -> 3000
        "STARS" -> 10000
        "FISH" -> 8000
        "BUTTERFLIES" -> 9000
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
        List(20) {
            Particle(
                x = rnd.nextFloat(),
                y = rnd.nextFloat(),
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
                x = rnd.nextFloat(),
                y = rnd.nextFloat(),
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
            "FIRE_CANDLES" -> drawFireCandles(progress)
            "SNOW" -> drawSnowVariety(smallParticles + bigParticles, progress)
            "STARS" -> drawStars(smallParticles, progress)
            "LEAVES" -> drawLeaves(smallParticles + bigParticles, progress)
            "FLOWERS" -> drawFlowers(smallParticles + bigParticles, progress)
            "FISH" -> drawFish(smallParticles, progress)
            "BUTTERFLIES" -> drawButterflies(smallParticles, progress)
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

// ============ ПУЗЫРЬКИ ============
private fun DrawScope.drawBubbles(particles: List<Particle>, progress: Float, tint: Color) {
    particles.forEach { p ->
        // Полный цикл: y = p.y + progress (без mod, чтобы не было прыжка)
        // Рисуем только те, что в видимой зоне
        for (cycle in 0..1) {
            val rawY = p.y + progress + cycle.toFloat()
            val y = (1f - rawY) * size.height  // снизу вверх
            if (y < -30f || y > size.height + 30f) continue
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
}

// ============ ОГОНЬ (реалистичная стена) ============
private fun DrawScope.drawFireRealistic(progress: Float) {
    val baseY = size.height

    // Общее тёплое свечение снизу
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x1AFF2200),
                Color(0x44FF5500),
                Color(0x66FF6600)
            ),
            startY = baseY - size.height * 0.85f,
            endY = baseY
        ),
        topLeft = Offset(0f, baseY - size.height * 0.85f),
        size = Size(size.width, size.height * 0.85f)
    )

    // 3 слоя мелких языков
    drawFireLayer(progress, layer = 0, count = 26, maxHeightFrac = 0.85f,
        colorTop = 0x00FF1100, colorMid = 0xAAFF3300, colorBottom = 0xFFFF4400)
    drawFireLayer(progress, layer = 1, count = 22, maxHeightFrac = 0.60f,
        colorTop = 0x00FF7700, colorMid = 0xBBFF9900, colorBottom = 0xFFFFAA00)
    drawFireLayer(progress, layer = 2, count = 16, maxHeightFrac = 0.35f,
        colorTop = 0x00FFDD00, colorMid = 0xCCFFEE00, colorBottom = 0xFFFFF499)

    // Искры-точки на самом низу
    val sparks = 14
    for (i in 0 until sparks) {
        val xFrac = (i + 0.5f) / sparks
        val sx = xFrac * size.width + sin(progress * 6.28f * 2 + i) * 6f
        val phase = i * 1.3f
        val bounce = sin(progress * 6.28f * 4f + phase)
        val sy = baseY - 6f - 5f * bounce.coerceAtLeast(0f)
        val alpha = 0.4f + 0.5f * bounce.coerceAtLeast(0f)
        drawCircle(
            color = Color(0xFFFFEE66).copy(alpha = alpha),
            radius = 1.8f + bounce.coerceAtLeast(0f),
            center = Offset(sx, sy)
        )
    }
}

private fun DrawScope.drawFireLayer(
    progress: Float,
    layer: Int,
    count: Int,
    maxHeightFrac: Float,
    colorTop: Long,
    colorMid: Long,
    colorBottom: Long
) {
    val baseY = size.height
    val maxHeight = size.height * maxHeightFrac

    for (i in 0 until count) {
        // Каждый язычок имеет свою x-позицию с небольшим перекрытием
        val xFrac = (i + 0.5f) / count
        // Разная высота для каждого язычка
        val heightSeed = sin(i * 12.9898f) * 43758.5453f
        val heightNorm = (heightSeed - heightSeed.toInt() + 1f) % 1f
        val baseHeight = maxHeight * (0.55f + 0.45f * heightNorm)
        // Фазы для анимации
        val p1 = i * 0.7f + layer * 1.3f
        val p2 = i * 1.1f + layer * 0.7f
        // Колебания
        val flicker = sin(progress * 6.28f * 3f + p1)
        val sway = sin(progress * 6.28f * 1.8f + p2) * (8f + layer * 2f)
        val height = baseHeight * (0.9f + 0.1f * flicker)

        val cx = xFrac * size.width
        val halfWidth = size.width / (count * 0.7f)

        // Форма язычка — плавная капля, заострённая сверху
        val path = Path().apply {
            moveTo(cx - halfWidth, baseY)
            cubicTo(
                cx - halfWidth * 0.6f + sway * 0.3f, baseY - height * 0.4f,
                cx - halfWidth * 0.3f + sway * 0.7f, baseY - height * 0.75f,
                cx + sway, baseY - height
            )
            cubicTo(
                cx + halfWidth * 0.3f + sway * 0.7f, baseY - height * 0.75f,
                cx + halfWidth * 0.6f + sway * 0.3f, baseY - height * 0.4f,
                cx + halfWidth, baseY
            )
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(colorTop),
                    Color(colorMid),
                    Color(colorBottom)
                ),
                startY = baseY - height,
                endY = baseY
            )
        )
    }
}

// ============ ОГОНЬ — свечи (старый вариант) ============
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

// ============ СНЕГ (3 вида снежинок) ============
private fun DrawScope.drawSnowVariety(particles: List<Particle>, progress: Float) {
    particles.forEachIndexed { idx, p ->
        // Непрерывное падение — рисуем 2 копии, чтобы не было прыжка
        for (cycle in 0..1) {
            val rawY = p.y + progress * p.speed + cycle.toFloat()
            val y = rawY * size.height
            if (y < -40f || y > size.height + 40f) continue
            val x = p.x * size.width + sin(progress * 4f + p.phase) * 14f
            val rot = (p.rotation + progress * p.rotSpeed) % 360f
            val color = Color.White.copy(alpha = 0.85f)
            // Выбираем узор по индексу
            when (idx % 3) {
                0 -> drawSnowflake6(cx = x, cy = y, radius = p.size, rotation = rot, color = color)
                1 -> drawSnowflakeStar(cx = x, cy = y, radius = p.size, rotation = rot, color = color)
                else -> drawSnowflakeSimple(cx = x, cy = y, radius = p.size, rotation = rot, color = color)
            }
        }
    }
}

// Классическая 6-лучевая
private fun DrawScope.drawSnowflake6(cx: Float, cy: Float, radius: Float, rotation: Float, color: Color) {
    rotate(rotation, Offset(cx, cy)) {
        val strokeW = (radius / 8f).coerceAtLeast(1.2f)
        for (i in 0 until 6) {
            rotate(i * 60f, Offset(cx, cy)) {
                drawLine(color, Offset(cx, cy), Offset(cx, cy - radius), strokeW)
                val branchStartY = cy - radius * 0.7f
                val branchLen = radius * 0.3f
                drawLine(color, Offset(cx, branchStartY),
                    Offset(cx - branchLen, branchStartY - branchLen), strokeW * 0.8f)
                drawLine(color, Offset(cx, branchStartY),
                    Offset(cx + branchLen, branchStartY - branchLen), strokeW * 0.8f)
            }
        }
        drawCircle(color, strokeW * 1.5f, Offset(cx, cy))
    }
}

// Звёздочка — 6 ромбов
private fun DrawScope.drawSnowflakeStar(cx: Float, cy: Float, radius: Float, rotation: Float, color: Color) {
    rotate(rotation, Offset(cx, cy)) {
        val path = Path()
        val r1 = radius
        val r2 = radius * 0.4f
        for (i in 0 until 12) {
            val angle = i * 30f * PI.toFloat() / 180f
            val r = if (i % 2 == 0) r1 else r2
            val x = cx + cos(angle) * r
            val y = cy + sin(angle) * r
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, color = color)
        drawCircle(color, radius * 0.15f, Offset(cx, cy))
    }
}

// Простая — просто 6 точек
private fun DrawScope.drawSnowflakeSimple(cx: Float, cy: Float, radius: Float, rotation: Float, color: Color) {
    rotate(rotation, Offset(cx, cy)) {
        for (i in 0 until 6) {
            val angle = i * 60f * PI.toFloat() / 180f
            val x = cx + cos(angle) * radius * 0.7f
            val y = cy + sin(angle) * radius * 0.7f
            drawCircle(color, radius * 0.28f, Offset(x, y))
        }
        drawCircle(color, radius * 0.2f, Offset(cx, cy))
    }
}

// ============ ЗВЁЗДЫ ============
private fun DrawScope.drawStars(particles: List<Particle>, progress: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0A0F2C), Color(0xFF050818), Color(0xFF000000))
        )
    )

    particles.forEach { p ->
        val fast = sin(progress * 6.28f * 5f + p.phase)
        val slow = sin(progress * 6.28f * 2f + p.phase * 0.7f)
        val twinkle = 0.3f + 0.7f * (0.5f + 0.5f * (fast * 0.6f + slow * 0.4f))
        val r = p.size * 0.28f
        drawCircle(Color(0xFFFFE082).copy(alpha = twinkle * 0.4f), r * 2.5f,
            Offset(p.x * size.width, p.y * size.height))
        drawCircle(Color(0xFFFFF8D0).copy(alpha = twinkle), r * (0.8f + 0.4f * twinkle),
            Offset(p.x * size.width, p.y * size.height))
    }

    val bigStars = listOf(Triple(0.15f, 0.25f, 0.9f), Triple(0.78f, 0.15f, 1.2f), Triple(0.55f, 0.7f, 1.5f))
    bigStars.forEach { (x, y, phase) ->
        val cx = x * size.width
        val cy = y * size.height
        val twinkle = 0.5f + 0.5f * sin(progress * 6.28f * 3f + phase)
        val rayLen = 14f + 6f * twinkle
        val alpha = 0.55f + 0.45f * twinkle
        drawLine(Color(0xFFFFFDE7).copy(alpha = alpha),
            Offset(cx - rayLen, cy), Offset(cx + rayLen, cy), 2.2f)
        drawLine(Color(0xFFFFFDE7).copy(alpha = alpha),
            Offset(cx, cy - rayLen), Offset(cx, cy + rayLen), 2.2f)
        drawCircle(Color.White.copy(alpha = alpha), 3.5f, Offset(cx, cy))
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
            val tailX = x - (endX - startX) * 0.35f
            val tailY = y - (endY - startY) * 0.35f
            val alpha = if (t < 0.2f) t / 0.2f else if (t > 0.8f) (1f - t) / 0.2f else 1f
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent,
                        Color(0xAAFFF8D0).copy(alpha = alpha),
                        Color.White.copy(alpha = alpha)),
                    start = Offset(tailX, tailY), end = Offset(x, y)
                ),
                start = Offset(tailX, tailY), end = Offset(x, y), strokeWidth = 3.5f
            )
            drawCircle(Color.White.copy(alpha = alpha), 4.5f, Offset(x, y))
            drawCircle(Color(0xFFFFF8D0).copy(alpha = 0.4f * alpha), 9f, Offset(x, y))
        }
    }
}

private data class ShootingStar(
    val startT: Float, val endT: Float,
    val x1: Float, val y1: Float, val x2: Float, val y2: Float
)

// ============ ОСЕННИЕ ЛИСТЬЯ ============
private fun DrawScope.drawLeaves(particles: List<Particle>, progress: Float) {
    val autumnColors = listOf(
        Color(0xFFE65100), Color(0xFFFF8F00), Color(0xFFD84315),
        Color(0xFFFFB300), Color(0xFFBF360C), Color(0xFFF57C00)
    )
    particles.forEachIndexed { idx, p ->
        // Двойное рисование для плавности
        for (cycle in 0..1) {
            val rawY = p.y + progress * p.speed + cycle.toFloat()
            val y = rawY * size.height
            if (y < -50f || y > size.height + 50f) continue
            val x = p.x * size.width + cos(progress * 5f + p.phase) * 26f
            val rot = (p.rotation + progress * p.rotSpeed) % 360f
            val color = autumnColors[idx % autumnColors.size]
            drawLeaf(x, y, p.size, rot, color.copy(alpha = 0.9f))
        }
    }
}

private fun DrawScope.drawLeaf(cx: Float, cy: Float, size: Float, rotation: Float, color: Color) {
    rotate(rotation, Offset(cx, cy)) {
        val w = size * 0.85f
        val h = size * 1.4f
        val path = Path().apply {
            moveTo(cx, cy - h)
            cubicTo(cx + w, cy - h * 0.5f, cx + w * 0.9f, cy + h * 0.3f,
                cx + w * 0.3f, cy + h * 0.8f)
            quadraticBezierTo(cx, cy + h, cx - w * 0.3f, cy + h * 0.8f)
            cubicTo(cx - w * 0.9f, cy + h * 0.3f, cx - w, cy - h * 0.5f,
                cx, cy - h)
            close()
        }
        drawPath(path, color)
        drawLine(Color(0x66FFFFFF), Offset(cx, cy - h * 0.85f),
            Offset(cx, cy + h * 0.85f), 1.5f)
        for (i in 1..2) {
            val frac = i * 0.25f
            val py = cy - h * 0.5f + h * frac
            drawLine(Color(0x44FFFFFF), Offset(cx, py),
                Offset(cx - w * 0.5f, py + h * 0.15f), 1f)
            drawLine(Color(0x44FFFFFF), Offset(cx, py),
                Offset(cx + w * 0.5f, py + h * 0.15f), 1f)
        }
    }
}

// ============ ЦВЕТЫ (переделаны) ============
private fun DrawScope.drawFlowers(particles: List<Particle>, progress: Float) {
    val flowerColors = listOf(
        Color(0xFFE91E63), Color(0xFFF06292), Color(0xFF9C27B0),
        Color(0xFFFF5722), Color(0xFFE1BEE7), Color(0xFFFF7043)
    )
    particles.forEachIndexed { idx, p ->
        for (cycle in 0..1) {
            val rawY = p.y + progress * p.speed + cycle.toFloat()
            val y = rawY * size.height
            if (y < -50f || y > size.height + 50f) continue
            val x = p.x * size.width + cos(progress * 5f + p.phase) * 26f
            val rot = (p.rotation + progress * p.rotSpeed * 0.5f) % 360f
            val color = flowerColors[idx % flowerColors.size]
            drawFlowerRealistic(x, y, p.size, rot, color.copy(alpha = 0.9f))
        }
    }
}

private fun DrawScope.drawFlowerRealistic(cx: Float, cy: Float, size: Float, rotation: Float, petalColor: Color) {
    rotate(rotation, Offset(cx, cy)) {
        val petalCount = 6
        val petalDistance = size * 0.65f
        val petalRadius = size * 0.55f
        // Лепестки-кружки вокруг центра
        for (i in 0 until petalCount) {
            val angle = i * (360f / petalCount) * PI.toFloat() / 180f
            val px = cx + cos(angle) * petalDistance
            val py = cy + sin(angle) * petalDistance
            drawCircle(petalColor, petalRadius, Offset(px, py))
            // Блик на лепестке
            drawCircle(Color.White.copy(alpha = 0.25f),
                petalRadius * 0.4f,
                Offset(px - petalRadius * 0.2f, py - petalRadius * 0.2f))
        }
        // Серединка
        drawCircle(Color(0xFFFFEB3B), size * 0.42f, Offset(cx, cy))
        // Крапинки на серединке
        for (i in 0 until 5) {
            val a = i * 72f * PI.toFloat() / 180f
            val dx = cos(a) * size * 0.18f
            val dy = sin(a) * size * 0.18f
            drawCircle(Color(0xFFFFA000), size * 0.06f, Offset(cx + dx, cy + dy))
        }
    }
}

// ============ РЫБКИ ============
private fun DrawScope.drawFish(particles: List<Particle>, progress: Float) {
    // Фон — подводный градиент
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A2A4A),
                Color(0xFF041A33),
                Color(0xFF001024)
            )
        )
    )

    particles.forEachIndexed { idx, p ->
        // Плывут слева направо, с покачиванием
        val xBase = p.x * size.width
        val xOffset = (progress * p.speed + p.phase / 6.28f) % 1f
        val x = ((xBase + xOffset * size.width) % (size.width + 100f)) - 50f
        val y = p.y * size.height + sin(progress * 6.28f * 2 + p.phase) * 6f
        val color = when (idx % 4) {
            0 -> Color(0xFFFF7043)
            1 -> Color(0xFFFFCA28)
            2 -> Color(0xFF42A5F5)
            else -> Color(0xFFAB47BC)
        }
        val facingRight = true
        drawFishShape(x, y, p.size, color.copy(alpha = 0.85f), facingRight)
    }
}

private fun DrawScope.drawFishShape(cx: Float, cy: Float, size: Float, color: Color, facingRight: Boolean) {
    // Тело — овал
    val bodyW = size * 1.6f
    val bodyH = size * 0.9f
    // Хвост — треугольник
    if (facingRight) {
        drawOval(color, topLeft = Offset(cx - bodyW * 0.5f, cy - bodyH * 0.5f),
            size = Size(bodyW, bodyH))
        // Хвост
        val path = Path().apply {
            moveTo(cx - bodyW * 0.5f, cy)
            lineTo(cx - bodyW * 0.5f - size * 0.7f, cy - size * 0.5f)
            lineTo(cx - bodyW * 0.5f - size * 0.7f, cy + size * 0.5f)
            close()
        }
        drawPath(path, color)
        // Глаз
        drawCircle(Color.White, size * 0.14f, Offset(cx + bodyW * 0.25f, cy - size * 0.1f))
        drawCircle(Color.Black, size * 0.07f, Offset(cx + bodyW * 0.27f, cy - size * 0.1f))
    } else {
        drawOval(color, topLeft = Offset(cx - bodyW * 0.5f, cy - bodyH * 0.5f),
            size = Size(bodyW, bodyH))
        val path = Path().apply {
            moveTo(cx + bodyW * 0.5f, cy)
            lineTo(cx + bodyW * 0.5f + size * 0.7f, cy - size * 0.5f)
            lineTo(cx + bodyW * 0.5f + size * 0.7f, cy + size * 0.5f)
            close()
        }
        drawPath(path, color)
        drawCircle(Color.White, size * 0.14f, Offset(cx - bodyW * 0.25f, cy - size * 0.1f))
        drawCircle(Color.Black, size * 0.07f, Offset(cx - bodyW * 0.27f, cy - size * 0.1f))
    }
}

// ============ БАБОЧКИ ============
private fun DrawScope.drawButterflies(particles: List<Particle>, progress: Float) {
    val butterflyColors = listOf(
        Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFFFF9800),
        Color(0xFF3F51B5), Color(0xFF00BCD4), Color(0xFFFFC107)
    )
    particles.forEachIndexed { idx, p ->
        // Плавно летают по кругу/эллипсу
        val baseX = p.x * size.width
        val baseY = p.y * size.height
        val angle = progress * 6.28f * p.speed + p.phase
        val radiusX = 30f + p.size * 1.5f
        val radiusY = 20f + p.size * 1.2f
        val x = baseX + cos(angle) * radiusX
        val y = baseY + sin(angle * 1.3f) * radiusY
        // Взмах крыльев — периодический
        val flap = (sin(progress * 6.28f * 6f + p.phase) + 1f) / 2f
        val color = butterflyColors[idx % butterflyColors.size]
        drawButterfly(x, y, p.size, flap, color.copy(alpha = 0.9f))
    }
}

private fun DrawScope.drawButterfly(cx: Float, cy: Float, size: Float, flap: Float, color: Color) {
    // flap = 0 — крылья раскрыты, flap = 1 — сложены
    val wingScale = 1f - flap * 0.7f
    val wingH = size * 1.2f * wingScale
    val wingW = size * 1.0f

    // Левое верхнее крыло
    drawOval(color,
        topLeft = Offset(cx - wingW * 0.9f, cy - wingH * 0.6f),
        size = Size(wingW, wingH * 0.9f))
    // Правое верхнее
    drawOval(color,
        topLeft = Offset(cx - wingW * 0.1f, cy - wingH * 0.6f),
        size = Size(wingW, wingH * 0.9f))
    // Левое нижнее (поменьше)
    drawOval(color.copy(alpha = 0.85f),
        topLeft = Offset(cx - wingW * 0.75f, cy - wingH * 0.1f),
        size = Size(wingW * 0.85f, wingH * 0.7f))
    // Правое нижнее
    drawOval(color.copy(alpha = 0.85f),
        topLeft = Offset(cx - wingW * 0.1f, cy - wingH * 0.1f),
        size = Size(wingW * 0.85f, wingH * 0.7f))
    // Тело
    drawOval(Color(0xFF2C1810),
        topLeft = Offset(cx - size * 0.08f, cy - size * 0.55f),
        size = Size(size * 0.16f, size * 1.1f))
    // Усики
    drawLine(Color(0xFF2C1810), Offset(cx, cy - size * 0.55f),
        Offset(cx - size * 0.25f, cy - size * 1.0f), 1.2f)
    drawLine(Color(0xFF2C1810), Offset(cx, cy - size * 0.55f),
        Offset(cx + size * 0.25f, cy - size * 1.0f), 1.2f)
}

// ============ ВОЛНЫ ============
private fun DrawScope.drawWavesSimple(progress: Float, tint: Color) {
    // Периодическая функция — при progress=0 и progress=1 значения совпадают
    for (layer in 0..2) {
        val amplitude = 8f + layer * 5f
        val yBase = size.height - (size.height * 0.20f) + layer * 12f
        val alpha = 0.12f - layer * 0.03f
        val path = Path()
        path.moveTo(0f, yBase)
        var x = 0f
        while (x <= size.width) {
            // 4 полных периода за цикл → совпадение в начале и конце
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
            drawCircle(Color.White.copy(alpha = alpha * 0.6f),
                1.5f + alpha * 1.5f, Offset(fx, y - 2f))
        }
    }
}
