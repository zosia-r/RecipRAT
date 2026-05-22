package com.example.recipapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.recipapp.ui.theme.MintCream

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PhotoViewerScreen(
    initialIndex: Int,
    allUris: List<String>,
    onNavigateBack: () -> Unit
) {
    if (allUris.isEmpty()) {
        onNavigateBack()
        return
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, allUris.lastIndex),
        pageCount   = { allUris.size }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets(0))
    ) {
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ZoomableImage(uri = allUris[page])
        }

        // ── Przycisk wstecz ───────────────────────────────────────────────
        IconButton(
            onClick  = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MintCream)
        }

        // ── Licznik stron ─────────────────────────────────────────────────
        if (allUris.size > 1) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 12.dp),
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.45f)
            ) {
                Text(
                    text     = "${pagerState.currentPage + 1} / ${allUris.size}",
                    color    = MintCream,
                    style    = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ZoomableImage(uri: String) {
    var scale   by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(uri) {
        scale   = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    // Czekaj na pierwsze dotknięcie
                    val down = awaitFirstDown(requireUnconsumed = false)

                    var zoom        = 1f
                    var panX        = 0f
                    var panY        = 0f
                    var pastSlop    = false

                    do {
                        val event = awaitPointerEvent()
                        val zoomChange = event.calculateZoom()
                        val panChange  = event.calculatePan()

                        zoom = zoomChange
                        panX = panChange.x
                        panY = panChange.y

                        val newScale = (scale * zoomChange).coerceIn(1f, 5f)

                        // Jeśli zoom > 1 już jest aktywny, konsumuj gesty
                        if (scale > 1f || zoomChange != 1f) {
                            scale = newScale
                            if (scale <= 1f) {
                                scale = 1f; offsetX = 0f; offsetY = 0f
                            } else {
                                offsetX += panChange.x
                                offsetY += panChange.y
                            }
                            // Konsumuj tylko gdy zoomujemy — pager nie dostanie tych zdarzeń
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                        // Gdy scale == 1f i ruch jest poziomy — NIE konsumujemy,
                        // żeby HorizontalPager mógł obsłużyć swipe
                    } while (event.changes.any { it.pressed })
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