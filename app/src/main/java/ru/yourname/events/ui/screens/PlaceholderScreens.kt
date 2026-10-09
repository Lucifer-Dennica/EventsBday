package ru.yourname.events.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScaffold(title: String, text: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { p ->
        Box(Modifier.fillMaxSize().padding(p).padding(24.dp), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun NotificationsSettingsScreen(onBack: () -> Unit) =
    PlaceholderScaffold("Уведомления",
        "Здесь будут: режим «Все/Выборочно», частота, время напоминаний.",
        onBack)

@Composable
fun BehaviorSettingsScreen(onBack: () -> Unit) =
    PlaceholderScaffold("Поведение",
        "Здесь будут: свайпы, сортировка, вид списка (список/календарь).",
        onBack)

@Composable
fun DataSettingsScreen(onBack: () -> Unit) =
    PlaceholderScaffold("Данные",
        "Здесь будут: экспорт и импорт резервной копии.",
        onBack)

@Composable
fun AboutScreen(onBack: () -> Unit) =
    PlaceholderScaffold("О приложении",
        "Мои События\nВерсия 1.3.0\n\nНе является официальным продуктом.\n\nПриложение для напоминаний о днях рождения и событиях.",
        onBack)
