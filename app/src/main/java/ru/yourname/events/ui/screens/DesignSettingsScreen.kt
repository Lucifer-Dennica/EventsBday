package ru.yourname.events.ui.screens

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
import ru.yourname.events.data.settings.AppIcon
import ru.yourname.events.data.settings.AppTheme
import ru.yourname.events.data.settings.AvatarShape
import ru.yourname.events.data.settings.CardGlowStyle
import ru.yourname.events.data.settings.DaysFormat
import ru.yourname.events.data.settings.IconSwitcher
import ru.yourname.events.data.settings.ListDensity
import ru.yourname.events.data.settings.NeonMode
import ru.yourname.events.data.settings.SettingsRepository
import ru.yourname.events.data.settings.TitleFontSize
import ru.yourname.events.ui.components.AvatarConfig
import ru.yourname.events.ui.components.DesignConfig
import ru.yourname.events.ui.components.EventCard
import ru.yourname.events.ui.components.LocalAvatarConfig
import ru.yourname.events.ui.components.LocalDesignConfig
import ru.yourname.events.ui.components.SwipeNavigationBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val current by SettingsRepository.themeFlow(ctx).collectAsState(initial = AppTheme.SYSTEM)
    val appIcon by SettingsRepository.appIconFlow(ctx).collectAsState(initial = AppIcon.MAIN)
    val animEnabled by SettingsRepository.animEnabledFlow(ctx).collectAsState(initial = true)

    val avatarGlow by SettingsRepository.avatarGlowFlow(ctx).collectAsState(initial = true)
    val avatarSize by SettingsRepository.avatarSizeFlow(ctx).collectAsState(initial = 54)
    val avatarShape by SettingsRepository.avatarShapeFlow(ctx).collectAsState(initial = AvatarShape.CIRCLE)

    val borderWidth by SettingsRepository.cardBorderWidthFlow(ctx).collectAsState(initial = 2)
    val neonMode by SettingsRepository.neonModeFlow(ctx).collectAsState(initial = NeonMode.OFF)
    val glowStyle by SettingsRepository.cardGlowStyleFlow(ctx).collectAsState(initial = CardGlowStyle.NONE)
    val glowIntensity by SettingsRepository.cardGlowIntensityFlow(ctx).collectAsState(initial = 60)

    val cornerRadius by SettingsRepository.cardCornerRadiusFlow(ctx).collectAsState(initial = 18)
    val density by SettingsRepository.listDensityFlow(ctx).collectAsState(initial = ListDensity.NORMAL)
    val showZodiac by SettingsRepository.showZodiacFlow(ctx).collectAsState(initial = true)
    val titleFontSize by SettingsRepository.titleFontSizeFlow(ctx).collectAsState(initial = TitleFontSize.MEDIUM)
    val daysFormat by SettingsRepository.daysFormatFlow(ctx).collectAsState(initial = DaysFormat.SHORT)
    val showDayOfWeek by SettingsRepository.showDayOfWeekFlow(ctx).collectAsState(initial = false)

    val previewEvent = remember {
        Event(
            id = -1, name = "Иван Иванов", dayOfMonth = 15, month = 6, year = 1990,
            group = "Друзья", note = "Любит книги", cardColor = 1,
            animation = ru.yourname.events.data.model.CardAnimation.NONE
        )
    }

    val previewAvatarConfig = AvatarConfig(glow = avatarGlow, size = avatarSize, shape = avatarShape)
    val previewDesignConfig = DesignConfig(
        cornerRadius = cornerRadius,
        density = density,
        titleFontSize = titleFontSize,
        daysFormat = daysFormat,
        showDayOfWeek = showDayOfWeek,
        neonMode = neonMode,
        glowStyle = glowStyle,
        glowIntensity = glowIntensity,
        showZodiac = showZodiac
    )

    SwipeNavigationBox(onSwipeRight = { onBack() }, modifier = Modifier.fillMaxSize()) {
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

                SectionHeader("Предпросмотр карточки")
                CompositionLocalProvider(
                    LocalAvatarConfig provides previewAvatarConfig,
                    LocalDesignConfig provides previewDesignConfig
                ) {
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        EventCard(
                            event = previewEvent, isSelected = false, selectionMode = false,
                            showNotificationsIcon = true, showPinIcon = true,
                            animationsAllowed = animEnabled,
                            borderWidth = borderWidth,
                            borderColorPref = ru.yourname.events.data.settings.CardBorderColor.AUTO,
                            borderGlow = false,
                            neonBorder = false,
                            onClick = {}, onLongClick = {},
                            onToggleNotifications = {}, onTogglePin = {}
                        )
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 20.dp))

                // === ТЕМА ===
                SectionHeader("Тема оформления")
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
                SwitchRow("Свечение вокруг аватара", avatarGlow) {
                    scope.launch { SettingsRepository.setAvatarGlow(ctx, it) }
                }
                Text("Форма", style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AvatarShape.entries.forEach { shape ->
                        FilterChip(
                            selected = avatarShape == shape,
                            onClick = { scope.launch { SettingsRepository.setAvatarShape(ctx, shape) } },
                            label = { Text(shape.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Text("Размер", style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(44 to "S", 54 to "M", 64 to "L", 76 to "XL").forEach { (value, label) ->
                        FilterChip(
                            selected = avatarSize == value,
                            onClick = { scope.launch { SettingsRepository.setAvatarSize(ctx, value) } },
                            label = { Text(label, fontWeight = if (avatarSize == value) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 20.dp))

                // === ФОРМА КАРТОЧКИ ===
                SectionHeader("Форма карточек")
                SliderRow("Скругление углов", cornerRadius, 0..24, " dp") {
                    scope.launch { SettingsRepository.setCardCornerRadius(ctx, it) }
                }
                SliderRow("Толщина обводки", borderWidth, 0..4, " dp") {
                    scope.launch { SettingsRepository.setCardBorderWidth(ctx, it) }
                }

                HorizontalDivider(Modifier.padding(vertical = 20.dp))

                // === НЕОНОВАЯ ОБВОДКА ===
                SectionHeader("Неоновая обводка")
                Text("Цвет обводки задаётся индивидуально для каждой карточки в её редакторе.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

                NeonMode.entries.forEach { m ->
                    RadioOption(m.label, neonMode == m) {
                        scope.launch { SettingsRepository.setNeonMode(ctx, m) }
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 20.dp))

                // === СВЕЧЕНИЕ / ТЕНЬ ===
                SectionHeader("Свечение и тень")
                CardGlowStyle.entries.forEach { g ->
                    RadioOption(g.label, glowStyle == g) {
                        scope.launch { SettingsRepository.setCardGlowStyle(ctx, g) }
                    }
                }
                if (glowStyle != CardGlowStyle.NONE) {
                    SliderRow("Интенсивность", glowIntensity, 0..100, "%") {
                        scope.launch { SettingsRepository.setCardGlowIntensity(ctx, it) }
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 20.dp))

                // === ПЛОТНОСТЬ ===
                SectionHeader("Плотность списка")
                ListDensity.entries.forEach { d ->
                    RadioOption(d.label, density == d) {
                        scope.launch { SettingsRepository.setListDensity(ctx, d) }
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 20.dp))

                // === ШРИФТ ===
                SectionHeader("Размер шрифта заголовка")
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TitleFontSize.entries.forEach { s ->
                        FilterChip(
                            selected = titleFontSize == s,
                            onClick = { scope.launch { SettingsRepository.setTitleFontSize(ctx, s) } },
                            label = { Text(s.label, fontWeight = if (titleFontSize == s) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 20.dp))

                // === ФОРМАТ ДНЕЙ ===
                SectionHeader("Формат счётчика дней")
                DaysFormat.entries.forEach { f ->
                    RadioOption(f.label, daysFormat == f) {
                        scope.launch { SettingsRepository.setDaysFormat(ctx, f) }
                    }
                }

                SwitchRow("Показывать день недели на карточке", showDayOfWeek) {
                    scope.launch { SettingsRepository.setShowDayOfWeek(ctx, it) }
                }

                HorizontalDivider(Modifier.padding(vertical = 20.dp))

                // === АСТРОЛОГИЯ ===
                SectionHeader("Детали события")
                SwitchRow("Показывать блок астрологии", showZodiac) {
                    scope.launch { SettingsRepository.setShowZodiac(ctx, it) }
                }

                HorizontalDivider(Modifier.padding(vertical = 20.dp))

                // === ИКОНКА ===
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
private fun RadioOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
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
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp))
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
        Slider(value = value.toFloat(), onValueChange = { onChange(it.toInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = (valueRange.last - valueRange.first) - 1)
    }
}
