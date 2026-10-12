package ru.yourname.events

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.yourname.events.data.achievements.Achievements
import ru.yourname.events.data.settings.AppTheme
import ru.yourname.events.data.settings.SettingsRepository
import ru.yourname.events.nav.NavGraph
import ru.yourname.events.nav.Routes
import ru.yourname.events.notif.NotificationHelper
import ru.yourname.events.notif.NotificationScheduler
import ru.yourname.events.ui.components.AchievementDialog
import ru.yourname.events.ui.theme.EventsTheme
import ru.yourname.events.vm.EventViewModel

class MainActivity : ComponentActivity() {
    private val vm: EventViewModel by viewModels()

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        NotificationHelper.ensureChannel(this)

        lifecycleScope.launch {
            SettingsRepository.ensureFirstLaunch(this@MainActivity)
            NotificationScheduler.schedule(this@MainActivity)
        }

        // Определяем стартовый маршрут по intent deep-link
        val startRoute = when (intent?.data?.host) {
            "calendar" -> Routes.LIST
            else -> Routes.LIST
        }

        setContent {
            val theme by SettingsRepository.themeFlow(this)
                .collectAsState(initial = AppTheme.SYSTEM)

            var pendingAchievement by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                val key = Achievements.checkAndGetNewlyUnlocked(this@MainActivity)
                if (key != null) pendingAchievement = key
            }

            EventsTheme(theme = theme) {
                NavGraph(vm, startRoute = startRoute)

                pendingAchievement?.let { key ->
                    val (avatarRes, title, desc) = when (key) {
                        Achievements.ASTRONAUT_KEY -> Triple(
                            R.drawable.ic_avatar_astronaut,
                            "Космонавт",
                            "Вы с нами уже целый год! Спасибо, что пользуетесь приложением."
                        )
                        Achievements.CAT_KEY -> Triple(
                            R.drawable.ic_avatar_cat,
                            "Кот-программист",
                            "Вы создали 50 карточек событий. Вот вам кот!"
                        )
                        else -> Triple(0, "", "")
                    }
                    if (avatarRes != 0) {
                        AchievementDialog(
                            avatarRes = avatarRes,
                            title = title,
                            description = desc,
                            onDismiss = {
                                lifecycleScope.launch {
                                    SettingsRepository.markAchievementShown(this@MainActivity, key)
                                }
                                pendingAchievement = null
                            }
                        )
                    } else {
                        pendingAchievement = null
                    }
                }
            }
        }
    }
}
