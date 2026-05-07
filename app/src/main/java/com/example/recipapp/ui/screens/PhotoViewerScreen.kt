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
 * Pełnoekranowy podgląd zdjęć z możliwością:
 * - przewijania między zdjęciami (HorizontalPager)
 * - powiększania/pomniejszania gestem szczypania (pinch-to-zoom)
 *
 * @param initialUri  ścieżka do zdjęcia które kliknięto – od niego zaczyna pager
 * @param allUris     lista wszystkich zdjęć przepisu – do przewijania między nimi
 *                    (jeśli pusta, wyświetla tylko initialUri)
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PhotoViewerScreen(
    initialUri: String,
    allUris: List<String> = listOf(initialUri),
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
    ) {
        // ── Pager ze zdjęciami ───────────────────────────────────────────────
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ZoomableImage(uri = allUris[page])
        }

        // ── Przycisk wstecz ──────────────────────────────────────────────────
        IconButton(
            onClick  = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 8.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint               = Color.White
            )
        }

        // ── Licznik zdjęć (np. "2 / 5") ─────────────────────────────────────
        if (allUris.size > 1) {
            Text(
                text     = "${pagerState.currentPage + 1} / ${allUris.size}",
                color    = Color.White,
                style    = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 16.dp)
            )
        }
    }
}

/** Pojedyncze zdjęcie z obsługą pinch-to-zoom i przesuwania */
@Composable
private fun ZoomableImage(uri: String) {
    var scale       by remember { mutableFloatStateOf(1f) }
    var offsetX     by remember { mutableFloatStateOf(0f) }
    var offsetY     by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    // gdy nie powiększone – zresetuj pozycję
                    if (scale == 1f) {
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
                    scaleX         = scale,
                    scaleY         = scale,
                    translationX   = offsetX,
                    translationY   = offsetY
                )
        )
    }
}