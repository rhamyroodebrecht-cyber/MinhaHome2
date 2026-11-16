package com.example.minhahome

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppRepository {

    suspend fun loadApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val leanback = query(pm, Intent.CATEGORY_LEANBACK_LAUNCHER)
        val mobile = query(pm, Intent.CATEGORY_LAUNCHER)

        (leanback + mobile)
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { ri ->
                val ai = ri.activityInfo
                val icon = ri.loadIcon(pm).toBitmap(128, 128).asImageBitmap()
                val banner = try {
                    ai.loadBanner(pm)?.toBitmap(320, 180)?.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
                AppInfo(
                    label = ri.loadLabel(pm).toString(),
                    packageName = ai.packageName,
                    activityName = ai.name,
                    icon = icon,
                    banner = banner
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun query(pm: PackageManager, category: String): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        return pm.queryIntentActivities(intent, 0)
    }
}

object Prefs {
    private fun sp(c: Context) =
        c.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)

    /** null = usuário nunca editou (usa os 6 primeiros apps) */
    fun getFavorites(c: Context): List<String>? {
        val raw = sp(c).getString("favorites", null) ?: return null
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun setFavorites(c: Context, list: List<String>) {
        sp(c).edit().putString("favorites", list.joinToString(",")).apply()
    }
}

object AppActions {

    fun open(context: Context, app: AppInfo) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName(app.packageName, app.activityName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            toast(context, "Não foi possível abrir ${app.label}")
        }
    }

    fun appInfo(context: Context, app: AppInfo) {
        try {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${app.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            openSettings(context)
        }
    }

    fun uninstall(context: Context, app: AppInfo) {
        try {
            val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            toast(context, "Não foi possível desinstalar ${app.label}")
        }
    }

    fun openSettings(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            toast(context, "Configurações indisponíveis")
        }
    }

    private fun toast(context: Context, msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
