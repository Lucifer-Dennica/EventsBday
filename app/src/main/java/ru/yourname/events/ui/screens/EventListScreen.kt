package ru.yourname.events.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import ru.yourname.events.ads.YandexBanner
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.model.SocialPlatform
import ru.yourname.events.data.settings.CalendarMarkStyle
import ru.yourname.events.data.settings.SortMode
import ru.yourname.events.data.settings.SwipeAction
import ru.yourname.events.data.settings.ViewMode
import ru.yourname.events.ui.components.CalendarView
import ru.yourname.events.ui.components.EventCard
import ru.yourname.events.ui.components.SwipeNavigationBox
import java.time.LocalDate

private const val AD_EVERY_N_ITEMS = 10

/** Тип элемента в списке — заголовок секции или событие. */
private sealed interface ListRow {
    data class Header(val title: String, val key: String, val secondary: Boolean = false) : ListRow
    data class EventItem(val event: Event) : ListRow
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EventListScreen(
    events: List<Event>,
    swipesEnabled: Boolean,
    swipeLeftAction: SwipeAction,
    swipeRightAction: SwipeAction,
    swipeToSettingsEnabled: Boolean,
    sortMode: SortMode,
    viewMode: ViewMode,
    calendarMarkStyle: CalendarMarkStyle,
    animationsAllowed: Boolean,
    onToggleViewMode: () -> Unit,
    onAddClick: () -> Unit,
    onEventClick: (Event) -> Unit,
    onImportContacts: () -> Unit,
    onOpenSettings: () -> Unit,
    onDeleteMany: (List<Event>) -> Unit,
    onToggleNotifications: (Event) -> Unit,
    onTogglePin: (Event) -> Unit,
    onEditEvent: (Event) -> Unit
) {
    val ctx = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var eventToDelete by remember { mutableStateOf<Event?>(null) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    var selectedCalendarDate by remember { mutableStateOf<LocalDate?>(null) }

    BackHandler(enabled = viewMode == ViewMode.CALENDAR) {
        selectedCalendarDate = null
        onToggleViewMode()
    }

    val showNotificationsIcon = swipeLeftAction != SwipeAction.NOTIFICATIONS &&
            swipeRightAction != SwipeAction.NOTIFICATIONS
    val showPinIcon = swipeLeftAction != SwipeAction.PIN &&
            swipeRightAction != SwipeAction.PIN

    fun dialEvent(event: Event) {
        val phone = event.socialLinks.firstOrNull {
            it.platform == SocialPlatform.PHONE ||
                    it.platform == SocialPlatform.WHATSAPP ||
                    it.platform == SocialPlatform.VIBER
        }?.value
        if (phone.isNullOrBlank()) {
            scope.launch { snackbar.showSnackbar("Нет телефона в контактах события") }
            return
        }
        val digits = phone.filter { it.isDigit() || it == '+' }
        if (digits.isBlank()) {
            scope.launch { snackbar.showSnackbar("Не удалось извлечь номер") }
            return
        }
        runCatching {
            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")))
        }
    }

    val allGroups = remember(events) {
        events.map { it.group }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filtered = remember(events, query, selectedGroup) {
        val q = query.trim()
        events
            .filter { e ->
                if (q.isBlank()) true
                else {
                    e.name.contains(q, ignoreCase = true) ||
                            e.note.contains(q, ignoreCase = true) ||
                            e.group.contains(q, ignoreCase = true) ||
                            e.checklist.any { it.text.contains(q, ignoreCase = true) }
                }
            }
            .filter { selectedGroup == null || it.group == selectedGroup }
    }

    val today = LocalDate.now()
    val sorted = remember(filtered, sortMode) {
        val base = when (sortMode) {
            SortMode.BY_DATE -> filtered.sortedBy { it.daysUntil(today) }
            SortMode.BY_NAME -> filtered.sortedBy { it.name.lowercase() }
            SortMode.BY_GROUP -> filtered.sortedWith(
                compareBy<Event> { it.group.lowercase() }.thenBy { it.daysUntil(today) }
            )
        }
        base.sortedByDescending { it.isPinned }
    }

    val pinned = remember(sorted) { sorted.filter { it.isPinned } }

    val showTodaySection = selectedGroup == null && query.isBlank() && sortMode == SortMode.BY_DATE
    val todayEvents = remember(sorted, showTodaySection) {
        if (showTodaySection) sorted.filter { it.daysUntil(today) == 0L && !it.isPinned }
        else emptyList()
    }

    val others = remember(sorted, todayEvents) {
        val todayIds = todayEvents.map { it.id }.toSet()
        sorted.filter { !it.isPinned && it.id !in todayIds }
    }

    // ЕДИНЫЙ список с заголовками и событиями — единый счётчик для рекламы
    val listRows: List<ListRow> = remember(pinned, todayEvents, others) {
        buildList {
            if (pinned.isNotEmpty()) {
                add(ListRow.Header("Закреплённые", "h_pin"))
                pinned.forEach { add(ListRow.EventItem(it)) }
            }
            if (todayEvents.isNotEmpty()) {
                add(ListRow.Header("Сегодня", "h_today"))
                todayEvents.forEach { add(ListRow.EventItem(it)) }
                add(ListRow.Header("Все события", "h_all", secondary = true))
            }
            others.forEach { add(ListRow.EventItem(it)) }
        }
    }

    // Позиции в listRows, ПОСЛЕ которых нужно вставить рекламу
    // Счётчик сквозной — не сбрасывается на границах секций
    val adAfterIndexes: Set<Int> = remember(listRows) {
        val result = mutableSetOf<Int>()
        var eventCounter = 0
        listRows.forEachIndexed { idx, row ->
            if (row is ListRow.EventItem) {
                eventCounter++
                if (eventCounter % AD_EVERY_N_ITEMS == 0) {
                    result.add(idx)
                }
            }
        }
        // Не показываем рекламу после самого последнего элемента
        result.remove(listRows.lastIndex)
        result
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onImportContacts()
            scope.launch { snackbar.showSnackbar("Импорт запущен") }
        } else {
            scope.launch { snackbar.showSnackbar("Разрешение на контакты не выдано") }
        }
    }

    fun requestImport() {
        val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
        if (granted) {
            onImportContacts()
            scope.launch { snackbar.showSnackbar("Импорт запущен") }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else onImportContacts()
    }

    val selectionMode = selectedIds.isNotEmpty()

    val navigationSwipeEnabled = swipeToSettingsEnabled &&
            !selectionMode &&
            viewMode == ViewMode.LIST

    fun handleCardClick(event: Event) {
        if (selectionMode) {
            selectedIds = if (event.id in selectedIds) selectedIds - event.id
                          else selectedIds + event.id
        } else {
            onEventClick(event)
        }
    }

    fun handleCardLongClick(event: Event) {
        selectedIds = if (event.id in selectedIds) selectedIds - event.id
                      else selectedIds + event.id
    }

    SwipeNavigationBox(
        onSwipeLeft = if (navigationSwipeEnabled) {
            { onOpenSettings() }
        } else null,
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                if (selectionMode) {
                    TopAppBar(
                        title = { Text("Выбрано: ${selectedIds.size}") },
                        navigationIcon = {
                            IconButton(onClick = { selectedIds = emptySet() }) {
                                Icon(Icons.Default.Close, contentDescription = "Отменить")
                            }
                        },
                        actions = {
                            IconButton(onClick = { showBulkDeleteConfirm = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                } else {
                    TopAppBar(
                        title = { Text("Мои События") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        actions = {
                            IconButton(onClick = onToggleViewMode) {
                                Icon(
                                    imageVector = if (viewMode == ViewMode.LIST)
                                        Icons.Default.CalendarMonth else Icons.Default.List,
                                    contentDescription = "Переключить вид"
                                )
                            }
                            IconButton(onClick = { requestImport() }) {
                                Icon(Icons.Default.Person, contentDescription = "Импорт")
                            }
                            IconButton(onClick = onOpenSettings) {
                                Icon(Icons.Default.Settings, contentDescription = "Настройки")
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (!selectionMode) {
                    ExtendedFloatingActionButton(
                        onClick = onAddClick,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Добавить") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {

                if (viewMode == ViewMode.CALENDAR) {
                    Column(Modifier.fillMaxSize()) {
                        CalendarView(
                            events = events,
                            selectedDate = selectedCalendarDate,
                            markStyle = calendarMarkStyle,
                            onDayClick = { date ->
                                selectedCalendarDate = if (selectedCalendarDate == date) null else date
                            }
                        )
                        HorizontalDivider(Modifier.padding(vertical = 10.dp))
                        val dayEvents = selectedCalendarDate?.let { date ->
                            events.filter { e ->
                                runCatching {
                                    LocalDate.of(date.year, e.month, e.dayOfMonth) == date
                                }.getOrDefault(false)
                            }
                        }.orEmpty()
                        when {
                            selectedCalendarDate == null -> {
                                Box(Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center) {
                                    Text("Выберите день в календаре",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            dayEvents.isEmpty() -> {
                                Box(Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center) {
                                    Text("Нет событий в этот день",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(dayEvents, key = { "cal_${it.id}" }) { event ->
                                        EventCard(
                                            event = event,
                                            isSelected = false,
                                            selectionMode = false,
                                            showNotificationsIcon = showNotificationsIcon,
                                            showPinIcon = showPinIcon,
                                            animationsAllowed = animationsAllowed,
                                            onClick = { onEventClick(event) },
                                            onLongClick = {},
                                            onToggleNotifications = { onToggleNotifications(event) },
                                            onTogglePin = { onTogglePin(event) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Поиск: имя, заметка, группа, чек-лист") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    if (allGroups.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedGroup == null,
                                    onClick = { selectedGroup = null },
                                    label = { Text("Все") }
                                )
                            }
                            items(allGroups) { g ->
                                FilterChip(
                                    selected = selectedGroup == g,
                                    onClick = { selectedGroup = if (selectedGroup == g) null else g },
                                    label = { Text(g) }
                                )
                            }
                        }
                    }

                    if (listRows.isEmpty()) {
                        EmptyState(Modifier.fillMaxSize(), query)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listRows.forEachIndexed { index, row ->
                                when (row) {
                                    is ListRow.Header -> {
                                        item(key = row.key) {
                                            if (row.secondary) {
                                                Spacer(Modifier.height(4.dp))
                                            }
                                            Text(
                                                row.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (row.secondary)
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                else
                                                    MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    is ListRow.EventItem -> {
                                        item(key = "event_${row.event.id}") {
                                            EventRow(
                                                event = row.event,
                                                swipesEnabled = swipesEnabled && !selectionMode,
                                                swipeLeftAction = swipeLeftAction,
                                                swipeRightAction = swipeRightAction,
                                                isSelected = row.event.id in selectedIds,
                                                selectionMode = selectionMode,
                                                showNotificationsIcon = showNotificationsIcon,
                                                showPinIcon = showPinIcon,
                                                animationsAllowed = animationsAllowed,
                                                onClick = { handleCardClick(row.event) },
                                                onLongClick = { handleCardLongClick(row.event) },
                                                onDelete = { eventToDelete = row.event },
                                                onToggleNotifications = { onToggleNotifications(row.event) },
                                                onTogglePin = { onTogglePin(row.event) },
                                                onEdit = { onEditEvent(row.event) },
                                                onCall = { dialEvent(row.event) }
                                            )
                                        }
                                    }
                                }

                                // Реклама как карточка — после каждого 10-го события
                                if (index in adAfterIndexes) {
                                    item(key = "ad_$index") {
                                        AdCard()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    eventToDelete?.let { ev ->
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text("Удалить событие?") },
            text = { Text("«${ev.name}» будет удалено.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteMany(listOf(ev))
                    eventToDelete = null
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) { Text("Отмена") }
            }
        )
    }

    if (showBulkDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirm = false },
            title = { Text("Удалить ${selectedIds.size} событий?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    val toDelete = events.filter { it.id in selectedIds }
                    onDeleteMany(toDelete)
                    selectedIds = emptySet()
                    showBulkDeleteConfirm = false
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteConfirm = false }) { Text("Отмена") }
            }
        )
    }
}

/** Реклама, оформленная как карточка — визуально похожа на EventCard. */
@Composable
private fun AdCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            YandexBanner(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventRow(
    event: Event,
    swipesEnabled: Boolean,
    swipeLeftAction: SwipeAction,
    swipeRightAction: SwipeAction,
    isSelected: Boolean,
    selectionMode: Boolean,
    showNotificationsIcon: Boolean,
    showPinIcon: Boolean,
    animationsAllowed: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleNotifications: () -> Unit,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onCall: () -> Unit
) {
    fun dispatch(action: SwipeAction) {
        when (action) {
            SwipeAction.NOTHING -> {}
            SwipeAction.DELETE -> onDelete()
            SwipeAction.PIN -> onTogglePin()
            SwipeAction.NOTIFICATIONS -> onToggleNotifications()
            SwipeAction.EDIT -> onEdit()
            SwipeAction.CALL -> onCall()
        }
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> dispatch(swipeLeftAction)
                SwipeToDismissBoxValue.StartToEnd -> dispatch(swipeRightAction)
                else -> {}
            }
            false
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.55f }
    )

    val leftEnabled = swipesEnabled && swipeLeftAction != SwipeAction.NOTHING
    val rightEnabled = swipesEnabled && swipeRightAction != SwipeAction.NOTHING

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = rightEnabled,
        enableDismissFromEndToStart = leftEnabled,
        backgroundContent = {
            val target = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> swipeLeftAction
                SwipeToDismissBoxValue.StartToEnd -> swipeRightAction
                else -> SwipeAction.NOTHING
            }
            if (target != SwipeAction.NOTHING) {
                val icon = actionIcon(target)
                val bg = actionBg(target)
                val fg = actionFg(target)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = bg,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 20.dp),
                    contentAlignment =
                        if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                            Alignment.CenterEnd
                        else Alignment.CenterStart
                ) {
                    Icon(icon, contentDescription = target.label, tint = fg)
                }
            }
        }
    ) {
        EventCard(
            event = event,
            isSelected = isSelected,
            selectionMode = selectionMode,
            showNotificationsIcon = showNotificationsIcon,
            showPinIcon = showPinIcon,
            animationsAllowed = animationsAllowed,
            onClick = onClick,
            onLongClick = onLongClick,
            onToggleNotifications = onToggleNotifications,
            onTogglePin = onTogglePin
        )
    }
}

private fun actionIcon(action: SwipeAction): ImageVector = when (action) {
    SwipeAction.DELETE -> Icons.Default.Delete
    SwipeAction.PIN -> Icons.Default.PushPin
    SwipeAction.NOTIFICATIONS -> Icons.Default.NotificationsOff
    SwipeAction.EDIT -> Icons.Default.Edit
    SwipeAction.CALL -> Icons.Default.Call
    SwipeAction.NOTHING -> Icons.Default.Delete
}

@Composable
private fun actionBg(action: SwipeAction): Color = when (action) {
    SwipeAction.DELETE -> MaterialTheme.colorScheme.errorContainer
    SwipeAction.PIN -> MaterialTheme.colorScheme.primaryContainer
    SwipeAction.NOTIFICATIONS -> MaterialTheme.colorScheme.tertiaryContainer
    SwipeAction.EDIT -> MaterialTheme.colorScheme.secondaryContainer
    SwipeAction.CALL -> MaterialTheme.colorScheme.primaryContainer
    SwipeAction.NOTHING -> Color.Transparent
}

@Composable
private fun actionFg(action: SwipeAction): Color = when (action) {
    SwipeAction.DELETE -> MaterialTheme.colorScheme.onErrorContainer
    SwipeAction.PIN -> MaterialTheme.colorScheme.onPrimaryContainer
    SwipeAction.NOTIFICATIONS -> MaterialTheme.colorScheme.onTertiaryContainer
    SwipeAction.EDIT -> MaterialTheme.colorScheme.onSecondaryContainer
    SwipeAction.CALL -> MaterialTheme.colorScheme.onPrimaryContainer
    SwipeAction.NOTHING -> Color.Transparent
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier, query: String = "") {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (query.isBlank()) "🎂" else "🔍",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (query.isBlank()) "Пока нет событий" else "Ничего не найдено",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (query.isBlank()) "Импортируйте из контактов или добавьте вручную"
                else "Попробуйте изменить запрос",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
