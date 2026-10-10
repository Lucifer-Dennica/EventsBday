package ru.yourname.events.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.yourname.events.data.settings.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorSettingsScreen(
    onBack: () -> Unit,
    onOpenSwipeSettings: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val swipesEnabled by SettingsRepository.swipesEnabledFlow(ctx).collectAsState(initial = true)

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

            ListItem(
                headlineContent = { Text("Свайпы в списке",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Движение пальцем по карточке для действий") },
                trailingContent = {
                    Switch(
                        checked = swipesEnabled,
                        onCheckedChange = { scope.launch { SettingsRepository.setSwipesEnabled(ctx, it) } }
                    )
                }
            )

            if (swipesEnabled) {
                ListItem(
                    headlineContent = { Text("Настроить действия жестов",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Что происходит при свайпе влево и вправо") },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier.clickable { onOpenSwipeSettings() }
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            Text(
                "Совет: свайпните вправо по пустому месту на главном экране, чтобы быстро открыть настройки.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
