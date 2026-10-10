package ru.yourname.events.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.yourname.events.data.settings.SettingsRepository
import ru.yourname.events.data.settings.SwipeAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeGesturesScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val leftAction by SettingsRepository.swipeLeftActionFlow(ctx).collectAsState(initial = SwipeAction.DELETE)
    val rightAction by SettingsRepository.swipeRightActionFlow(ctx).collectAsState(initial = SwipeAction.NOTHING)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Действия свайпов") },
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

            Text(
                "Выберите, что происходит при свайпе карточки. " +
                        "Если действие назначено на жест, соответствующая иконка исчезает с карточки.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(Modifier.height(8.dp))

            ActionPicker(
                title = "Свайп влево",
                subtitle = "Справа налево",
                current = leftAction,
                exclude = rightAction,
                onPick = { scope.launch { SettingsRepository.setSwipeLeftAction(ctx, it) } }
            )

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            ActionPicker(
                title = "Свайп вправо",
                subtitle = "Слева направо",
                current = rightAction,
                exclude = leftAction,
                onPick = { scope.launch { SettingsRepository.setSwipeRightAction(ctx, it) } }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionPicker(
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
        Spacer(Modifier.height(8.dp))

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
