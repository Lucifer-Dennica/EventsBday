package ru.yourname.events.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.model.SocialPlatform
import ru.yourname.events.data.social.SocialUrlParser
import java.time.LocalDate
import kotlin.random.Random

private enum class GreetingTone(val label: String) {
    SHORT("Короткое"),
    WARM("Тёплое"),
    FUNNY("С юмором"),
    POETIC("Стихи")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreetingsDialog(
    event: Event,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    var tone by remember { mutableStateOf(GreetingTone.WARM) }
    var seed by remember { mutableStateOf(Random.nextInt()) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val age = event.ageAtNext(LocalDate.now())
    val currentText = remember(tone, seed, event.name) {
        generateGreeting(event.name, tone, age, seed)
    }

    // Как отправить — ищем соцсеть из контактов
    val preferredPlatform = event.socialLinks.firstOrNull()?.platform
    val phoneDigits = event.socialLinks.firstOrNull {
        it.platform == SocialPlatform.PHONE || it.platform == SocialPlatform.WHATSAPP || it.platform == SocialPlatform.VIBER
    }?.value?.filter { it.isDigit() || it == '+' }

    fun copyToClipboard() {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Поздравление", currentText))
        snackbarMessage = "Скопировано в буфер обмена"
    }

    fun shareGeneric() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, currentText)
        }
        runCatching { ctx.startActivity(Intent.createChooser(intent, "Поделиться")) }
    }

    fun sendToPreferred() {
        val link = event.socialLinks.firstOrNull()
        if (link != null) {
            val url = when (link.platform) {
                SocialPlatform.WHATSAPP -> if (phoneDigits != null) "https://wa.me/${phoneDigits.filter { it.isDigit() }}" else null
                SocialPlatform.TELEGRAM -> SocialUrlParser.buildUrl(link)
                SocialPlatform.VIBER -> if (phoneDigits != null) "viber://chat?number=$phoneDigits" else null
                SocialPlatform.PHONE, SocialPlatform.EMAIL -> null
                else -> SocialUrlParser.buildUrl(link)
            }
            if (url != null) {
                runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))) }
                return
            }
        }
        // Fallback — обычный SEND
        shareGeneric()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Поздравление для ${event.name}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tone chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GreetingTone.entries.forEach { t ->
                        FilterChip(
                            selected = tone == t,
                            onClick = { tone = t; seed = Random.nextInt() },
                            label = { Text(t.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Текст поздравления
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            currentText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { seed = Random.nextInt() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Другое")
                    }
                    Button(
                        onClick = { copyToClipboard() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Копировать")
                    }
                }

                if (snackbarMessage != null) {
                    Text(
                        snackbarMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalDivider()

                Text(
                    if (preferredPlatform != null) "Отправить через ${preferredPlatform.label}" else "Отправить",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                if (preferredPlatform != null) {
                    Button(
                        onClick = { sendToPreferred() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SocialIcon(preferredPlatform)
                        Spacer(Modifier.width(8.dp))
                        Text("Открыть ${preferredPlatform.label}")
                    }
                }

                OutlinedButton(
                    onClick = { shareGeneric() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Поделиться в любом приложении")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}

private fun generateGreeting(name: String, tone: GreetingTone, age: Int?, seed: Int): String {
    val baseGreetings = when (tone) {
        GreetingTone.SHORT -> listOf(
            "🎉 С днём рождения, $name!",
            "С днём рождения, $name! 🎂",
            "$name, с праздником! 🎉",
            "Поздравляю, $name! 🥳",
            "$name, желаю всего наилучшего! ✨",
            "С днём рождения! 💐",
            "Наилучшие пожелания, $name! 🌟",
            "Пусть этот день будет особенным, $name! 🎈"
        )
        GreetingTone.WARM -> listOf(
            "🎉 Дорогой(ая) $name!\n\nС днём рождения! Пусть каждый день приносит радость, а рядом всегда будут близкие люди. Здоровья, счастья и исполнения желаний!\n\nОбнимаю 🤗",
            "$name, от всей души поздравляю!\n\nПусть жизнь будет полна ярких моментов, а все мечты обязательно сбудутся. Ты заслуживаешь только самого лучшего! ✨",
            "Дорогой(ая) $name, с праздником!\n\nЖелаю тебе тепла, уюта, любви и вдохновения. Пусть каждый новый день приносит что-то хорошее! 💖",
            "$name, поздравляю от всего сердца! 🎂\n\nПусть рядом всегда будут верные друзья, а удача сопутствует во всех начинаниях!",
            "С днём рождения, $name! 🎉\n\nЖелаю крепкого здоровья, огромного счастья и море позитивных эмоций. Ты классный человек — оставайся таким!",
            "$name, с праздником тебя! 🌷\n\nПусть твоя жизнь будет такой же яркой и красивой, как ты сам(а). Всех благ и тепла!"
        )
        GreetingTone.FUNNY -> listOf(
            "🎉 $name, с днём рождения!\n\nПомни: возраст — это только цифра! Главное — чувствовать себя на 18! 💃🕺\n\nЖелаю, чтобы все проблемы решались сами, а деньги появлялись из воздуха!",
            "$name, с днём варенья! 🎂\n\nЖелаю: зарплату как у министра, здоровье как у космонавта, а нервов как у буддистского монаха! ☺️",
            "Ура! $name, ты стал(а) на год мудрее! 🧙‍♂️\n\nЖелаю, чтобы внуки были послушными, а пенсия — золотой! (шутка, ты ещё молод(а)!) 😄",
            "🎈 $name, поздравляю!\n\nЖелаю, чтобы дома всегда был интернет, еда в холодильнике и никто не трогал твой шоколад. Ну и счастья, конечно!",
            "С днём рождения, $name! 🥳\n\nЖелаю меньше будильников по утрам и больше выходных подольше! И пусть все желания сбываются не по расписанию, а прямо сейчас!"
        )
        GreetingTone.POETIC -> listOf(
            "$name, в этот день прекрасный\nПусть счастье постучится в дом.\nЖелаю жизни яркой, ясной,\nИ пусть удача будет в нём! ✨",
            "Дорогой(ая) $name,\nПусть солнце светит ярко,\nА в сердце — теплый свет.\nЗдоровья, счастья, мира\nНа много-много лет! 🌷",
            "С днём рождения, $name!\nПусть будет путь твой светел,\nПусть радость в каждый день придёт,\nИ счастье не ответит...\nА просто навсегда войдёт! 🌟",
            "$name, в твой день рождения\nХочу я пожелать:\nЛюбви, добра, везения,\nУметь мечтать, летать!\nПусть все мечты сбываются,\nКак звёзды в вышине! ✨"
        )
    }

    var text = baseGreetings[Random(seed).nextInt(baseGreetings.size)]
    // Для ДР с возрастом — добавляем строчку
    if (age != null && age > 0 && tone != GreetingTone.SHORT) {
        text += "\n\n🎂 Тебе сегодня $age! Пусть следующий год будет ещё лучше!"
    }
    return text
}
