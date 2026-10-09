package ru.yourname.events.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.yourname.events.data.settings.SettingsRepository
import ru.yourname.events.ui.screens.AboutScreen
import ru.yourname.events.ui.screens.AddEventScreen
import ru.yourname.events.ui.screens.BehaviorSettingsScreen
import ru.yourname.events.ui.screens.DataSettingsScreen
import ru.yourname.events.ui.screens.DesignSettingsScreen
import ru.yourname.events.ui.screens.EventDetailScreen
import ru.yourname.events.ui.screens.EventListScreen
import ru.yourname.events.ui.screens.NotificationsSettingsScreen
import ru.yourname.events.ui.screens.SettingsScreen
import ru.yourname.events.vm.EventViewModel

object Routes {
    const val LIST = "list"
    const val ADD = "add"
    const val EDIT = "edit/{id}"
    const val DETAIL = "detail/{id}"
    const val SETTINGS = "settings"
    const val SETTINGS_DESIGN = "settings/design"
    const val SETTINGS_NOTIF = "settings/notif"
    const val SETTINGS_BEHAVIOR = "settings/behavior"
    const val SETTINGS_DATA = "settings/data"
    const val SETTINGS_ABOUT = "settings/about"
    fun edit(id: Long) = "edit/$id"
    fun detail(id: Long) = "detail/$id"
}

@Composable
fun NavGraph(vm: EventViewModel) {
    val nav = rememberNavController()
    val events by vm.events.collectAsState()
    val ctx = LocalContext.current
    val swipes by SettingsRepository.swipesEnabledFlow(ctx).collectAsState(initial = true)

    NavHost(navController = nav, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            EventListScreen(
                events = events,
                swipesEnabled = swipes,
                onAddClick = { nav.navigate(Routes.ADD) },
                onEventClick = { nav.navigate(Routes.detail(it.id)) },
                onImportContacts = { vm.importFromContacts { } },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                onDeleteMany = { vm.deleteMany(it) }
            )
        }

        composable(Routes.ADD) {
            AddEventScreen(editing = null,
                onSave = { vm.addOrUpdate(it); nav.popBackStack() },
                onBack = { nav.popBackStack() })
        }

        composable(Routes.EDIT, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            val event = events.find { it.id == id }
            if (event == null) { LaunchedEffect(Unit) { nav.popBackStack() }; return@composable }
            AddEventScreen(editing = event,
                onSave = { vm.addOrUpdate(it); nav.popBackStack() },
                onBack = { nav.popBackStack() })
        }

        composable(Routes.DETAIL, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            val event = events.find { it.id == id }
            if (event == null) { LaunchedEffect(Unit) { nav.popBackStack() }; return@composable }
            EventDetailScreen(event = event,
                onEdit = { nav.navigate(Routes.edit(event.id)) },
                onDelete = { vm.delete(event); nav.popBackStack() },
                onBack = { nav.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { nav.popBackStack() },
                onOpenDesign = { nav.navigate(Routes.SETTINGS_DESIGN) },
                onOpenNotifications = { nav.navigate(Routes.SETTINGS_NOTIF) },
                onOpenBehavior = { nav.navigate(Routes.SETTINGS_BEHAVIOR) },
                onOpenData = { nav.navigate(Routes.SETTINGS_DATA) },
                onOpenAbout = { nav.navigate(Routes.SETTINGS_ABOUT) }
            )
        }

        composable(Routes.SETTINGS_DESIGN) { DesignSettingsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SETTINGS_NOTIF) { NotificationsSettingsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SETTINGS_BEHAVIOR) { BehaviorSettingsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SETTINGS_DATA) { DataSettingsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SETTINGS_ABOUT) { AboutScreen(onBack = { nav.popBackStack() }) }
    }
}
