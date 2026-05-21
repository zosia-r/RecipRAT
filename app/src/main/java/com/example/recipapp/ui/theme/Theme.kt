package com.example.recipapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ──────────────────────────────────────────────────────────────────
//  JASNY schemat kolorów
// ──────────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(

    // Główne akcenty
    primary          = DeepTeal,           // przyciski, FAB, aktywne elementy
    onPrimary        = MintCream,          // tekst/ikona na primary
    primaryContainer = DeepTealLight,      // tło chipa, tła pomocnicze
    onPrimaryContainer = DeepTealDark,     // tekst na primaryContainer

    secondary        = DustyRose,          // drugorzędne akcenty, tagi
    onSecondary      = CoffeeBean,
    secondaryContainer = DustyRoseLight,
    onSecondaryContainer = CoffeeBean,

    tertiary         = CherryRose,         // ulubione, wyróżnienia, błędy-akcenty
    onTertiary       = MintCream,
    tertiaryContainer = CherryRoseLight,
    onTertiaryContainer = CoffeeBean,

    // Tła i powierzchnie
    background       = MintCream,
    onBackground     = CoffeeBean,

    surface          = NeutralSurface,     // karty, arkusze dolne
    onSurface        = CoffeeBean,
    surfaceVariant   = DustyRoseLight,     // alternatywne tło kart
    onSurfaceVariant = CoffeeBeanSoft,

    // Kontury
    outline          = NeutralOutline,
    outlineVariant   = DustyRoseLight,

    // Błędy
    error            = CherryRose,
    onError          = MintCream,
    errorContainer   = CherryRoseLight,
    onErrorContainer = CoffeeBean,

    // System
    inverseSurface   = CoffeeBean,
    inverseOnSurface = MintCream,
    inversePrimary   = DeepTealLight,

    scrim            = CoffeeBean
)

// ──────────────────────────────────────────────────────────────────
//  CIEMNY schemat kolorów
// ──────────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(

    primary          = DeepTealLight,
    onPrimary        = DeepTealDark,
    primaryContainer = DeepTealDark,
    onPrimaryContainer = DeepTealLight,

    secondary        = DustyRoseLight,
    onSecondary      = CoffeeBean,
    secondaryContainer = Color(0xFF5C3535),  // ciemny rose
    onSecondaryContainer = DustyRoseLight,

    tertiary         = CherryRoseLight,
    onTertiary       = CoffeeBean,
    tertiaryContainer = Color(0xFF6B0F2A),
    onTertiaryContainer = CherryRoseLight,

    background       = CoffeeBean,
    onBackground     = MintCream,

    surface          = Color(0xFF2E1315),    // CoffeeBean lekko rozjaśniony
    onSurface        = MintCream,
    surfaceVariant   = Color(0xFF4A2C2E),
    onSurfaceVariant = DustyRoseLight,

    outline          = CoffeeBeanSoft,
    outlineVariant   = Color(0xFF4A2C2E),

    error            = CherryRoseLight,
    onError          = CoffeeBean,
    errorContainer   = Color(0xFF6B0F2A),
    onErrorContainer = CherryRoseLight,

    inverseSurface   = MintCream,
    inverseOnSurface = CoffeeBean,
    inversePrimary   = DeepTeal,

    scrim            = CoffeeBean
)

// ──────────────────────────────────────────────────────────────────
//  Główny composable tematu
// ──────────────────────────────────────────────────────────────────
@Composable
fun RecipAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color dostępny od Android 12 — wyłącz jeśli chcesz
    // konsekwentnie używać własnej palety na wszystkich urządzeniach
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    // Kolor paska statusu dopasowany do tła
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = RecipAppTypography,
        content     = content
    )
}