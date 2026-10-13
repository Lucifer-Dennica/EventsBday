package ru.yourname.events.data.settings

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

object IconSwitcher {

    private const val TAG = "IconSwitcher"

    
    fun switchTo(ctx: Context, target: AppIcon) {
        val pm = ctx.packageManager
        val packageName = ctx.packageName

        AppIcon.allAliases().forEach { alias ->
            val component = ComponentName(packageName, alias)
            val newState = if (alias == target.alias) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            try {
                pm.setComponentEnabledSetting(
                    component,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "OK: $alias -> $newState")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to switch $alias: ${e.message}")                
            }
        }
    }
}
