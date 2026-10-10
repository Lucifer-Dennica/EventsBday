package ru.yourname.events.nav

import android.app.Activity
import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import ru.yourname.events.ads.AdsConfig
import ru.yourname.events.ads.InterstitialManager
import ru.yourname.events.data.settings.AnimationMode
import ru.yourname.events.data.settings.CalendarMarkStyle
import ru.yourname.events.data.settings.SettingsRepository
import ru.yourname.events.data.settings.SortMode
import ru.yourname.events.data.settings.SwipeAction
import ru.yourname.events.data.settings.ViewMode
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
    val scope = rememberCoroutineScope()

    val swipes by SettingsRepository.swipesEnabledFlow(ctx).collectAsState(initial = true)
    val swipeToSettings by SettingsRepository.swipeToSettingsEnabledFlow(ctx).collectAsState(initial = true)
    val swipeLeft by SettingsRepository.swipeLeftActionFlow(ctx).collectAsState(initial = SwipeAction.DELETE)
    val swipeRight by SettingsRepository.swipeRightActionFlow(ctx).collectAsState(initial = SwipeAction.NOTHING)
    val sortMode by SettingsRepository.sortModeFlow(ctx).collectAsState(initial = SortMode.BY_DATE)
    val viewMode by SettingsRepository.viewModeFlow(ctx).collectAsState(initial = ViewMode.LIST)
    val calMark by SettingsRepository.calendarMarkStyleFlow(ctx).collectAsState(initial = CalendarMarkStyle.FILLED_SQUARE)

    val animEnabled by SettingsRepository.animEnabledFlow(ctx).collectAsState(initial = true)
    val animBatterySaver by SettingsRepository.animBatterySaverFlow(ctx).collectAsState(initial = false)
    val animRespectSystem by SettingsRepository.animRespectSystemFlow(ctx).collectAsState(initial = true)
    val animMode by SettingsRepository.animModeFlow(ctx).collectAsState(initial = AnimationMode.ALL_VISIBLE)

    // Финальное решение: разрешены ли анимации на этом устройстве прямо сейчас
    val animationsAllowed = remember(animEnabled, animBatterySaver, animRespectSystem, animMode) {
        if (!animEnabled || animMode == AnimationMode.OFF) {
            false
        } else {
            val inPowerSave = (ctx.getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode
            val systemAnimationsDisabled = if (animRespectSystem) {
                runCatching {
                    Settings.Global.getFloat(
                        ctx.contentResolver,
                        Settings.Global.ANIMATOR_DURATION_SCALE,
                        1f
                    ) == 0f
                }.getOrDefault(false)
            } else false

            when {
                systemAnimationsDisabled -> false
                inPowerSave && !animBatterySaver -> false
                else -> true
            }
        }
    }

    LaunchedEffect(Unit) {
        InterstitialManager.preload(ctx)
    }

    NavHost(navController = nav, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            EventListScreen(
                events = events,
                swipesEnabled = swipes,
                swipeLeftAction = swipeLeft,
                swipeRightAction = swipeRight,
                swipeToSettingsEnabled = swipeToSettings,
                sortMode = sortMode,
                viewMode = viewMode,
                calendarMarkStyle = calMark,
                animationsAllowed = animationsAllowed,
                onToggleViewMode = {
                    scope.launch {
                        SettingsRepository.setViewMode(
                            ctx,
                            if (viewMode == ViewMode.LIST) ViewMode.CALENDAR else ViewMode.LIST
                        )
                    }
                },
                onAddClick = { nav.navigate(Routes.ADD) },
                onEventClick = { event ->
                    scope.launch {
                        val count = SettingsRepository.incrementCardOpens(ctx)
                        if (count % AdsConfig.INTERSTITIAL_EVERY_N_OPENS == 0) {
                            val activity = ctx as? Activity
                            if (activity != null) {
                                InterstitialManager.showIfReady(activity) {
                                    InterstitialManager.preload(ctx)
                                }
                            }
                        }
                    }
                    nav.navigate(Routes.detail(event.id))
                },
                onImportContacts = { vm.importFromContacts { } },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                onDeleteMany = { vm.deleteMany(it) },
                onToggleNotifications = { vm.toggleNotifications(it) },
                onTogglePin = { vm.togglePin(it) },
                onEditEvent = { nav.navigate(Routes.edit(it.id)) }
            )
        }

        composable(Routes.ADD) {
            AddEventScreen(editing = null,
                onSave = {
                    vm.addOrUpdate(it)
                    scope.launch { SettingsRepository.incrementEventsCreated(ctx) }
                    nav.popBackStack()
                },
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
