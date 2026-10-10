package ru.yourname.events.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.yourname.events.data.settings.AppTheme
import ru.yourname.events.data.settings.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val current by SettingsRepository.themeFlow(ctx).collectAsState(initial = AppTheme.SYSTEM)

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
            Text(
                "Тема",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            AppTheme.entries.forEach { t ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = current == t,
                            onClick = { scope.launch { SettingsRepository.setTheme(ctx, t) } }
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = current == t,
                        onClick = { scope.launch { SettingsRepository.setTheme(ctx, t) } }
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(t.label, style = MaterialTheme.typography.bodyLarge)
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "Скоро: цвет акцента, иконка приложения, шрифт",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
