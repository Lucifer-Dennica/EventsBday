// ======================= 🎆 САЛЮТ (переделано) =======================
private fun DrawScope.drawFireworks(progress: Float, tint: Color) {
    val bursts = 5
    val colorSets = listOf(
        listOf(Color(0xFFFF5252), Color(0xFFFF8A80), Color(0xFFFF1744)), // красный
        listOf(Color(0xFFFFD740), Color(0xFFFFE57F), Color(0xFFFFC400)), // жёлтый
        listOf(Color(0xFF69F0AE), Color(0xFFB9F6CA), Color(0xFF00E676)), // зелёный
        listOf(Color(0xFF40C4FF), Color(0xFF80D8FF), Color(0xFF00B0FF)), // голубой
        listOf(Color(0xFFE040FB), Color(0xFFEA80FC), Color(0xFFD500F9))  // фиолетовый
    )

    for (b in 0 until bursts) {
        val burstPhase = ((progress + b * 0.2f) % 1f)

        // Разгорается 0–0.1, горит 0.1–0.5, тухнет 0.5–1.0
        val intensity = when {
            burstPhase < 0.1f -> burstPhase / 0.1f
            burstPhase < 0.5f -> 1f
            else -> (1f - (burstPhase - 0.5f) / 0.5f).coerceAtLeast(0f)
        }
        if (intensity <= 0.01f) continue

        // Расширение: в начале быстрый разлёт, затем замедление
        val expansion = if (burstPhase < 0.5f) {
            val t = burstPhase / 0.5f
            1f - (1f - t) * (1f - t) // ease-out
        } else 1f

        val cx = size.width * ((b * 0.37f + 0.13f) % 1f)
        val cy = size.height * ((b * 0.53f + 0.22f) % 0.6f + 0.18f)
        val colors = colorSets[b % colorSets.size]
        val maxRadius = (70f + (b % 3) * 20f) * expansion
        val radius = maxRadius * intensity

        // Тип залпа: 0=звезда, 1=кольцо, 2=ромб, 3=хаос, 4=сердце
        when (b % 5) {
            0 -> drawFireworkStar(cx, cy, radius, intensity, colors)
            1 -> drawFireworkRing(cx, cy, radius, intensity, colors)
            2 -> drawFireworkDiamond(cx, cy, radius, intensity, colors)
            3 -> drawFireworkChaos(cx, cy, radius, intensity, colors, b)
            4 -> drawFireworkHeart(cx, cy, radius, intensity, colors)
        }
    }
}

/** Многослойное ядро + внешнее гало — общее для всех залпов */
private fun DrawScope.drawFireworkCore(cx: Float, cy: Float, intensity: Float, color: Color) {
    // Дальнее гало
    drawCircle(color = color.copy(alpha = intensity * 0.15f), radius = 28f * intensity, center = Offset(cx, cy))
    drawCircle(color = color.copy(alpha = intensity * 0.3f), radius = 14f * intensity, center = Offset(cx, cy))
    // Белое горячее ядро
    drawCircle(color = Color.White.copy(alpha = intensity * 0.95f), radius = 3.5f + 3f * intensity, center = Offset(cx, cy))
    // Цветной ободок
    drawCircle(color = color.copy(alpha = intensity * 0.7f), radius = 6f * intensity, center = Offset(cx, cy))
}

/** Залп-звезда: 12 лучей с градиентом по длине */
private fun DrawScope.drawFireworkStar(cx: Float, cy: Float, radius: Float, intensity: Float, colors: List<Color>) {
    drawFireworkCore(cx, cy, intensity, colors[0])
    val rays = 12
    for (i in 0 until rays) {
        val angle = i * (360f / rays) * PI.toFloat() / 180f
        val len = radius * (0.85f + 0.15f * sin(i * 1.3f))
        val x1 = cx + cos(angle) * radius * 0.2f
        val y1 = cy + sin(angle) * radius * 0.2f
        val x2 = cx + cos(angle) * len
        val y2 = cy + sin(angle) * len
        // Основной луч
        drawLine(
            color = colors[0].copy(alpha = intensity * 0.9f),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = 2.5f
        )
        // Кончик луча — точка с ярким цветом
        drawCircle(
            color = colors[1].copy(alpha = intensity * 0.8f),
            radius = 1.8f,
            center = Offset(x2, y2)
        )
    }
}

