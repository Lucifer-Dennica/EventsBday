package ru.yourname.events.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

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
            SettingItem("Версия", "1.1.0", onClick = null)
            SettingItem("Политика конфиденциальности", "", onClick = null)

            SettingsSection("Реклама")
            // Место под рекламный баннер Yandex Mobile Ads.
            // Позже сюда вставим AdView. Пока — заглушка.
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(64.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
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
        modifier = if (onClick != null)
            Modifier.clickableItem(onClick) else Modifier
    )
}

@Composable
private fun Modifier.clickableItem(block: () -> Unit): Modifier =
    this.then(Modifier).let { it }
