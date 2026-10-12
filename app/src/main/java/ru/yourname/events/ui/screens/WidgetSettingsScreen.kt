package ru.yourname.events.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch
import ru.yourname.events.data.model.Event
import ru.yourname.events.ui.components.BigWidgetPreview
import ru.yourname.events.ui.components.MiniWidgetPreview
import ru.yourname.events.ui.components.SwipeNavigationBox
import ru.yourname.events.widget.EventsWidget
import ru.yourname.events.widget.EventsWidgetMini
import ru.yourname.events.widget.WidgetBgMode
import ru.yourname.events.widget.WidgetSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSettingsScreen(
    events: List<Event>,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val fontScale by WidgetSettings.fontScaleFlow(ctx).collectAsState(initial = 100)
    val opacity by WidgetSettings.opacityFlow(ctx).collectAsState(initial = 100)
    val photoSize by WidgetSettings.photoSizeFlow(ctx).collectAsState(initial = 44)
    val showHeader by WidgetSettings.showHeaderFlow(ctx).collectAsState(initial = true)
    val showDate by WidgetSettings.showDateFlow(ctx).collectAsState(initial = true)
    val bgMode by WidgetSettings.bgModeFlow(ctx).collectAsState(initial = WidgetBgMode.LIGHT)
    val miniFontScale by WidgetSettings.miniFontScaleFlow(ctx).collectAsState(initial = 100)
    val miniShowHeader by WidgetSettings.miniShowHeaderFlow(ctx).collectAsState(initial = true)
    val miniDaysOnly by WidgetSettings.miniDaysOnlyFlow(ctx).collectAsState(initial = true)

    val snackbarHost = remember { SnackbarHostState() }

    fun refreshWidgets() {
        scope.launch {
            runCatching { EventsWidget().updateAll(ctx) }
            runCatching { EventsWidgetMini().updateAll(ctx) }
        }
    }

    fun refreshWidgetsWithToast() {
        scope.launch {
            runCatching { EventsWidget().updateAll(ctx) }
            runCatching { EventsWidgetMini().updateAll(ctx) }
            snackbarHost.showSnackbar("Виджеты обновлены")
        }
    }

    SwipeNavigationBox(
        onSwipeRight = { onBack() },
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHost) },
            topBar = {
                TopAppBar(
                    title = { Text("Виджеты") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { refreshWidgetsWithToast() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Обновить виджеты")
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
                    "Изменения в настройках сразу видны на предпросмотре ниже. На рабочем столе виджет обновится в течение 5–30 секунд или нажмите кнопку «Обновить».",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )

                Button(
                    onClick = { refreshWidgetsWithToast() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Обновить виджеты на рабочем столе")
                }

                SectionHeader("Предпросмотр большого виджета")
                Box(Modifier.padding(horizontal = 16.dp)) {
                    BigWidgetPreview(
                        events = events,
                        fontScale = fontScale,
                        opacity = opacity,
                        photoSize = photoSize,
                        showHeader = showHeader,
                        showDate = showDate,
                        bgMode = bgMode
                    )
                }

                SectionHeader("Предпросмотр мини-виджета")
                Box(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .width(180.dp)
                ) {
                    MiniWidgetPreview(
                        events = events,
                        fontScale = miniFontScale,
                        opacity = opacity,
                        showHeader = miniShowHeader,
                        daysOnly = miniDaysOnly,
                        bgMode = bgMode
                    )
                }

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
                    valueRange = 40..100,
                    suffix = "%",
                    onChange = {
                        scope.launch {
                            WidgetSettings.setOpacity(ctx, it)
                            refreshWidgets()
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

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
                    valueRange = 32..64,
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
                    onToggle = {
                        scope.launch {
                            WidgetSettings.setShowHeader(ctx, it)
                            refreshWidgets()
                        }
                    }
                )

                SwitchRow(
                    label = "Показывать дату",
                    checked = showDate,
                    onToggle = {
                        scope.launch {
                            WidgetSettings.setShowDate(ctx, it)
                            refreshWidgets()
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

                SectionHeader("Мини-виджет (4 плитки)")

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
                    label = "Показывать заголовок",
                    checked = miniShowHeader,
                    onToggle = {
                        scope.launch {
                            WidgetSettings.setMiniShowHeader(ctx, it)
                            refreshWidgets()
                        }
                    }
                )

                SwitchRow(
                    label = "Только количество дней",
                    checked = miniDaysOnly,
                    onToggle = {
                        scope.launch {
                            WidgetSettings.setMiniDaysOnly(ctx, it)
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
    onToggle: (Boolean) -> Unit
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
        Switch(
            checked = checked,
            onCheckedChange = onToggle
        )
    }
}
