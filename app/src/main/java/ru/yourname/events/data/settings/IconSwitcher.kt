package ru.yourname.events.data.settings

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object IconSwitcher {

    /**
     * Переключает активный alias иконки.
     * @param ctx любой контекст
     * @param target желаемая иконка
     */
    fun switchTo(ctx: Context, target: AppIcon) {
        val pm = ctx.packageManager

        AppIcon.allAliases().forEach { alias ->
            val component = ComponentName(ctx.packageName, alias)
            val newState = if (alias == target.alias) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            pm.setComponentEnabledSetting(
                component,
                newState,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
