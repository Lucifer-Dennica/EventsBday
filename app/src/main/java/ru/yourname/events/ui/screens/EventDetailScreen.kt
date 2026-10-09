package ru.yourname.events.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.yourname.events.data.model.Event
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
    val today = LocalDate.now()
    val next = event.nextOccurrence(today)
    val days = event.daysUntil(today)
    val fmtFull = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    val age = event.ageAtNext(today)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Изменить", tint = Color.White)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { p ->
        Column(
            Modifier.fillMaxSize().padding(p).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Ближайшее событие", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(next.format(fmtFull), style = MaterialTheme.typography.headlineMedium)

            if (age != null) {
                Text("Исполнится $age лет", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
            }

            Text(
                text = if (days == 0L) "Это сегодня 🎉" else "Через $days дн.",
                style = MaterialTheme.typography.titleLarge
            )

            HorizontalDivider()

            InfoRow("Группа", "${event.group.emoji} ${event.group.label}")
            if (event.year != null) InfoRow("Год рождения", event.year.toString())
            if (event.note.isNotBlank()) InfoRow("Заметка", event.note)
            if (event.isFromContacts) InfoRow("Источник", "Импортировано из контактов")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
