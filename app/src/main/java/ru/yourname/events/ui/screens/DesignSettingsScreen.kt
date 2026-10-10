package ru.yourname.events.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.yourname.events.data.settings.AppTheme
import ru.yourname.events.data.settings.CalendarMarkStyle
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

    var themeExpanded by remember { mutableStateOf(false) }

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
            Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState())) {

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

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                SectionHeader("Сортировка списка")
                SortMode.entries.forEach { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = sortMode == s,
                                onClick = { scope.launch { SettingsRepository.setSortMode(ctx, s) } }
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortMode == s,
                            onClick = { scope.launch { SettingsRepository.setSortMode(ctx, s) } }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(s.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                SectionHeader("Отметки в календаре")
                CalendarMarkStyle.entries.forEach { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = markStyle == m,
                                onClick = { scope.launch { SettingsRepository.setCalendarMarkStyle(ctx, m) } }
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = markStyle == m,
                            onClick = { scope.launch { SettingsRepository.setCalendarMarkStyle(ctx, m) } }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(m.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}
