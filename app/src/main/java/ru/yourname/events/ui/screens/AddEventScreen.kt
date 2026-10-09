package ru.yourname.events.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.model.GroupType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    editing: Event?,
    onSave: (Event) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(editing?.name ?: "") }
    var day by remember { mutableStateOf(editing?.dayOfMonth?.toString() ?: "") }
    var month by remember { mutableStateOf(editing?.month?.toString() ?: "") }
    var year by remember { mutableStateOf(editing?.year?.toString() ?: "") }
    var note by remember { mutableStateOf(editing?.note ?: "") }
    var group by remember { mutableStateOf(editing?.group ?: GroupType.FRIENDS) }

    val valid = name.isNotBlank() &&
            (day.toIntOrNull() ?: 0) in 1..31 &&
            (month.toIntOrNull() ?: 0) in 1..12

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editing == null) "Новое событие" else "Редактировать") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("Имя или название события") }, modifier = Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = day, onValueChange = { day = it.filter(Char::isDigit).take(2) },
                    label = { Text("День") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = month, onValueChange = { month = it.filter(Char::isDigit).take(2) },
                    label = { Text("Месяц") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = year, onValueChange = { year = it.filter(Char::isDigit).take(4) },
                    label = { Text("Год") }, modifier = Modifier.weight(1f), singleLine = true)
            }

            Text("Группа", style = MaterialTheme.typography.titleMedium)
            GroupPicker(group) { group = it }

            OutlinedTextField(value = note, onValueChange = { note = it },
                label = { Text("Заметка") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    onSave(
                        Event(
                            id = editing?.id ?: 0,
                            name = name.trim(),
                            dayOfMonth = day.toInt(),
                            month = month.toInt(),
                            year = year.toIntOrNull(),
                            group = group,
                            note = note.trim(),
                            photoUri = editing?.photoUri,
                            contactId = editing?.contactId,
                            isFromContacts = editing?.isFromContacts ?: false
                        )
                    )
                },
                enabled = valid,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Сохранить") }
        }
    }
}

@Composable
private fun GroupPicker(current: GroupType, onSelect: (GroupType) -> Unit) {
    FlowRowLike(current, onSelect)
}

@Composable
private fun FlowRowLike(current: GroupType, onSelect: (GroupType) -> Unit) {
    // простой горизонтальный список с обёрткой
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GroupType.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { g ->
                    FilterChip(
                        selected = g == current,
                        onClick = { onSelect(g) },
                        label = { Text("${g.emoji} ${g.label}") },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
