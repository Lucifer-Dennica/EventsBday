package ru.yourname.events

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.yourname.events.data.achievements.Achievements
import ru.yourname.events.data.settings.AppTheme
import ru.yourname.events.data.settings.SettingsRepository
import ru.yourname.events.nav.NavGraph
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

        setContent {
            val theme by SettingsRepository.themeFlow(this)
                .collectAsState(initial = AppTheme.SYSTEM)

            var pendingAchievement by remember { mutableStateOf<String?>(null) }

            // При старте проверяем, есть ли новое достижение
            LaunchedEffect(Unit) {
                val key = Achievements.checkAndGetNewlyUnlocked(this@MainActivity)
                if (key != null) pendingAchievement = key
            }

            EventsTheme(theme = theme) {
                NavGraph(vm)

                // Показ всплывашки при разблокировке
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