/** Залп-кольцо: точки по кругу */
private fun DrawScope.drawFireworkRing(cx: Float, cy: Float, radius: Float, intensity: Float, colors: List<Color>) {
    drawFireworkCore(cx, cy, intensity, colors[0])
    val dots = 18
    for (i in 0 until dots) {
        val angle = i * (360f / dots) * PI.toFloat() / 180f
        val x = cx + cos(angle) * radius
        val y = cy + sin(angle) * radius
        val dotSize = 2.2f + 1.2f * (i % 2)
        drawCircle(color = colors[i % colors.size].copy(alpha = intensity * 0.85f), radius = dotSize, center = Offset(x, y))
    }
    // Внутреннее кольцо
    val innerR = radius * 0.55f
    for (i in 0 until 10) {
        val angle = (i * 36f + 18f) * PI.toFloat() / 180f
        val x = cx + cos(angle) * innerR
        val y = cy + sin(angle) * innerR
        drawCircle(color = colors[1].copy(alpha = intensity * 0.55f), radius = 1.5f, center = Offset(x, y))
    }
}

/** Залп-ромб: 4 основных луча + 4 вспомогательных */
private fun DrawScope.drawFireworkDiamond(cx: Float, cy: Float, radius: Float, intensity: Float, colors: List<Color>) {
    drawFireworkCore(cx, cy, intensity, colors[0])
    val angles = listOf(0f, 90f, 180f, 270f)
    angles.forEach { deg ->
        val a = deg * PI.toFloat() / 180f
        val x = cx + cos(a) * radius
        val y = cy + sin(a) * radius
        drawLine(
            color = colors[0].copy(alpha = intensity * 0.9f),
            start = Offset(cx, cy),
            end = Offset(x, y),
            strokeWidth = 3f
        )
    }
    // Диагональные — короче
    val diagAngles = listOf(45f, 135f, 225f, 315f)
    diagAngles.forEach { deg ->
        val a = deg * PI.toFloat() / 180f
        val x = cx + cos(a) * radius * 0.6f
        val y = cy + sin(a) * radius * 0.6f
        drawLine(
            color = colors[1].copy(alpha = intensity * 0.7f),
            start = Offset(cx, cy),
            end = Offset(x, y),
            strokeWidth = 2f
        )
    }
}

/** Залп-хаос: случайно разлетающиеся частицы */
private fun DrawScope.drawFireworkChaos(cx: Float, cy: Float, radius: Float, intensity: Float, colors: List<Color>, seed: Int) {
    drawFireworkCore(cx, cy, intensity, colors[0])
    val particles = 24
    for (i in 0 until particles) {
        val angle = (i * 37f + seed * 11f) % 360f * PI.toFloat() / 180f
        val dist = radius * (0.4f + ((i * 13) % 100) / 100f * 0.6f)
        val x = cx + cos(angle) * dist
        val y = cy + sin(angle) * dist
        val size = 1.5f + ((i * 7) % 100) / 100f * 2f
        drawCircle(color = colors[i % colors.size].copy(alpha = intensity * 0.85f), radius = size, center = Offset(x, y))
    }
}

/** Залп-сердце: частицы по контуру сердца */
private fun DrawScope.drawFireworkHeart(cx: Float, cy: Float, radius: Float, intensity: Float, colors: List<Color>) {
    drawFireworkCore(cx, cy, intensity, colors[0])
    val points = 32
    for (i in 0 until points) {
        val t = i.toFloat() / points * 2f * PI.toFloat()
        // Параметрическое уравнение сердца
        val hx = 16f * sin(t).pow(3)
        val hy = -(13f * cos(t) - 5f * cos(2 * t) - 2f * cos(3 * t) - cos(4 * t))
        val scale = radius / 18f
        val x = cx + hx * scale
        val y = cy + hy * scale
        drawCircle(color = colors[i % colors.size].copy(alpha = intensity * 0.85f), radius = 1.8f, center = Offset(x, y))
    }
}

