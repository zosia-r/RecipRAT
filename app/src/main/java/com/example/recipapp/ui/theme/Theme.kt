package com.example.recipapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ──────────────────────────────────────────────────────────────────
//  JASNY schemat kolorów
// ──────────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary          = DeepTeal,
    onPrimary        = MintCream,
    primaryContainer = DeepTealLight,
    onPrimaryContainer = DeepTealDark,

    secondary        = DustyRose,
    onSecondary      = CoffeeBean,
    secondaryContainer = DustyRoseLight,
    onSecondaryContainer = CoffeeBean,

    tertiary         = CherryRose,
    onTertiary       = MintCream,
    tertiaryContainer = CherryRoseLight,
    onTertiaryContainer = CoffeeBean,

    background       = MintCream,
    onBackground     = CoffeeBean,

    surface          = NeutralSurface,
    onSurface        = CoffeeBean,
    surfaceVariant   = DustyRoseLight,
    onSurfaceVariant = CoffeeBeanSoft,

    outline          = NeutralOutline,
    outlineVariant   = DustyRoseLight,

    error            = CherryRose,
    onError          = MintCream,
    errorContainer   = CherryRoseLight,
    onErrorContainer = CoffeeBean,

    inverseSurface   = CoffeeBean,
    inverseOnSurface = MintCream,
    inversePrimary   = DeepTealLight,

    scrim            = CoffeeBean
)

// ──────────────────────────────────────────────────────────────────
//  Główny composable tematu
// ──────────────────────────────────────────────────────────────────
@Composable
fun RecipAppTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicLightColorScheme(LocalContext.current)
        }
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = RecipAppTypography,
        content     = content
    )
}