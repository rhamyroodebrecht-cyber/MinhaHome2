package com.example.minhahome

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var apps by mutableStateOf<List<AppInfo>>(emptyList())
    private var favorites by mutableStateOf<List<String>?>(null)

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshApps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        favorites = Prefs.getFavorites(this)

        setContent {
            // Normaliza a densidade: o layout sempre se comporta como uma tela de 960dp
            // de largura, seja a TV 1080p com 320dpi (960dp) ou 213dpi (1280dp).
            val config = LocalConfiguration.current
            val density = LocalDensity.current
            val scale = config.screenWidthDp / 960f
            val normalized = Density(
                density = density.density * scale,
                fontScale = density.fontScale
            )

            CompositionLocalProvider(LocalDensity provides normalized) {
                // Impede que o botão Voltar feche a tela inicial
                BackHandler(enabled = true) { }

                LauncherScreen(
                    apps = apps,
                    favoritePackages = favorites,
                    onToggleFavorite = { pkg -> toggleFavorite(pkg) }
                )
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(
            this, packageReceiver, filter, ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onResume() {
        super.onResume()
        refreshApps()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(packageReceiver)
    }

    private fun refreshApps() {
        lifecycleScope.launch {
            apps = AppRepository.loadApps(this@MainActivity)
        }
    }

    private fun toggleFavorite(pkg: String) {
        val current = favorites ?: apps.take(6).map { it.packageName }
        val updated = if (pkg in current) current - pkg else current + pkg
        favorites = updated
        Prefs.setFavorites(this, updated)
    }
}