// ======================= 🎈 ВОЗДУШНЫЕ ШАРЫ (переделано) =======================
private fun DrawScope.drawBalloons(particles: List<Particle>, progress: Float) {
    val colors = listOf(
        Color(0xFFE91E63) to Color(0xFFAD1457),
        Color(0xFFFF9800) to Color(0xFFE65100),
        Color(0xFFFFEB3B) to Color(0xFFF57F17),
        Color(0xFF4CAF50) to Color(0xFF1B5E20),
        Color(0xFF2196F3) to Color(0xFF0D47A1),
        Color(0xFF9C27B0) to Color(0xFF4A148C)
    )
    particles.forEachIndexed { idx, p ->
        val raw = p.y + progress * p.speed * 0.8f
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = (1f - yNorm) * size.height
            if (y < -100f || y > size.height + 100f) continue
            // Покачивание — разная фаза, чуть больше амплитуды
            val x = p.x * size.width + sin(progress * 4f + p.phase + idx * 0.5f) * 28f
            val (mainColor, darkColor) = colors[idx % colors.size]
            val sz = p.size * 1.6f
            drawRealisticBalloon(x, y, sz, mainColor, darkColor, progress + idx * 0.7f)
        }
    }
}

private fun DrawScope.drawBalloonRealistic_legacy(cx: Float, cy: Float, size: Float, color: Color) {
    // оставлено для обратной совместимости — не используется
}

private fun DrawScope.drawRealisticBalloon(
    cx: Float, cy: Float, size: Float,
    mainColor: Color, darkColor: Color, phase: Float
) {
    val bodyW = size * 1.0f
    val bodyH = size * 1.3f
    val top = cy - bodyH * 0.85f

    // Тень под куполом (глубина)
    drawOval(
        color = darkColor.copy(alpha = 0.35f),
        topLeft = Offset(cx - bodyW * 0.48f, top + bodyH * 0.25f),
        size = Size(bodyW * 0.96f, bodyH * 0.7f)
    )
    // Основной купол
    drawOval(
        color = mainColor.copy(alpha = 0.92f),
        topLeft = Offset(cx - bodyW / 2, top),
        size = Size(bodyW, bodyH)
    )
    // Блик (слева сверху)
    drawOval(
        color = Color.White.copy(alpha = 0.55f),
        topLeft = Offset(cx - bodyW * 0.32f, top + bodyH * 0.15f),
        size = Size(bodyW * 0.25f, bodyH * 0.35f)
    )
    // Второй маленький блик
    drawCircle(
        color = Color.White.copy(alpha = 0.4f),
        radius = bodyW * 0.08f,
        center = Offset(cx - bodyW * 0.15f, top + bodyH * 0.12f)
    )

    // Узелок
    val knotY = top + bodyH
    val knot = Path().apply {
        moveTo(cx - 3f, knotY - 1f)
        lineTo(cx + 3f, knotY - 1f)
        lineTo(cx, knotY + 4f)
        close()
    }
    drawPath(knot, darkColor)

    // Нитка — волнистая с покачиванием
    val threadLen = bodyH * 1.8f
    val threadPath = Path().apply {
        moveTo(cx, knotY + 4f)
        var prevX = cx
        var prevY = knotY + 4f
        val segments = 5
        for (s in 1..segments) {
            val frac = s.toFloat() / segments
            val ny = knotY + 4f + threadLen * frac
            val nx = cx + sin(phase + frac * 4f) * 5f * frac
            quadraticBezierTo(prevX, (prevY + ny) / 2f, nx, ny)
            prevX = nx
            prevY = ny
        }
    }
    drawPath(threadPath, Color(0xFF424242).copy(alpha = 0.65f), style = Stroke(width = 1.4f))
}

