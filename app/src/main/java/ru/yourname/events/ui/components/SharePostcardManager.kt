package ru.yourname.events.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.model.cardColorAt
import ru.yourname.events.data.settings.AvatarShape
import ru.yourname.events.data.settings.SettingsRepository
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object SharePostcardManager {

    private const val POSTER_WIDTH = 1080
    private const val POSTER_HEIGHT = 1350

    suspend fun generateAndShare(ctx: Context, event: Event) {
        val shape = SettingsRepository.avatarShapeFlow(ctx).first()

        val bitmap = withContext(Dispatchers.IO) {
            renderPostcard(ctx, event, shape)
        }

        val file = withContext(Dispatchers.IO) { saveToFile(ctx, bitmap, event.id) }
        val uri = FileProvider.getUriForFile(
            ctx,
            "${ctx.packageName}.fileprovider",
            file
        )

        withContext(Dispatchers.Main) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "🎂 ${event.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            runCatching {
                ctx.startActivity(Intent.createChooser(intent, "Поделиться открыткой"))
            }
        }
    }

    private fun saveToFile(ctx: Context, bitmap: Bitmap, eventId: Long): File {
        val dir = File(ctx.cacheDir, "postcards").apply { mkdirs() }
        val file = File(dir, "postcard_${eventId}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    private fun renderPostcard(ctx: Context, event: Event, avatarShape: AvatarShape): Bitmap {
        val bmp = Bitmap.createBitmap(POSTER_WIDTH, POSTER_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)

        // === 1. ФОН ===
        val (bgPrimary, bgSecondary, isDarkBg) = resolveBackground(event)

        if (bgSecondary != null) {
            val paint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 0f, POSTER_HEIGHT.toFloat(),
                    bgPrimary, bgSecondary,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, POSTER_WIDTH.toFloat(), POSTER_HEIGHT.toFloat(), paint)
        } else {
            canvas.drawColor(bgPrimary)
        }

        val textColor = if (isDarkBg) Color.WHITE else Color.rgb(26, 28, 34)
        val subTextColor = if (isDarkBg) Color.argb(180, 255, 255, 255) else Color.rgb(91, 100, 120)
        val accentColor = if (isDarkBg) Color.rgb(200, 210, 255) else Color.rgb(57, 64, 153)

        // === 2. ЭМОДЗИ СВЕРХУ (🎂) ===
        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 180f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🎂", POSTER_WIDTH / 2f, 220f, emojiPaint)

        // === 3. АВАТАР ===
        val avatarSize = 320f
        val avatarLeft = (POSTER_WIDTH - avatarSize) / 2f
        val avatarTop = 320f
        val avatarRect = RectF(avatarLeft, avatarTop, avatarLeft + avatarSize, avatarTop + avatarSize)

        drawAvatar(ctx, canvas, event, avatarShape, avatarRect, accentColor, isDarkBg)

        // === 4. ИМЯ ===
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 84f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val name = event.name
        val nameY = avatarTop + avatarSize + 130f
        canvas.drawText(name, POSTER_WIDTH / 2f, nameY, namePaint)

        // === 5. ДАТА ===
        val today = LocalDate.now()
        val next = event.nextOccurrence(today)
        val fmt = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
        val dateStr = next.format(fmt)

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = subTextColor
            textSize = 48f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
        }
        canvas.drawText(dateStr, POSTER_WIDTH / 2f, nameY + 80f, datePaint)

        // === 6. ВОЗРАСТ ===
        val age = event.ageAtNext(today)
        if (age != null) {
            val agePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = 60f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val ageText = if (age % 10 == 1 && age % 100 != 11) "$age год"
                          else if (age % 10 in 2..4 && age % 100 !in 12..14) "$age года"
                          else "$age лет"
            canvas.drawText(ageText, POSTER_WIDTH / 2f, nameY + 165f, agePaint)
        }

        // === 7. НИЖНИЙ БРЕНДИНГ ===
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = subTextColor
            textSize = 32f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
        }
        canvas.drawText("Мои События", POSTER_WIDTH / 2f, POSTER_HEIGHT - 80f, brandPaint)

        val brandEmoji = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 44f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🎉", POSTER_WIDTH / 2f, POSTER_HEIGHT - 140f, brandEmoji)

        return bmp
    }

    private fun resolveBackground(event: Event): Triple<Int, Int?, Boolean> {
        val customHex = event.customColorHex
        if (!customHex.isNullOrBlank()) {
            val parsed = parseCustomColor(customHex)
            if (parsed.isNotEmpty()) {
                val primary = parsed[0].toArgb()
                val secondary = if (parsed.size > 1) parsed[1].toArgb() else null
                val isDark = !parsed[0].isLight()
                return Triple(primary, secondary, isDark)
            }
        }
        if (event.cardColor != 0) {
            val c = cardColorAt(event.cardColor).toArgb()
            val isDark = !cardColorAt(event.cardColor).isLight()
            return Triple(c, null, isDark)
        }
        return Triple(Color.rgb(232, 235, 250), null, false)
    }

    private fun drawAvatar(
        ctx: Context,
        canvas: Canvas,
        event: Event,
        shape: AvatarShape,
        rect: RectF,
        accentColor: Int,
        isDarkBg: Boolean
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDarkBg) Color.argb(60, 255, 255, 255)
                    else Color.argb(50, 0, 0, 0)
        }
        val padding = 18f
        val innerRect = RectF(rect.left + padding, rect.top + padding, rect.right - padding, rect.bottom - padding)

        when (shape) {
            AvatarShape.CIRCLE -> canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width() / 2f, bgPaint)
            AvatarShape.SQUARE -> canvas.drawRect(rect, bgPaint)
            AvatarShape.ROUNDED_SQUARE -> {
                val r = rect.width() * 0.22f
                canvas.drawRoundRect(rect, r, r, bgPaint)
            }
        }

        val presetRes = event.presetAvatar?.let { presetAvatarRes(it) }
        val photoBmp = loadAvatarBitmap(ctx, event, presetRes)

        if (photoBmp != null) {
            val srcRect = Rect(0, 0, photoBmp.width, photoBmp.height)
            when (shape) {
                AvatarShape.CIRCLE -> {
                    val path = android.graphics.Path().apply {
                        addCircle(innerRect.centerX(), innerRect.centerY(), innerRect.width() / 2f, android.graphics.Path.Direction.CW)
                    }
                    canvas.save()
                    canvas.clipPath(path)
                    canvas.drawBitmap(photoBmp, srcRect, innerRect, Paint(Paint.ANTI_ALIAS_FLAG))
                    canvas.restore()
                }
                AvatarShape.SQUARE -> {
                    canvas.drawBitmap(photoBmp, srcRect, innerRect, Paint(Paint.ANTI_ALIAS_FLAG))
                }
                AvatarShape.ROUNDED_SQUARE -> {
                    val r = innerRect.width() * 0.22f
                    val path = android.graphics.Path().apply {
                        addRoundRect(innerRect, r, r, android.graphics.Path.Direction.CW)
                    }
                    canvas.save()
                    canvas.clipPath(path)
                    canvas.drawBitmap(photoBmp, srcRect, innerRect, Paint(Paint.ANTI_ALIAS_FLAG))
                    canvas.restore()
                }
            }
        } else {
            val letterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = innerRect.width() * 0.55f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val letter = event.name.trim().firstOrNull()?.uppercase() ?: "?"
            val fm = letterPaint.fontMetrics
            val offset = (fm.ascent + fm.descent) / 2f
            canvas.drawText(letter, innerRect.centerX(), innerRect.centerY() - offset, letterPaint)
        }
    }

    private fun loadAvatarBitmap(ctx: Context, event: Event, presetRes: Int?): Bitmap? {
        if (!event.photoUri.isNullOrBlank()) {
            val bmp = runCatching {
                ctx.contentResolver.openInputStream(android.net.Uri.parse(event.photoUri))?.use {
                    BitmapFactory.decodeStream(it)
                }
            }.getOrNull()
            if (bmp != null) return bmp
        }
        if (presetRes != null && presetRes != 0) {
            return runCatching {
                BitmapFactory.decodeResource(ctx.resources, presetRes)
            }.getOrNull()
        }
        return null
    }
}
