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
import ru.yourname.events.ui.components.SwipeNavigationBox
import ru.yourname.events.widget.EventsWidget
import ru.yourname.events.widget.EventsWidgetMini
import ru.yourname.events.widget.WidgetBgMode
import ru.yourname.events.widget.WidgetSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val fontScale by WidgetSettings.fontScaleFlow(ctx).collectAsState(initial = 100)
    val opacity by WidgetSettings.opacityFlow(ctx).collectAsState(initial = 100)
    val photoSize by WidgetSettings.photoSizeFlow(ctx).collectAsState(initial = 48)
    val showHeader by WidgetSettings.showHeaderFlow(ctx).collectAsState(initial = true)
    val showDate by WidgetSettings.showDateFlow(ctx).collectAsState(initial = true)
    val showGroup by WidgetSettings.showGroupFlow(ctx).collectAsState(initial = true)
    val showAge by WidgetSettings.showAgeFlow(ctx).collectAsState(initial = true)
    val bgMode by WidgetSettings.bgModeFlow(ctx).collectAsState(initial = WidgetBgMode.SYSTEM)
    val miniFontScale by WidgetSettings.miniFontScaleFlow(ctx).collectAsState(initial = 100)
    val miniShowHeader by WidgetSettings.miniShowHeaderFlow(ctx).collectAsState(initial = true)

    // Обновляет оба виджета после изменения настроек
    fun refreshWidgets() {
        scope.launch {
            runCatching { EventsWidget().updateAll(ctx) }
            runCatching { EventsWidgetMini().updateAll(ctx) }
        }
    }

    SwipeNavigationBox(
        onSwipeRight = { onBack() },
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Виджеты") },
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

                Text(
                    "Виджеты добавляются через долгое нажатие на пустой области рабочего стола → «Виджеты». Изменения применяются к уже добавленным виджетам через 2–5 секунд.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )

                // ====== ОБЩИЕ ======
                SectionHeader("Общие")

                Text(
                    "Фон виджета",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                WidgetBgMode.entries.forEach { mode ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = bgMode == mode,
                                onClick = {
                                    scope.launch {
                                        WidgetSettings.setBgMode(ctx, mode)
                                        refreshWidgets()
                                    }
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = bgMode == mode,
                            onClick = {
                                scope.launch {
                                    WidgetSettings.setBgMode(ctx, mode)
                                    refreshWidgets()
                                }
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(mode.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                SliderRow(
                    label = "Прозрачность фона",
                    value = opacity,
                    valueRange = 20..100,
                    suffix = "%",
                    onChange = {
                        scope.launch {
                            WidgetSettings.setOpacity(ctx, it)
                            refreshWidgets()
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

                // ====== БОЛЬШОЙ ВИДЖЕТ ======
                SectionHeader("Большой виджет")

                SliderRow(
                    label = "Размер шрифта",
                    value = fontScale,
                    valueRange = 80..140,
                    suffix = "%",
                    onChange = {
                        scope.launch {
                            WidgetSettings.setFontScale(ctx, it)
                            refreshWidgets()
                        }
                    }
                )

                SliderRow(
                    label = "Размер фото",
                    value = photoSize,
                    valueRange = 32..72,
                    suffix = " dp",
                    onChange = {
                        scope.launch {
                            WidgetSettings.setPhotoSize(ctx, it)
                            refreshWidgets()
                        }
                    }
                )

                SwitchRow(
                    label = "Показывать заголовок",
                    checked = showHeader,
                    onChange = {
                        scope.launch {
                            WidgetSettings.setShowHeader(ctx, it)
                            refreshWidgets()
                        }
                    }
                )

                SwitchRow(
                    label = "Показывать дату",
                    checked = showDate,
                    onChange = {
                        scope.launch {
                            WidgetSettings.setShowDate(ctx, it)
                            refreshWidgets()
                        }
                    }
                )

                SwitchRow(
                    label = "Показывать возраст",
                    checked = showAge,
                    onChange = {
                        scope.launch {
                            WidgetSettings.setShowAge(ctx, it)
                            refreshWidgets()
                        }
                    }
                )

                SwitchRow(
                    label = "Показывать группу",
                    checked = showGroup,
                    onChange = {
                        scope.launch {
                            WidgetSettings.setShowGroup(ctx, it)
                            refreshWidgets()
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

                // ====== МИНИ-КАЛЕНДАРЬ ======
                SectionHeader("Мини-календарь")

                Text(
                    "Показывает текущий месяц с отметками событий. Сегодняшний день выделен синим.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )

                SliderRow(
                    label = "Размер шрифта",
                    value = miniFontScale,
                    valueRange = 80..140,
                    suffix = "%",
                    onChange = {
                        scope.launch {
                            WidgetSettings.setMiniFontScale(ctx, it)
                            refreshWidgets()
                        }
                    }
                )

                SwitchRow(
                    label = "Показывать название месяца",
                    checked = miniShowHeader,
                    onChange = {
                        scope.launch {
                            WidgetSettings.setMiniShowHeader(ctx, it)
                            refreshWidgets()
                        }
                    }
                )

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

@Composable
private fun SliderRow(
    label: String,
    value: Int,
    valueRange: IntRange,
    suffix: String,
    onChange: (Int) -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                "$value$suffix",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = (valueRange.last - valueRange.first) / 4 - 1
        )
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
