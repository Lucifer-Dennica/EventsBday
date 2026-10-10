package ru.yourname.events.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.yourname.events.R
import ru.yourname.events.data.settings.AppIcon
import ru.yourname.events.data.settings.AppTheme
import ru.yourname.events.data.settings.CalendarMarkStyle
import ru.yourname.events.data.settings.IconSwitcher
import ru.yourname.events.data.settings.SettingsRepository
import ru.yourname.events.data.settings.SortMode
import ru.yourname.events.ui.components.SwipeNavigationBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val current by SettingsRepository.themeFlow(ctx).collectAsState(initial = AppTheme.SYSTEM)
    val sortMode by SettingsRepository.sortModeFlow(ctx).collectAsState(initial = SortMode.BY_DATE)
    val markStyle by SettingsRepository.calendarMarkStyleFlow(ctx).collectAsState(initial = CalendarMarkStyle.FILLED_SQUARE)
    val appIcon by SettingsRepository.appIconFlow(ctx).collectAsState(initial = AppIcon.MAIN)

    var themeExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }
    var markExpanded by remember { mutableStateOf(false) }

    SwipeNavigationBox(
        onSwipeRight = { onBack() },
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Дизайн") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { p ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(p)
                    .verticalScroll(rememberScrollState())
            ) {

                // === ТЕМА ===
                SectionHeader("Тема оформления")
                ExposedDropdownMenuBox(
                    expanded = themeExpanded,
                    onExpandedChange = { themeExpanded = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = current.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Тема") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = themeExpanded,
                        onDismissRequest = { themeExpanded = false }
                    ) {
                        AppTheme.entries.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.label) },
                                onClick = {
                                    scope.launch { SettingsRepository.setTheme(ctx, t) }
                                    themeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // === СОРТИРОВКА ===
                SectionHeader("Сортировка списка")
                ExposedDropdownMenuBox(
                    expanded = sortExpanded,
                    onExpandedChange = { sortExpanded = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = sortMode.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Сортировка") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        SortMode.entries.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.label) },
                                onClick = {
                                    scope.launch { SettingsRepository.setSortMode(ctx, s) }
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // === ОТМЕТКИ КАЛЕНДАРЯ ===
                SectionHeader("Отметки в календаре")
                ExposedDropdownMenuBox(
                    expanded = markExpanded,
                    onExpandedChange = { markExpanded = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = markStyle.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Стиль отметок") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = markExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = markExpanded,
                        onDismissRequest = { markExpanded = false }
                    ) {
                        CalendarMarkStyle.entries.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.label) },
                                onClick = {
                                    scope.launch { SettingsRepository.setCalendarMarkStyle(ctx, m) }
                                    markExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // === ИКОНКА ПРИЛОЖЕНИЯ (в самом низу) ===
                SectionHeader("Иконка приложения")
                Text(
                    "Рабочий стол может обновляться не сразу",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconTile(
                        iconRes = R.mipmap.ic_launcher_main,
                        selected = appIcon == AppIcon.MAIN,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch { SettingsRepository.setAppIcon(ctx, AppIcon.MAIN) }
                            IconSwitcher.switchTo(ctx, AppIcon.MAIN)
                        }
                    )
                    IconTile(
                        iconRes = R.mipmap.ic_launcher_cake,
                        selected = appIcon == AppIcon.CAKE,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch { SettingsRepository.setAppIcon(ctx, AppIcon.CAKE) }
                            IconSwitcher.switchTo(ctx, AppIcon.CAKE)
                        }
                    )
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconTile(
                        iconRes = R.mipmap.ic_launcher_calendar,
                        selected = appIcon == AppIcon.CALENDAR,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch { SettingsRepository.setAppIcon(ctx, AppIcon.CALENDAR) }
                            IconSwitcher.switchTo(ctx, AppIcon.CALENDAR)
                        }
                    )
                    IconTile(
                        iconRes = R.mipmap.ic_launcher_gift,
                        selected = appIcon == AppIcon.GIFT,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch { SettingsRepository.setAppIcon(ctx, AppIcon.GIFT) }
                            IconSwitcher.switchTo(ctx, AppIcon.GIFT)
                        }
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun IconTile(
    iconRes: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val borderWidth = if (selected) 3.dp else 1.dp

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
    )
}
