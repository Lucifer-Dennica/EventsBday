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
import ru.yourname.events.ui.screens.AddEventScreen
import ru.yourname.events.ui.screens.EventDetailScreen
import ru.yourname.events.ui.screens.EventListScreen
import ru.yourname.events.ui.screens.SettingsScreen
import ru.yourname.events.vm.EventViewModel

object Routes {
    const val LIST = "list"
    const val ADD = "add"
    const val EDIT = "edit/{id}"
    const val DETAIL = "detail/{id}"
    const val SETTINGS = "settings"
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
                onDeleteMany = { list -> vm.deleteMany(list) }
            )
        }

        composable(Routes.ADD) {
            AddEventScreen(
                editing = null,
                onSave = { vm.addOrUpdate(it); nav.popBackStack() },
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.EDIT, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            val event = events.find { it.id == id }
            if (event == null) {
                LaunchedEffect(Unit) { nav.popBackStack() }
                return@composable
            }
            AddEventScreen(
                editing = event,
                onSave = { vm.addOrUpdate(it); nav.popBackStack() },
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.DETAIL, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            val event = events.find { it.id == id }
            if (event == null) {
                LaunchedEffect(Unit) { nav.popBackStack() }
                return@composable
            }
            EventDetailScreen(
                event = event,
                onEdit = { nav.navigate(Routes.edit(event.id)) },
                onDelete = { vm.delete(event); nav.popBackStack() },
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
    }
}
