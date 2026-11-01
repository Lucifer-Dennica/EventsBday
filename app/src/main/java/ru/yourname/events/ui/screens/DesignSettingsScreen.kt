package ru.yourname.events.ui.screens

import android.content.Context
import android.os.PowerManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.yourname.events.R
import ru.yourname.events.ads.YandexBanner
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.settings.AnimationMode
import ru.yourname.events.data.settings.AppIcon
import ru.yourname.events.data.settings.AppTheme
import ru.yourname.events.data.settings.CalendarMarkStyle
import ru.yourname.events.data.settings.CardBorderColor
import ru.yourname.events.data.settings.IconSwitcher
import ru.yourname.events.data.settings.SettingsRepository
import ru.yourname.events.data.settings.SortMode
import ru.yourname.events.ui.components.EventCard
import ru.yourname.events.ui.components.SwipeNavigationBox
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val current by SettingsRepository.themeFlow(ctx).collectAsState(initial = AppTheme.SYSTEM)
    val sortMode by SettingsRepository.sortModeFlow(ctx).collectAsState(initial = SortMode.BY_DATE)
    val markStyle by SettingsRepository.calendarMarkStyleFlow(ctx).collectAsState(initial = CalendarMarkStyle.FILLED_SQUARE)
    val appIcon by SettingsRepository.appIconFlow(ctx).collectAsState(initial = AppIcon.MAIN)
    val animEnabled by SettingsRepository.animEnabledFlow(ctx).collectAsState(initial = true)
    val animBatterySaver by SettingsRepository.animBatterySaverFlow(ctx).collectAsState(initial = false)
    val animRespectSystem by SettingsRepository.animRespectSystemFlow(ctx).collectAsState(initial = true)
    val animMode by SettingsRepository.animModeFlow(ctx).collectAsState(initial = AnimationMode.ALL_VISIBLE)
    val avatarGlow by SettingsRepository.avatarGlowFlow(ctx).collectAsState(initial = true)
    val avatarSize by SettingsRepository.avatarSizeFlow(ctx).collectAsState(initial = 54)
    val borderWidth by SettingsRepository.cardBorderWidthFlow(ctx).collectAsState(initial = 2)
    val borderColor by SettingsRepository.cardBorderColorFlow(ctx).collectAsState(initial = CardBorderColor.AUTO)
    val borderGlow by SettingsRepository.cardBorderGlowFlow(ctx).collectAsState(initial = false)

    // Демо-событие для предпросмотра
    val previewEvent = remember {
        Event(
            id = -1,
            name = "Иван Иванов",
            dayOfMonth = 15,
            month = 6,
            year = 1990,
            group = "Друзья",
            note = "Любит книги",
            cardColor = 1,
            animation = ru.yourname.events.data.model.CardAnimation.NONE
        )
    }

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
                Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState())
            ) {
                
                // === ПРЕДПРОСМОТР КАРТОЧКИ ===
                Text(
                    "Предпросмотр карточки",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                
                Box(Modifier.padding(horizontal = 16.dp)) {
                    EventCard(
                        event = previewEvent.copy(
                            cardColor = if (borderColor == CardBorderColor.AUTO) 1 else previewEvent.cardColor
                        ),
                        isSelected = false,
                        selectionMode = false,
                        showNotificationsIcon = true,
                        showPinIcon = true,
                        animationsAllowed = animEnabled,
                        borderWidth = borderWidth,
                        borderColorPref = borderColor,
                        borderGlow = borderGlow,
                        onClick = {},
                        onLongClick = {},
                        onToggleNotifications = {},
                        onTogglePin = {}
                    )
                }

                HorizontalDivider(Modifier.padding(vertical = 20.dp))

                // === ТЕМА ===
                SectionHeader("Тема оформления")
                // ... (код темы без изменений) ...
                var themeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = themeExpanded, onExpandedChange = { themeExpanded = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = current.label, onValueChange = {}, readOnly = true,
                        label = { Text("Тема") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                        AppTheme.entries.forEach { t ->
                            DropdownMenuItem(text = { Text(t.label) }, onClick = {
                                scope.launch { SettingsRepository.setTheme(ctx, t) }
                                themeExpanded = false
                            })
                        }
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 20.dp))

                // === АВАТАР ===
                SectionHeader("Аватар события")
                SwitchRow(
                    label = "Свечение вокруг аватара",
                    checked = avatarGlow,
                    onToggle = { scope.launch { SettingsRepository.setAvatarGlow(ctx, it) } }
                )
                
                Text(
                    "Размер аватара",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                
                // Компактный выбор размера чипсами
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sizeOptions = listOf(
                        44 to "S",
                        54 to "M",
                        64 to "L",
                        76 to "XL"
                    )
                    sizeOptions.forEach { (value, label) ->
                        FilterChip(
                            selected = avatarSize == value,
                            onClick = { scope.launch { SettingsRepository.setAvatarSize(ctx, value) } },
                            label = { Text(label, fontWeight = if (avatarSize == value) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 20.dp))

                // === ОБВОДКА ===
                SectionHeader("Обводка карточек")
                
                SliderRow(
                    label = "Толщина обводки",
                    value = borderWidth,
                    valueRange = 0..4,
                    suffix = " dp",
                    onChange = { scope.launch { SettingsRepository.setCardBorderWidth(ctx, it) } }
                )

                Text(
                    "Цвет обводки",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
                )
                CardBorderColor.entries.forEach { c ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .selectable(
                                selected = borderColor == c,
                                onClick = { scope.launch { SettingsRepository.setCardBorderColor(ctx, c) } }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = borderColor == c,
                            onClick = { scope.launch { SettingsRepository.setCardBorderColor(ctx, c) } }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(c.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                SwitchRow(
                    label = "Свечение вокруг карточки",
                    checked = borderGlow,
                    onToggle = { scope.launch { SettingsRepository.setCardBorderGlow(ctx, it) } }
                )

                HorizontalDivider(Modifier.padding(vertical = 20.dp))

                // === ИКОНКА И ДРУГОЕ (без изменений) ===
                SectionHeader("Иконка приложения")
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconTile(R.mipmap.ic_launcher_main, appIcon == AppIcon.MAIN, Modifier.weight(1f)) { scope.launch { SettingsRepository.setAppIcon(ctx, AppIcon.MAIN) }; IconSwitcher.switchTo(ctx, AppIcon.MAIN) }
                    IconTile(R.mipmap.ic_launcher_cake, appIcon == AppIcon.CAKE, Modifier.weight(1f)) { scope.launch { SettingsRepository.setAppIcon(ctx, AppIcon.CAKE) }; IconSwitcher.switchTo(ctx, AppIcon.CAKE) }
                    IconTile(R.mipmap.ic_launcher_calendar, appIcon == AppIcon.CALENDAR, Modifier.weight(1f)) { scope.launch { SettingsRepository.setAppIcon(ctx, AppIcon.CALENDAR) }; IconSwitcher.switchTo(ctx, AppIcon.CALENDAR) }
                    IconTile(R.mipmap.ic_launcher_gift, appIcon == AppIcon.GIFT, Modifier.weight(1f)) { scope.launch { SettingsRepository.setAppIcon(ctx, AppIcon.GIFT) }; IconSwitcher.switchTo(ctx, AppIcon.GIFT) }
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconTile(R.mipmap.ic_launcher_balloons, appIcon == AppIcon.BALLOONS, Modifier.weight(1f)) { scope.launch { SettingsRepository.setAppIcon(ctx, AppIcon.BALLOONS) }; IconSwitcher.switchTo(ctx, AppIcon.BALLOONS) }
                    IconTile(R.mipmap.ic_launcher_heart, appIcon == AppIcon.HEART, Modifier.weight(1f)) { scope.launch { SettingsRepository.setAppIcon(ctx, AppIcon.HEART) }; IconSwitcher.switchTo(ctx, AppIcon.HEART) }
                    IconTile(R.mipmap.ic_launcher_bear, appIcon == AppIcon.BEAR, Modifier.weight(1f)) { scope.launch { SettingsRepository.setAppIcon(ctx, AppIcon.BEAR) }; IconSwitcher.switchTo(ctx, AppIcon.BEAR) }
                    IconTile(R.mipmap.ic_launcher_present, appIcon == AppIcon.PRESENT, Modifier.weight(1f)) { scope.launch { SettingsRepository.setAppIcon(ctx, AppIcon.PRESENT) }; IconSwitcher.switchTo(ctx, AppIcon.PRESENT) }
                }
                
                Spacer(Modifier.height(16.dp))
                YandexBanner(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun IconTile(iconRes: Int, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val borderWidth = if (selected) 3.dp else 1.dp
    Box(modifier = modifier.aspectRatio(1f).clip(RoundedCornerShape(16.dp)).border(borderWidth, borderColor, RoundedCornerShape(16.dp)).clickable { onClick() }) {
        Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)))
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp))
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun SliderRow(label: String, value: Int, valueRange: IntRange, suffix: String, onChange: (Int) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text("$value$suffix", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value.toFloat(), onValueChange = { onChange(it.toInt()) }, valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(), steps = (valueRange.last - valueRange.first) - 1)
    }
}
