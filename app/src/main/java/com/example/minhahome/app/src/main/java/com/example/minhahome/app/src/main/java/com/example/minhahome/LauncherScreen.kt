package com.example.minhahome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun LauncherScreen(
    apps: List<AppInfo>,
    favoritePackages: List<String>?,
    onToggleFavorite: (String) -> Unit,
    backgroundImage: ImageBitmap? = null
) {
    val context = LocalContext.current
    var menuApp by remember { mutableStateOf<AppInfo?>(null) }

    val favorites = remember(apps, favoritePackages) {
        if (favoritePackages == null) {
            apps.take(6)
        } else {
            favoritePackages.mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } }
        }
    }

    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(favorites.isNotEmpty()) {
        if (favorites.isNotEmpty()) {
            delay(150)
            runCatching { firstFocus.requestFocus() }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fundo: gradiente padrão ou imagem escolhida (gancho para a galeria no futuro)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0B1426), Color(0xFF000000))
                    )
                )
        )
        if (backgroundImage != null) {
            Image(
                bitmap = backgroundImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xB3000000))
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Header(onSettings = { AppActions.openSettings(context) })

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionTitle("Favoritos")
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    if (favorites.isEmpty()) {
                        Text(
                            text = "Segure OK em um app para adicioná-lo aos favoritos.",
                            color = Color(0xFFB8C4D9),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(favorites, key = { it.packageName }) { app ->
                                FavoriteCard(
                                    app = app,
                                    focusRequester = if (app == favorites.first()) firstFocus else null,
                                    onClick = { AppActions.open(context, app) },
                                    onLongClick = { menuApp = app }
                                )
                            }
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionTitle("Todos os aplicativos")
                }

                items(apps, key = { it.packageName }) { app ->
                    GridAppItem(
                        app = app,
                        onClick = { AppActions.open(context, app) },
                        onLongClick = { menuApp = app }
                    )
                }
            }
        }
    }

    menuApp?.let { app ->
        AppContextMenu(
            app = app,
            isFavorite = favorites.any { it.packageName == app.packageName },
            onDismiss = { menuApp = null },
            onOpen = { AppActions.open(context, app) },
            onInfo = { AppActions.appInfo(context, app) },
            onUninstall = { AppActions.uninstall(context, app) },
            onToggleFavorite = { onToggleFavorite(app.packageName) }
        )
    }
}
