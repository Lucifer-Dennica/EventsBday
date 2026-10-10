package ru.yourname.events.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import ru.yourname.events.data.settings.SettingsRepository
import ru.yourname.events.data.settings.SwipeAction
import ru.yourname.events.ui.components.SwipeNavigationBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorSettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val swipesEnabled by SettingsRepository.swipesEnabledFlow(ctx).collectAsState(initial = true)
    val swipeToSettings by SettingsRepository.swipeToSettingsEnabledFlow(ctx).collectAsState(initial = true)
    val leftAction by SettingsRepository.swipeLeftActionFlow(ctx).collectAsState(initial = SwipeAction.DELETE)
    val rightAction by SettingsRepository.swipeRightActionFlow(ctx).collectAsState(initial = SwipeAction.NOTHING)

    SwipeNavigationBox(
        onSwipeRight = { onBack() },
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Поведение") },
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

                SectionHeader("Жесты")

                ListItem(
                    headlineContent = {
                        Text("Свайпы по карточкам",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    },
                    supportingContent = { Text("Движение пальцем по карточке для действий") },
                    trailingContent = {
                        Switch(
                            checked = swipesEnabled,
                            onCheckedChange = {
                                scope.launch { SettingsRepository.setSwipesEnabled(ctx, it) }
                            }
                        )
                    }
                )

                if (swipesEnabled) {
                    Spacer(Modifier.height(8.dp))

                    ActionDropdown(
                        title = "Свайп влево",
                        subtitle = "Справа налево",
                        current = leftAction,
                        exclude = rightAction,
                        onPick = { scope.launch { SettingsRepository.setSwipeLeftAction(ctx, it) } }
                    )

                    Spacer(Modifier.height(12.dp))

                    ActionDropdown(
                        title = "Свайп вправо",
                        subtitle = "Слева направо",
                        current = rightAction,
                        exclude = leftAction,
                        onPick = { scope.launch { SettingsRepository.setSwipeRightAction(ctx, it) } }
                    )
                }

                Spacer(Modifier.height(12.dp))

                ListItem(
                    headlineContent = {
                        Text("Свайп между главным экраном и настройками",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    },
                    supportingContent = {
                        Text(if (swipeToSettings)
                            "Свайп влево на главном → настройки; свайп вправо в настройках → назад"
                            else "Отключено")
                    },
                    trailingContent = {
                        Switch(
                            checked = swipeToSettings,
                            onCheckedChange = {
                                scope.launch { SettingsRepository.setSwipeToSettingsEnabled(ctx, it) }
                            }
                        )
                    }
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionDropdown(
    title: String,
    subtitle: String,
    current: SwipeAction,
    exclude: SwipeAction,
    onPick: (SwipeAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = current.label,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                SwipeAction.entries.forEach { a ->
                    if (a != exclude) {
                        DropdownMenuItem(
                            text = { Text(a.label) },
                            onClick = {
                                onPick(a)
                                expanded = false
                            }
                        )
                    }
                }
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
