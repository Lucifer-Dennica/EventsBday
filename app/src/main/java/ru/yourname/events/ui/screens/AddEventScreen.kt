package ru.yourname.events.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ru.yourname.events.data.model.CardAnimation
import ru.yourname.events.data.model.CardColors
import ru.yourname.events.data.model.ChecklistItem
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.settings.CardBorderColor
import ru.yourname.events.data.settings.SettingsRepository
import ru.yourname.events.ui.components.AllPresetAvatars
import ru.yourname.events.ui.components.CustomColorDialog
import ru.yourname.events.ui.components.EventCard
import ru.yourname.events.ui.components.SocialLinksEditor
import ru.yourname.events.ui.components.customColorBrush
import ru.yourname.events.ui.components.presetAvatarRes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    editing: Event?,
    onSave: (Event) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val globalBorderWidth by SettingsRepository.cardBorderWidthFlow(ctx).collectAsState(initial = 2)
    val globalBorderColor by SettingsRepository.cardBorderColorFlow(ctx).collectAsState(initial = CardBorderColor.AUTO)

    var name by remember { mutableStateOf(editing?.name ?: "") }
    var date by remember {
        mutableStateOf(
            if (editing != null)
                runCatching {
                    LocalDate.of(editing.year ?: LocalDate.now().year, editing.month, editing.dayOfMonth)
                }.getOrElse { LocalDate.now() }
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
    var repeatYearly by remember { mutableStateOf(editing?.repeatYearly ?: true) }
    var borderColorHex by remember { mutableStateOf(editing?.borderColorHex) }
    var borderStyle by remember { mutableStateOf(editing?.borderStyle) }
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

    val previewEvent = Event(
        id = editing?.id ?: -1L,
        name = name.ifBlank { "Имя события" },
        dayOfMonth = date.dayOfMonth,
        month = date.monthValue,
        year = if (hasYear) date.year else null,
        group = group.ifBlank { "Группа" },
        note = note,
        photoUri = photoUri,
        presetAvatar = presetAvatar,
        socialLinks = socials,
        notificationsEnabled = notifications,
        cardColor = cardColor,
        isPinned = isPinned,
        animation = animation,
        customColorHex = customColorHex,
        checklist = checklist,
        repeatYearly = repeatYearly,
        borderColorHex = borderColorHex,
        borderStyle = borderStyle
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editing == null) "Новое событие" else "Редактировать") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(model = photoUri, contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(64.dp).clip(CircleShape))
                        } else {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Выбрать фото")
                        }
                    }
                }
                items(AllPresetAvatars) { idx ->
                    val selected = presetAvatar == idx && photoUri == null
                    val res = presetAvatarRes(idx)
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape)
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
                            Image(painter = painterResource(res), contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(64.dp).clip(CircleShape))
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Имя или название события") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
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

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = repeatYearly, onCheckedChange = { repeatYearly = it })
                Column {
                    Text("Напоминать каждый год", style = MaterialTheme.typography.bodyLarge)
                    Text("Если выключено — событие будет разовым",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text("Группа", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = group, onValueChange = { group = it },
                label = { Text("Название группы") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(groupPresets) { g ->
                    FilterChip(selected = g == group, onClick = { group = g }, label = { Text(g) })
                }
            }

            Text("Цвет карточки", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(CardColors.indices.toList()) { i ->
                    val selected = cardColor == i && customColorHex == null
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
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
                        ?: Brush.verticalGradient(listOf(Color(0xFFFF9A9E), Color(0xFFFAD0C4), Color(0xFFA18CD1)))
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
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
                        Icon(Icons.Default.ColorLens, contentDescription = "Свой цвет",
                            tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Text("Обводка этой карточки", style = MaterialTheme.typography.titleMedium)
            Text("«A» — как в настройках дизайна. Цвета — статичная обводка. Неоновые кружки — анимированная.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            PerCardBorderPicker(
                selectedStyle = borderStyle,
                selectedColorHex = borderColorHex,
                onPick = { style, hex ->
                    borderStyle = style
                    borderColorHex = hex
                }
            )

            Text("Анимация карточки", style = MaterialTheme.typography.titleMedium)
            Text("Может немного нагружать устройство. Если не нужно — оставьте «Без анимации».",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CardAnimation.entries.toList()) { a ->
                    FilterChip(
                        selected = animation == a,
                        onClick = { animation = a },
                        label = { Text("${a.emoji} ${a.label}") }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Предпросмотр", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Text("— так будет выглядеть карточка",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(Modifier.fillMaxWidth()) {
                EventCard(
                    event = previewEvent,
                    isSelected = false,
                    selectionMode = false,
                    showNotificationsIcon = true,
                    showPinIcon = true,
                    animationsAllowed = true,
                    borderWidth = globalBorderWidth,
                    borderColorPref = globalBorderColor,
                    borderGlow = false,
                    neonBorder = false,
                    onClick = {},
                    onLongClick = {},
                    onToggleNotifications = {},
                    onTogglePin = {}
                )
            }

            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("Заметка") },
                modifier = Modifier.fillMaxWidth(), minLines = 2
            )

            Text("Чек-лист", style = MaterialTheme.typography.titleMedium)
            if (checklist.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    checklist.forEachIndexed { i, item ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = item.done,
                                onCheckedChange = { checked ->
                                    checklist = checklist.toMutableList().also {
                                        it[i] = item.copy(done = checked)
                                    }
                                }
                            )
                            Text(item.text, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                checklist = checklist.toMutableList().also { it.removeAt(i) }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Удалить")
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newItemText, onValueChange = { newItemText = it },
                    placeholder = { Text("Новый пункт") }, singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(onClick = {
                    if (newItemText.isNotBlank()) {
                        checklist = checklist + ChecklistItem(newItemText.trim())
                        newItemText = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
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
                            checklist = checklist,
                            repeatYearly = repeatYearly,
                            borderColorHex = borderColorHex,
                            borderStyle = borderStyle
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
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            yearRange = 1900..(LocalDate.now().year + 1)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selMillis = state.selectedDateMillis
                    val dispMillis = state.displayedMonthMillis
                    if (selMillis != null) {
                        val sel = Instant.ofEpochMilli(selMillis).atZone(ZoneOffset.UTC).toLocalDate()
                        val disp = Instant.ofEpochMilli(dispMillis).atZone(ZoneOffset.UTC).toLocalDate()
                        val resultYear = if (disp.year != sel.year) disp.year else sel.year
                        val resultDate = runCatching {
                            LocalDate.of(resultYear, sel.monthValue, sel.dayOfMonth)
                        }.getOrElse { LocalDate.of(resultYear, sel.monthValue, 28) }
                        date = resultDate
                        hasYear = true
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }
        ) { DatePicker(state = state) }
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

/** 1 «A» + 16 цветов + 3 неона. Компактный ряд из 20 кружков. */
@Composable
private fun PerCardBorderPicker(
    selectedStyle: String?,
    selectedColorHex: String?,
    onPick: (style: String?, hex: String?) -> Unit
) {
    val neonGradient = listOf(
        Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFFFFEB3B),
        Color(0xFF4CAF50), Color(0xFF00BCD4), Color(0xFF3F51B5),
        Color(0xFF9C27B0), Color(0xFFE91E63)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item {
            val isSelected = selectedStyle == null && selectedColorHex == null
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(Brush.sweepGradient(neonGradient))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable { onPick(null, null) },
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        items(CardBorderColor.entries.filter { it.hex != null }) { color ->
            val hex = "#" + (color.hex!!.toInt() and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()
            val isSelected = selectedStyle == "COLOR" && selectedColorHex.equals(hex, ignoreCase = true)
            val checkColor = when (color) {
                CardBorderColor.WHITE, CardBorderColor.YELLOW, CardBorderColor.GOLD -> Color.Black
                else -> Color.White
            }
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(SolidColor(Color(color.hex)))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable { onPick("COLOR", hex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = checkColor, modifier = Modifier.size(22.dp))
                }
            }
        }

        item {
            NeonStyleCircle(
                mode = NeonStyle.FULL_STATIC,
                isSelected = selectedStyle == "NEON_FULL_STATIC",
                onClick = { onPick("NEON_FULL_STATIC", null) }
            )
        }
        item {
            NeonStyleCircle(
                mode = NeonStyle.RUNNING,
                isSelected = selectedStyle == "NEON_RUNNING",
                onClick = { onPick("NEON_RUNNING", null) }
            )
        }
        item {
            NeonStyleCircle(
                mode = NeonStyle.FULL_ANIMATED,
                isSelected = selectedStyle == "NEON_FULL_ANIMATED",
                onClick = { onPick("NEON_FULL_ANIMATED", null) }
            )
        }
    }
}

private enum class NeonStyle { FULL_STATIC, RUNNING, FULL_ANIMATED }

/**
 * Живой кружок неона — внутри реально анимируется:
 * - FULL_STATIC: статичный залитый радужный круг + пульсирующее внешнее свечение
 * - RUNNING: вращающаяся неполная дуга (бегущий сегмент)
 * - FULL_ANIMATED: сплошное радужное кольцо с непрерывно вращающимся градиентом
 */
@Composable
private fun NeonStyleCircle(
    mode: NeonStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val neonGradient = listOf(
        Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFFFFEB3B),
        Color(0xFF4CAF50), Color(0xFF00BCD4), Color(0xFF3F51B5),
        Color(0xFF9C27B0), Color(0xFFE91E63)
    )

    val transition = rememberInfiniteTransition(label = "preview_${mode.name}")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (mode) {
                    NeonStyle.RUNNING -> 2000
                    NeonStyle.FULL_ANIMATED -> 3000
                    NeonStyle.FULL_STATIC -> 4000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val borderColor = if (isSelected) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val borderWidth = if (isSelected) 3.dp else 1.dp

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(borderWidth, borderColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val strokeW = 5f
            val brush = Brush.sweepGradient(neonGradient)
            val c = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - strokeW / 2f

            when (mode) {
                NeonStyle.FULL_STATIC -> {
                    // Пульсирующее внешнее свечение
                    val pulse = 0.5f + 0.5f * ((sinDegrees(phase) + 1f) / 2f)
                    drawCircle(
                        brush = brush,
                        alpha = 0.25f + pulse * 0.35f,
                        radius = radius + 3f
                    )
                    // Чёткое кольцо
                    drawCircle(brush = brush, style = Stroke(width = strokeW))
                }

                NeonStyle.RUNNING -> {
                    // Вращающаяся дуга — как «бегущий сегмент»
                    rotate(phase) {
                        drawArc(
                            brush = brush,
                            startAngle = 0f,
                            sweepAngle = 216f,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                        // Догоняющий «хвост»
                        drawArc(
                            brush = brush,
                            startAngle = 216f,
                            sweepAngle = 40f,
                            useCenter = false,
                            alpha = 0.35f,
                            style = Stroke(width = strokeW * 0.7f, cap = StrokeCap.Round)
                        )
                    }
                }

                NeonStyle.FULL_ANIMATED -> {
                    // Полное кольцо с вращающимся градиентом (эффект «перелива»)
                    rotate(phase) {
                        drawCircle(brush = brush, style = Stroke(width = strokeW))
                    }
                    // Белая точка-«искра», бегущая по кругу
                    rotate(phase) {
                        drawCircle(
                            color = Color.White,
                            radius = strokeW * 0.55f,
                            center = Offset(c.x, c.y - radius) 
                        )
                    }
                }
            }
        }
    }
}

/** Простой синус по градусам (0..360 → -1..1) */
private fun sinDegrees(deg: Float): Float {
    val rad = Math.toRadians(deg.toDouble())
    return kotlin.math.sin(rad).toFloat()
}
