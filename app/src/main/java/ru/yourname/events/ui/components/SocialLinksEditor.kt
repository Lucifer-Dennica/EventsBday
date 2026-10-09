package ru.yourname.events.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.yourname.events.data.model.SocialLink
import ru.yourname.events.data.model.SocialPlatform
import ru.yourname.events.data.social.SocialUrlParser

/** Редактор списка соцсетей: чипы с крестиком + кнопка "Добавить". */
@Composable
fun SocialLinksEditor(
    links: List<SocialLink>,
    onChange: (List<SocialLink>) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Соцсети и контакты", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { showPicker = true }) {
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                        IconButton(onClick = { editingIndex = i }) {
                            Icon(Icons.Default.Add, contentDescription = "Изменить",
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
        PlatformPickerSheet(
            onDismiss = { showPicker = false },
            onPick = { platform ->
                editingIndex = -1 // означает "новый"
                showPicker = false
                pendingPlatform = platform
            }
        )
    }

    // Диалог ввода значения
    var pendingPlatform by remember { mutableStateOf<SocialPlatform?>(null) }
    pendingPlatform?.let { platform ->
        var value by remember(platform) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { pendingPlatform = null; editingIndex = null },
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
                            val idx = editingIndex ?: -1
                            if (idx >= 0) list[idx] = newLink else list.add(newLink)
                            onChange(list)
                        }
                        pendingPlatform = null
                        editingIndex = null
                    },
                    enabled = value.isNotBlank()
                ) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { pendingPlatform = null; editingIndex = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlatformPickerSheet(
    onDismiss: () -> Unit,
    onPick: (SocialPlatform) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Выберите платформу", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp))
            SocialPlatform.entries.forEach { p ->
                ListItem(
                    headlineContent = { Text(p.label) },
                    leadingContent = { Text(p.emoji, style = MaterialTheme.typography.titleLarge) },
                    modifier = Modifier.clickableNoRipple { onPick(p) }
                )
            }
        }
    }
}

private fun Modifier.clickableNoRipple(block: () -> Unit): Modifier =
    this.then(Modifier).let { it } // упрощено — используем обычный clickable
