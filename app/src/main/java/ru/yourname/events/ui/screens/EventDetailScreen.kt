package ru.yourname.events.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.social.SocialUrlParser
import ru.yourname.events.ui.components.EventAvatar
import ru.yourname.events.ui.components.ZodiacBlock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val today = LocalDate.now()
    val next = event.nextOccurrence(today)
    val days = event.daysUntil(today)
    val fmtFull = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    val age = event.ageAtNext(today)

    var confirmDelete by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

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

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            // Соцсети
            if (event.socialLinks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Контакты", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    event.socialLinks.forEach { link ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                val url = SocialUrlParser.buildUrl(link)
                                runCatching {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(url))
                                    ctx.startActivity(intent)
                                }
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(link.platform.emoji, style = MaterialTheme.typography.titleLarge)
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
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                }
            }

            // Заметка
            if (event.note.isNotBlank()) {
                Column(Modifier.fillMaxWidth()) {
                    Text("Заметка", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(event.note, style = MaterialTheme.typography.bodyLarge)
                }
            }

            // Группа и год
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Группа", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(event.group, style = MaterialTheme.typography.bodyLarge)
                }
                if (event.year != null) {
                    Column {
                        Text("Год рождения", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(event.year.toString(), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            // Зодиак
            ZodiacBlock(event)
        }
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
