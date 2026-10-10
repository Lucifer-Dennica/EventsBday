package ru.yourname.events.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.yourname.events.data.settings.NotificationFrequency
import ru.yourname.events.data.settings.NotificationMode
import ru.yourname.events.data.settings.SettingsRepository
import ru.yourname.events.notif.NotificationScheduler
import ru.yourname.events.ui.components.SwipeNavigationBox
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val enabled by SettingsRepository.notifEnabledFlow(ctx).collectAsState(initial = true)
    val mode by SettingsRepository.notifModeFlow(ctx).collectAsState(initial = NotificationMode.ALL)
    val freq by SettingsRepository.notifFrequencyFlow(ctx).collectAsState(initial = NotificationFrequency.EVERY_DAY)
    val daysBefore by SettingsRepository.notifDaysBeforeFlow(ctx).collectAsState(initial = 7)
    val time by SettingsRepository.notifTimeFlow(ctx).collectAsState(initial = 9 to 0)

    var showTimePicker by remember { mutableStateOf(false) }

    fun reschedule() {
        scope.launch { NotificationScheduler.schedule(ctx) }
    }

    SwipeNavigationBox(
        onSwipeRight = { onBack() },
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Уведомления") },
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

                ListItem(
                    headlineContent = {
                        Text("Включить уведомления",
                            style = MaterialTheme.typography.titleMedium)
                    },
                    supportingContent = { Text("Напоминать о предстоящих событиях") },
                    trailingContent = {
                        Switch(
                            checked = enabled,
                            onCheckedChange = {
                                scope.launch {
                                    SettingsRepository.setNotifEnabled(ctx, it)
                                    if (it) reschedule() else NotificationScheduler.cancel(ctx)
                                }
                            }
                        )
                    }
                )

                if (!enabled) return@Column

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                SectionHeader("О каких событиях уведомлять")
                NotificationMode.entries.forEach { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = mode == m,
                                onClick = {
                                    scope.launch {
                                        SettingsRepository.setNotifMode(ctx, m)
                                        reschedule()
                                    }
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == m,
                            onClick = {
                                scope.launch {
                                    SettingsRepository.setNotifMode(ctx, m)
                                    reschedule()
                                }
                            }
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(m.label, style = MaterialTheme.typography.bodyLarge)
                            if (m == NotificationMode.CUSTOM) {
                                Text("Уведомления по событиям с включённым тумблером",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                SectionHeader("За сколько дней начинать напоминать")
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = {
                            scope.launch {
                                SettingsRepository.setNotifDaysBefore(ctx, (daysBefore - 1).coerceAtLeast(0))
                                reschedule()
                            }
                        },
                        shape = CircleShape
                    ) { Icon(Icons.Default.Remove, contentDescription = "Меньше") }

                    Text(
                        text = daysBefore.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 28.dp)
                    )

                    FilledIconButton(
                        onClick = {
                            scope.launch {
                                SettingsRepository.setNotifDaysBefore(ctx, (daysBefore + 1).coerceAtMost(30))
                                reschedule()
                            }
                        },
                        shape = CircleShape
                    ) { Icon(Icons.Default.Add, contentDescription = "Больше") }
                }
                Text(
                    text = "дней",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                SectionHeader("Время уведомлений")
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { showTimePicker = true }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Показывать в",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            String.format(Locale.US, "%02d:%02d", time.first, time.second),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                SectionHeader("Частота напоминаний")
                NotificationFrequency.entries.forEach { f ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = freq == f,
                                onClick = {
                                    scope.launch {
                                        SettingsRepository.setNotifFrequency(ctx, f)
                                        reschedule()
                                    }
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = freq == f,
                            onClick = {
                                scope.launch {
                                    SettingsRepository.setNotifFrequency(ctx, f)
                                    reschedule()
                                }
                            }
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(f.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = time.first,
            initialMinute = time.second,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    "Время уведомлений",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(280.dp)) {
                        TimePicker(state = state)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        SettingsRepository.setNotifTime(ctx, state.hour, state.minute)
                        reschedule()
                    }
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Отмена") }
            }
        )
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
