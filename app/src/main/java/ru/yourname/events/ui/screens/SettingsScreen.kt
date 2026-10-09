package ru.yourname.events.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentTheme by SettingsRepository.themeFlow(ctx).collectAsState(initial = AppTheme.SYSTEM)
    val swipesEnabled by SettingsRepository.swipesEnabledFlow(ctx).collectAsState(initial = true)

    fun open(url: String) {
        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { p ->
        Column(
            Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState())
        ) {
            SettingsSection("Оформление")
            Text(
                "Тема",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            AppTheme.entries.forEach { t ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = currentTheme == t,
                            onClick = { scope.launch { SettingsRepository.setTheme(ctx, t) } }
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentTheme == t,
                        onClick = { scope.launch { SettingsRepository.setTheme(ctx, t) } }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(t.label)
                }
            }

            SettingsSection("Поведение")
            ListItem(
                headlineContent = { Text("Свайпы в списке") },
                supportingContent = { Text("Свайп влево — удалить, долгое нажатие — выделить") },
                trailingContent = {
                    Switch(
                        checked = swipesEnabled,
                        onCheckedChange = { scope.launch { SettingsRepository.setSwipesEnabled(ctx, it) } }
                    )
                }
            )

            SettingsSection("Разработчик")
            SettingItem("✉️  Email", "denis22142qwe@gmail.com") {
                open("mailto:denis22142qwe@gmail.com")
            }
            SettingItem("✈️  Telegram", "@Lucifer_Denicca_22142") {
                open("https://t.me/Lucifer_Denicca_22142")
            }
            SettingItem("💝  Поддержать проект", "DonationAlerts") {
                open("https://www.donationalerts.com/r/lucifer_dennica_1999")
            }

            SettingsSection("О приложении")
            SettingItem("Версия", "1.2.0", onClick = null)
            SettingItem("Политика конфиденциальности", "", onClick = null)

            SettingsSection("Реклама")
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(64.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Здесь будет реклама (скоро)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingItem(title: String, subtitle: String, onClick: (() -> Unit)?) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = if (subtitle.isBlank()) null else { { Text(subtitle) } },
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    )
}
