package ru.yourname.events.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenDesign: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenBehavior: () -> Unit,
    onOpenData: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val ctx = LocalContext.current
    fun open(url: String) {
        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
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

            NavItem("🎨", "Дизайн", "Тема, цвет акцента, иконка приложения", onOpenDesign)
            NavItem("🔔", "Уведомления", "Частота, время, выбор событий", onOpenNotifications)
            NavItem("👆", "Поведение", "Свайпы, сортировка, вид списка", onOpenBehavior)
            NavItem("💾", "Данные", "Экспорт и импорт, резервная копия", onOpenData)

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            NavItem("💝", "Поддержать разработчика", "DonationAlerts") {
                open("https://www.donationalerts.com/r/lucifer_dennica_1999")
            }
            NavItem("✉️", "Написать разработчику", "denis22142qwe@gmail.com") {
                open("mailto:denis22142qwe@gmail.com")
            }
            NavItem("✈️", "Telegram", "@Lucifer_Denicca_22142") {
                open("https://t.me/Lucifer_Denicca_22142")
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            NavItem("ℹ️", "О приложении", "Версия 1.3.0", onOpenAbout)

            // Реклама — Yandex Mobile Ads. Позже вставим AdView сюда.
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
private fun NavItem(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
