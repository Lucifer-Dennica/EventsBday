package ru.yourname.events

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import ru.yourname.events.nav.NavGraph
import ru.yourname.events.ui.theme.EventsTheme
import ru.yourname.events.vm.EventViewModel

class MainActivity : ComponentActivity() {
    private val vm: EventViewModel by viewModels()

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* результат не важен, просто запрос */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Запрос POST_NOTIFICATIONS на Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            EventsTheme {
                NavGraph(vm)
            }
        }
    }
}
