package com.example.recipapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Pełnoekranowy podgląd zdjęć.
 * - Pager pokazuje wszystkie zdjęcia przepisu, startuje od klikniętego
 * - Pinch-to-zoom na każdym zdjęciu
 * - Navbar jest ukryty (MainScreen nie renderuje go dla tej trasy)
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PhotoViewerScreen(
    initialUri: String,
    allUris: List<String>,
    onNavigateBack: () -> Unit
) {
    val startIndex = allUris.indexOf(initialUri).coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount   = { allUris.size }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Zajmij cały ekran włącznie z obszarem systemowym (status bar, nav bar)
            .windowInsetsPadding(WindowInsets(0))
    ) {
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ZoomableImage(uri = allUris[page])
        }

        // Przycisk powrotu
        IconButton(
            onClick  = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 8.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        // Licznik zdjęć
        if (allUris.size > 1) {
            Text(
                text     = "${pagerState.currentPage + 1} / ${allUris.size}",
                color    = Color.White,
                style    = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 16.dp)
            )
        }
    }
}

@Composable
private fun ZoomableImage(uri: String) {
    var scale   by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Resetuj zoom gdy zmienia się URI (użytkownik przeskoczył na inne zdjęcie)
    LaunchedEffect(uri) {
        scale   = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale <= 1f) {
                        scale   = 1f
                        offsetX = 0f
                        offsetY = 0f
                    } else {
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model              = uri,
            contentDescription = null,
            contentScale       = ContentScale.Fit,
            modifier           = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX       = scale,
                    scaleY       = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        )
    }
}