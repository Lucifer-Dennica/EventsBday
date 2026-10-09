package ru.yourname.events

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import ru.yourname.events.nav.NavGraph
import ru.yourname.events.ui.theme.EventsTheme
import ru.yourname.events.vm.EventViewModel

class MainActivity : ComponentActivity() {
    private val vm: EventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventsTheme {
                NavGraph(vm)
            }
        }
    }
}
