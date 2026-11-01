package ru.yourname.events.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.model.SocialPlatform
import ru.yourname.events.data.social.SocialUrlParser
import ru.yourname.events.ui.components.EventAvatar
import ru.yourname.events.ui.components.GreetingsDialog
import ru.yourname.events.ui.components.LocalDesignConfig
import ru.yourname.events.ui.components.SocialIcon
import ru.yourname.events.ui.components.ZodiacBlock
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (Event) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val design = LocalDesignConfig.current
    val today = LocalDate.now()
    val next = event.nextOccurrence(today)
    val days = event.daysUntil(today)
    val fmtFull = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    val age = event.ageAtNext(today)

    var confirmDelete by remember { mutableStateOf(false) }
    var showGreetings by remember { mutableStateOf(false) }

    val phoneLink = event.socialLinks.firstOrNull {
        it.platform == SocialPlatform.PHONE ||
                it.platform == SocialPlatform.WHATSAPP ||
                it.platform == SocialPlatform.VIBER
    }
    val phoneDigits = phoneLink?.value
        ?.filter { it.isDigit() || it == '+' }
        ?.takeIf { it.isNotBlank() }

    fun dial() {
        if (phoneDigits == null) return
        runCatching { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneDigits"))) }
    }

    fun sendSms() {
        if (phoneDigits == null) return
        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneDigits"))) }
    }

    fun addToCalendar() {
        val beginMillis = next.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = beginMillis + 24L * 60 * 60 * 1000
        val title = if (age != null) "${event.name} — $age лет" else event.name
        val description = buildString {
            if (event.note.isNotBlank()) append(event.note).append("\n\n")
            append("Добавлено из приложения «Мои События»")
        }

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.TITLE, "🎂 $title")
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.Events.HAS_ALARM, 1)
            putExtra(CalendarContract.Reminders.MINUTES, 60)
            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            if (event.repeatYearly) {
                putExtra(CalendarContract.Events.RRULE, "FREQ=YEARLY")
            }
        }
        runCatching { ctx.startActivity(intent) }
    }

    fun toggleChecklistItem(index: Int) {
        val newList = event.checklist.toMutableList().also {
            it[index] = it[index].copy(done = !it[index].done)
        }
        onUpdate(event.copy(checklist = newList))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Изменить") }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { p ->
        Column(
            Modifier.fillMaxSize().padding(p).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EventAvatar(event, size = 96.dp, accentColor = MaterialTheme.colorScheme.primary)
            Text(event.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                text = if (days == 0L) "Сегодня 🎉" else "Через $days дн. — ${next.format(fmtFull)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (age != null) {
                Text("Исполнится $age лет", style = MaterialTheme.typography.titleLarge)
            }

            // === КНОПКИ ДЕЙСТВИЙ 2x2 ===
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (phoneDigits != null) {
                    Button(
                        onClick = { dial() },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Позвонить", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { sendSms() },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("SMS", maxLines = 1)
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { addToCalendar() },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("В календарь", maxLines = 1)
                }
                Button(
                    onClick = { showGreetings = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(Icons.Default.Cake, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Поздравить", maxLines = 1)
                }
            }

            if (event.checklist.isNotEmpty()) {
                HorizontalDivider()
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Чек-лист",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    event.checklist.forEachIndexed { i, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.done,
                                onCheckedChange = { toggleChecklistItem(i) }
                            )
                            Text(
                                item.text,
                                style = MaterialTheme.typography.bodyLarge,
                                textDecoration = if (item.done) TextDecoration.LineThrough
                                                 else TextDecoration.None,
                                color = if (item.done) MaterialTheme.colorScheme.onSurfaceVariant
                                        else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (design.showZodiac) {
                ZodiacBlock(event)
            }

            if (event.note.isNotBlank()) {
                Column(Modifier.fillMaxWidth()) {
                    Text("Заметка", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(event.note, style = MaterialTheme.typography.bodyLarge)
                }
                HorizontalDivider()
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Группа", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(event.group, style = MaterialTheme.typography.bodyLarge)
                }
                if (event.year != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Год рождения", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(event.year.toString(), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            if (event.socialLinks.isNotEmpty()) {
                HorizontalDivider()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Контакты", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    event.socialLinks.forEach { link ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                val url = SocialUrlParser.buildUrl(link)
                                runCatching {
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SocialIcon(link.platform, size = 28.dp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(link.platform.label, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(SocialUrlParser.displayName(link),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGreetings) {
        GreetingsDialog(event = event, onDismiss = { showGreetings = false })
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить событие?") },
            text = { Text("«${event.name}» будет удалено без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Отмена") }
            }
        )
    }
}
