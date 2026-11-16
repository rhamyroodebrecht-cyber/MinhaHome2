package com.example.minhahome

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val FocusColor = Color(0xFF00E5FF)

private class KeyState {
    var pressed = false
    var longHandled = false
}

/**
 * Cartão focável pelo D-pad: escala 1.08x + borda ciano quando focado.
 * Clique = soltar OK. Clique longo = segurar OK.
 */
@Composable
fun FocusCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    containerColor: Color = Color(0x1AFFFFFF),
    focusRequester: FocusRequester? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val keyState = remember { KeyState() }
    val scale by animateFloatAsState(if (focused) 1.08f else 1f, label = "focusScale")

    Box(
        modifier = modifier
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(containerColor)
            .border(3.dp, if (focused) FocusColor else Color.Transparent, shape)
            .onFocusChanged { focused = it.isFocused }
            .onKeyEvent { ev ->
                val isSelect = ev.key == Key.DirectionCenter ||
                    ev.key == Key.Enter ||
                    ev.key == Key.NumPadEnter
                if (!isSelect) return@onKeyEvent false
                when (ev.type) {
                    KeyEventType.KeyDown -> {
                        if (ev.nativeKeyEvent.repeatCount == 0) {
                            keyState.pressed = true
                            keyState.longHandled = false
                        } else if (keyState.pressed && !keyState.longHandled && onLongClick != null) {
                            keyState.longHandled = true
                            onLongClick()
                        }
                        true
                    }
                    KeyEventType.KeyUp -> {
                        if (keyState.pressed && !keyState.longHandled) onClick()
                        keyState.pressed = false
                        true
                    }
                    else -> false
                }
            }
            .focusable(),
        content = content
    )
}

@Composable
fun Header(onSettings: () -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("pt-BR")) }
    val hour = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }
    val date = Date(now)
    val dateText = dateFmt.format(date).replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FocusCard(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                onClick = onSettings
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configurações",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(22.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = greeting,
                color = Color(0xFFB8C4D9),
                fontSize = 16.sp
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = timeFmt.format(date),
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                text = dateText,
                color = Color(0xFFB8C4D9),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun FavoriteCard(
    app: AppInfo,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    FocusCard(
        modifier = Modifier
            .width(132.dp)
            .height(74.dp),
        containerColor = Color(0xFF16213A),
        focusRequester = focusRequester,
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        if (app.banner != null) {
            Image(
                bitmap = app.banner,
                contentDescription = app.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    bitmap = app.icon,
                    contentDescription = app.label,
                    modifier = Modifier.size(38.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = app.label,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun GridAppItem(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    FocusCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                modifier = Modifier.size(46.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = app.label,
                color = Color.White,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AppContextMenu(
    app: AppInfo,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onInfo: () -> Unit,
    onUninstall: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val firstItem = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(50)
        runCatching { firstItem.requestFocus() }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1B2433))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    bitmap = app.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = app.label,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))

            MenuButton("Abrir", firstItem) { onDismiss(); onOpen() }
            MenuButton("Informações do app (cache / forçar parada)") { onDismiss(); onInfo() }
            MenuButton("Desinstalar") { onDismiss(); onUninstall() }
            MenuButton(if (isFavorite) "Remover dos favoritos" else "Adicionar aos favoritos") {
                onDismiss(); onToggleFavorite()
            }
        }
    }
}

@Composable
private fun MenuButton(
    text: String,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit
) {
    FocusCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        containerColor = Color(0x22FFFFFF),
        focusRequester = focusRequester,
        onClick = onClick
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 14.dp)
        )
    }
}
