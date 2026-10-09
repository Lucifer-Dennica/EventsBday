package ru.yourname.events.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.yourname.events.data.model.SocialLink
import ru.yourname.events.data.model.SocialPlatform
import ru.yourname.events.data.social.SocialUrlParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialLinksEditor(
    links: List<SocialLink>,
    onChange: (List<SocialLink>) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    var pendingPlatform by remember { mutableStateOf<SocialPlatform?>(null) }
    var editingIndex by remember { mutableStateOf(-1) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Соцсети и контакты", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { editingIndex = -1; showPicker = true }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Добавить")
            }
        }

        if (links.isEmpty()) {
            Text(
                "Пока ничего не добавлено. Нажмите «Добавить».",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            links.forEachIndexed { i, link ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(link.platform.emoji, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(link.platform.label, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(SocialUrlParser.displayName(link),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = {
                            editingIndex = i
                            pendingPlatform = link.platform
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Изменить",
                                modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = {
                            onChange(links.toMutableList().also { it.removeAt(i) })
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Удалить")
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        ModalBottomSheet(onDismissRequest = { showPicker = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Выберите платформу", style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp))
                SocialPlatform.entries.forEach { p ->
                    ListItem(
                        headlineContent = { Text(p.label) },
                        leadingContent = { Text(p.emoji, style = MaterialTheme.typography.titleLarge) },
                        modifier = Modifier.clickable {
                            pendingPlatform = p
                            showPicker = false
                        }
                    )
                }
            }
        }
    }

    pendingPlatform?.let { platform ->
        val initial = if (editingIndex in links.indices) links[editingIndex].value else ""
        var value by remember(platform, editingIndex) { mutableStateOf(initial) }

        AlertDialog(
            onDismissRequest = { pendingPlatform = null; editingIndex = -1 },
            title = { Text("${platform.emoji} ${platform.label}") },
            text = {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(platform.hint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (value.isNotBlank()) {
                            val newLink = SocialLink(platform, value.trim())
                            val list = links.toMutableList()
                            if (editingIndex in list.indices) list[editingIndex] = newLink
                            else list.add(newLink)
                            onChange(list)
                        }
                        pendingPlatform = null
                        editingIndex = -1
                    },
                    enabled = value.isNotBlank()
                ) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { pendingPlatform = null; editingIndex = -1 }) { Text("Отмена") }
            }
        )
    }
}