// ======================= 💎 КРИСТАЛЛЫ (переделано) =======================
private fun DrawScope.drawCrystals(particles: List<Particle>, progress: Float) {
    val palettes = listOf(
        Triple(Color(0xFFB39DDB), Color(0xFF9575CD), Color(0xFF673AB7)), // фиолетовый
        Triple(Color(0xFF80DEEA), Color(0xFF4DD0E1), Color(0xFF0097A7)), // бирюза
        Triple(Color(0xFFF48FB1), Color(0xFFF06292), Color(0xFFC2185B)), // розовый
        Triple(Color(0xFF9FA8DA), Color(0xFF7986CB), Color(0xFF3F51B5)), // индиго
        Triple(Color(0xFF80CBC4), Color(0xFF4DB6AC), Color(0xFF00796B))  // зелёный
    )
    particles.forEachIndexed { idx, p ->
        val raw = p.y + progress * p.speed
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = yNorm * size.height
            if (y < -60f || y > size.height + 60f) continue
            val x = p.x * size.width + sin(progress * 5f + p.phase) * 20f
            val s = p.size * 1.1f
            val (light, mid, dark) = palettes[idx % palettes.size]

            // 3D-эффект вращения: горизонтальное сжатие меняется по синусу
            val rotationPhase = progress * 6.28f * 2f + p.phase
            val horizontalSquash = 0.55f + 0.45f * kotlin.math.abs(cos(rotationPhase))

            drawRealisticCrystal(x, y, s, horizontalSquash, light, mid, dark)
        }
    }
}

private fun DrawScope.drawRealisticCrystal(
    cx: Float, cy: Float, s: Float,
    squashX: Float,
    light: Color, mid: Color, dark: Color
) {
    val topY = cy - s
    val bottomY = cy + s
    val midY = cy
    val halfW = s * 0.6f * squashX
    val tipTopW = s * 0.28f * squashX

    // Верхняя левая грань (светлая)
    val topLeft = Path().apply {
        moveTo(cx - tipTopW, topY + s * 0.25f)
        lineTo(cx - halfW, midY)
        lineTo(cx, midY)
        lineTo(cx, topY)
        close()
    }
    drawPath(topLeft, light.copy(alpha = 0.95f))

    // Верхняя правая грань (тёмная)
    val topRight = Path().apply {
        moveTo(cx + tipTopW, topY + s * 0.25f)
        lineTo(cx + halfW, midY)
        lineTo(cx, midY)
        lineTo(cx, topY)
        close()
    }
    drawPath(topRight, dark.copy(alpha = 0.95f))

    // Нижняя левая (средний тон)
    val botLeft = Path().apply {
        moveTo(cx - halfW, midY)
        lineTo(cx - tipTopW, bottomY - s * 0.25f)
        lineTo(cx, bottomY)
        lineTo(cx, midY)
        close()
    }
    drawPath(botLeft, mid.copy(alpha = 0.9f))

    // Нижняя правая (тёмная)
    val botRight = Path().apply {
        moveTo(cx + halfW, midY)
        lineTo(cx + tipTopW, bottomY - s * 0.25f)
        lineTo(cx, bottomY)
        lineTo(cx, midY)
        close()
    }
    drawPath(botRight, dark.copy(alpha = 0.75f))

    // Блик сверху (маленький белый треугольник)
    val highlight = Path().apply {
        moveTo(cx, topY + s * 0.08f)
        lineTo(cx - tipTopW * 0.7f, topY + s * 0.35f)
        lineTo(cx, topY + s * 0.5f)
        close()
    }
    drawPath(highlight, Color.White.copy(alpha = 0.75f))

    // Тонкий контур для чёткости
    val outline = Path().apply {
        moveTo(cx, topY)
        lineTo(cx + halfW, midY)
        lineTo(cx, bottomY)
        lineTo(cx - halfW, midY)
        close()
    }
    drawPath(outline, Color.White.copy(alpha = 0.3f), style = Stroke(width = 1f))
}

