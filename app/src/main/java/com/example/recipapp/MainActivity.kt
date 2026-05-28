package com.example.recipapp

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recipapp.ui.screens.MainScreen
import com.example.recipapp.ui.screens.SplashScreen
import com.example.recipapp.ui.theme.RecipAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* przyznano lub odmówiono – obsługujemy po cichu */ }

    // SharedFlow przekazujący recipeId do MainScreen gdy app jest już otwarta
    companion object {
        private val _pendingRecipeId = MutableStateFlow<Long?>(null)
        val pendingRecipeId: StateFlow<Long?> = _pendingRecipeId.asStateFlow()
        fun clearPendingRecipeId() {
            _pendingRecipeId.value = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // ── UKRYWANIE TYLKO DOLNEGO PASKA DLA CAŁEJ APLIKACJI ──────────────
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Ukrywa wyłącznie pasek nawigacyjny (navigationBars)
        insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
        // Pozwala na tymczasowe pokazanie paska gestem od dolnej krawędzi
        insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // ──────────────────────────────────────────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Obsłuż deep link przy cold starcie / gdy app była zabita
        handleTimerIntent(intent)

        setContent {
            RecipAppTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(onTimeout = {
                            navController.navigate("main") {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }
                    composable("main") {
                        MainScreen(pendingRecipeId = pendingRecipeId)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Obsłuż deep link gdy app jest już otwarta na ekranie
        handleTimerIntent(intent)
    }

    private fun handleTimerIntent(intent: Intent?) {
        val recipeId = intent?.getLongExtra("LAUNCH_RECIPE_ID", -1L) ?: -1L
        if (recipeId != -1L) {
            _pendingRecipeId.value = recipeId
        }
    }
}