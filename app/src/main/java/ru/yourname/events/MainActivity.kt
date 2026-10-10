package ru.yourname.events

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.yourname.events.data.settings.AppTheme
import ru.yourname.events.data.settings.SettingsRepository
import ru.yourname.events.nav.NavGraph
import ru.yourname.events.notif.NotificationHelper
import ru.yourname.events.notif.NotificationScheduler
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
            EventsTheme(theme = theme) {
                NavGraph(vm)
            }
        }
    }
}
