package ru.yourname.events.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ru.yourname.events.data.model.CardAnimation
import ru.yourname.events.data.model.CardColors
import ru.yourname.events.data.model.ChecklistItem
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.settings.SettingsRepository
import ru.yourname.events.ui.components.AllPresetAvatars
import ru.yourname.events.ui.components.CustomColorDialog
import ru.yourname.events.ui.components.SocialLinksEditor
import ru.yourname.events.ui.components.customColorBrush
import ru.yourname.events.ui.components.presetAvatarRes
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    editing: Event?,
    onSave: (Event) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(editing?.name ?: "") }
    var date by remember {
        mutableStateOf(
            if (editing != null)
                LocalDate.of(editing.year ?: 2000, editing.month, editing.dayOfMonth)
            else LocalDate.now()
        )
    }
    var hasYear by remember { mutableStateOf(editing?.year != null) }
    var note by remember { mutableStateOf(editing?.note ?: "") }
    var group by remember { mutableStateOf(editing?.group ?: "Знакомые") }
    var photoUri by remember { mutableStateOf(editing?.photoUri) }
    var presetAvatar by remember { mutableStateOf(editing?.presetAvatar) }
    var socials by remember { mutableStateOf(editing?.socialLinks ?: emptyList()) }
    var notifications by remember { mutableStateOf(editing?.notificationsEnabled ?: true) }
    var cardColor by remember { mutableStateOf(editing?.cardColor ?: 0) }
    var isPinned by remember { mutableStateOf(editing?.isPinned ?: false) }
    var animation by remember { mutableStateOf(editing?.animation ?: CardAnimation.NONE) }
    var customColorHex by remember { mutableStateOf(editing?.customColorHex) }
    var checklist by remember { mutableStateOf(editing?.checklist ?: emptyList()) }
    var newItemText by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                ctx.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            photoUri = uri.toString()
            presetAvatar = null
        }
    }

    val groupPresets = listOf("Семья", "Друзья", "Родственники", "Знакомые", "Работа", "Прочее")
    val checklistPresets = listOf("Купить подарок", "Отправить открытку", "Позвонить", "Написать в мессенджер")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editing == null) "Новое событие" else "Редактировать") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Фото", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(64.dp).clip(CircleShape)
                            )
                        } else {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Выбрать фото")
                        }
                    }
                }
                items(AllPresetAvatars) { idx ->
                    val selected = presetAvatar == idx && photoUri == null
                    val res = presetAvatarRes(idx)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (selected) 3.dp else 0.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                photoUri = null
                                presetAvatar = if (presetAvatar == idx) null else idx
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (res != 0) {
                            Image(
                                painter = painterResource(res),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(64.dp).clip(CircleShape)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя или название события") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Дата", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (hasYear) "📅 ${date.dayOfMonth}.${date.monthValue}.${date.year}"
                    else "📅 ${date.dayOfMonth}.${date.monthValue} (без года)"
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = hasYear, onCheckedChange = { hasYear = it })
                Text("Указать год рождения")
            }

            Text("Группа", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = group,
                onValueChange = { group = it },
                label = { Text("Название группы") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(groupPresets) { g ->
                    FilterChip(
                        selected = g == group,
                        onClick = { group = g },
                        label = { Text(g) }
                    )
                }
            }

            // ЦВЕТ КАРТОЧКИ
            Text("Цвет карточки", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(CardColors.indices.toList()) { i ->
                    val selected = cardColor == i && customColorHex == null
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardColors[i])
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                cardColor = i
                                customColorHex = null
                            }
                    )
                }
                item {
                    val active = customColorHex != null
                    val brush: Brush = customColorBrush(customColorHex)
                        ?: Brush.verticalGradient(
                            listOf(
                                Color(0xFFFF9A9E),
                                Color(0xFFFAD0C4),
                                Color(0xFFA18CD1)
                            )
                        )
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(brush)
                            .border(
                                width = if (active) 3.dp else 1.dp,
                                color = if (active) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { showColorDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ColorLens,
                            contentDescription = "Свой цвет",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // АНИМАЦИЯ
            Text("Анимация карточки", style = MaterialTheme.typography.titleMedium)
            Text(
                "Может немного нагружать устройство. Если не нужно — оставьте «Без анимации».",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CardAnimation.entries.toList()) { a ->
                    FilterChip(
                        selected = animation == a,
                        onClick = { animation = a },
                        label = { Text("${a.emoji} ${a.label}") }
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // === ЧЕК-ЛИСТ ===
            Text("Чек-лист", style = MaterialTheme.typography.titleMedium)
            Text(
                "Полезно для подготовки: «Купить подарок», «Позвонить» и т.д.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (checklist.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    checklist.forEachIndexed { i, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.done,
                                onCheckedChange = { checked ->
                                    checklist = checklist.toMutableList().also {
                                        it[i] = item.copy(done = checked)
                                    }
                                }
                            )
                            Text(
                                item.text,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            IconButton(onClick = {
                                checklist = checklist.toMutableList().also { it.removeAt(i) }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Удалить")
                            }
                        }
                    }
                }
            }

            // Добавление нового пункта
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    placeholder = { Text("Новый пункт") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            checklist = checklist + ChecklistItem(newItemText.trim())
                            newItemText = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                }
            }

            // Быстрые пресеты
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(checklistPresets) { preset ->
                    AssistChip(
                        onClick = {
                            if (checklist.none { it.text == preset }) {
                                checklist = checklist + ChecklistItem(preset)
                            }
                        },
                        label = { Text("+ $preset") }
                    )
                }
            }

            SocialLinksEditor(links = socials, onChange = { socials = it })

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Закрепить событие", style = MaterialTheme.typography.titleMedium)
                    Text("Всегда сверху списка",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isPinned, onCheckedChange = { isPinned = it })
            }

            Spacer(Modifier.height(8.dp))

            val valid = name.isNotBlank()
            Button(
                onClick = {
                    onSave(
                        Event(
                            id = editing?.id ?: 0,
                            name = name.trim(),
                            dayOfMonth = date.dayOfMonth,
                            month = date.monthValue,
                            year = if (hasYear) date.year else null,
                            group = group.trim().ifBlank { "Прочее" },
                            note = note.trim(),
                            photoUri = photoUri,
                            presetAvatar = presetAvatar,
                            contactId = editing?.contactId,
                            isFromContacts = editing?.isFromContacts ?: false,
                            socialLinks = socials,
                            notificationsEnabled = notifications,
                            cardColor = cardColor,
                            isPinned = isPinned,
                            animation = animation,
                            customColorHex = customColorHex,
                            checklist = checklist
                        )
                    )
                },
                enabled = valid,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Сохранить") }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli(),
            yearRange = 1900..(LocalDate.now().year + 1)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showColorDialog) {
        CustomColorDialog(
            initial = customColorHex,
            onDismiss = { showColorDialog = false },
            onSave = { result ->
                customColorHex = result
                if (result != null) {
                    cardColor = 0
                    scope.launch { SettingsRepository.addRecentColor(ctx, result) }
                }
                showColorDialog = false
            }
        )
    }
}