// ======================= 💰 МОНЕТЫ (переделано) =======================
private fun DrawScope.drawCoins(particles: List<Particle>, progress: Float) {
    val palettes = listOf(
        Triple(Color(0xFFFFD54F), Color(0xFFFFC107), Color(0xFFFF8F00)), // классическое золото
        Triple(Color(0xFFFFE082), Color(0xFFFFCA28), Color(0xFFFFA000)), // светлое золото
        Triple(Color(0xFFFFCC80), Color(0xFFFFB74D), Color(0xFFEF6C00)), // тёплое золото
    )
    particles.forEachIndexed { idx, p ->
        val raw = p.y + progress * p.speed
        for (k in 0..1) {
            val yNorm = raw - k.toFloat()
            val y = yNorm * size.height
            if (y < -40f || y > size.height + 40f) continue
            val x = p.x * size.width + sin(progress * 4f + p.phase) * 18f
            val s = p.size * 1.3f
            val (light, mid, dark) = palettes[idx % palettes.size]

            // Эффект переворота — синус сжатия по X
            val flipPhase = progress * 6.28f * 3f + p.phase
            val squashX = kotlin.math.abs(cos(flipPhase)).coerceAtLeast(0.15f)

            drawRealisticCoin(x, y, s, squashX, light, mid, dark)
        }
    }
}

private fun DrawScope.drawRealisticCoin(
    cx: Float, cy: Float, radius: Float,
    squashX: Float,
    light: Color, mid: Color, dark: Color
) {
    val w = radius * squashX

    // Мягкая тень под монетой
    drawOval(
        color = Color.Black.copy(alpha = 0.15f),
        topLeft = Offset(cx - w * 1.05f, cy + radius * 0.6f),
        size = Size(w * 2.1f, radius * 0.6f)
    )

    // Внешний обод (тёмный)
    drawOval(
        color = dark,
        topLeft = Offset(cx - w, cy - radius),
        size = Size(w * 2, radius * 2)
    )
    // Средний слой
    drawOval(
        color = mid,
        topLeft = Offset(cx - w * 0.88f, cy - radius * 0.88f),
        size = Size(w * 1.76f, radius * 1.76f)
    )
    // Внутренний диск (светлый)
    drawOval(
        color = light,
        topLeft = Offset(cx - w * 0.7f, cy - radius * 0.7f),
        size = Size(w * 1.4f, radius * 1.4f)
    )

    // Гравировка — символ "$" или звёздочка (упрощённо — вертикальная линия)
    if (squashX > 0.4f) {
        val engraveW = 1.2f * squashX
        drawLine(
            color = dark.copy(alpha = 0.6f),
            start = Offset(cx, cy - radius * 0.45f),
            end = Offset(cx, cy + radius * 0.45f),
            strokeWidth = engraveW * 2f
        )
        // Поперечная перечёркивающая линия
        drawLine(
            color = dark.copy(alpha = 0.5f),
            start = Offset(cx - w * 0.3f, cy - radius * 0.1f),
            end = Offset(cx + w * 0.3f, cy - radius * 0.1f),
            strokeWidth = engraveW
        )
        drawLine(
            color = dark.copy(alpha = 0.5f),
            start = Offset(cx - w * 0.3f, cy + radius * 0.1f),
            end = Offset(cx + w * 0.3f, cy + radius * 0.1f),
            strokeWidth = engraveW
        )
    }

    // Блик (верхний левый квадрант) — виден при развороте
    if (squashX > 0.3f) {
        drawOval(
            color = Color.White.copy(alpha = 0.7f * squashX),
            topLeft = Offset(cx - w * 0.45f, cy - radius * 0.55f),
            size = Size(w * 0.3f, radius * 0.35f)
        )
    }
}
