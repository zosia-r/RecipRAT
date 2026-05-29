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
    ) {}

    companion object {

        // Pending recipe ID for timer notification
        private val _pendingRecipeId = MutableStateFlow<Long?>(null)
        val pendingRecipeId: StateFlow<Long?> = _pendingRecipeId.asStateFlow()
        fun clearPendingRecipeId() {
            _pendingRecipeId.value = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Immersive mode
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        handleTimerIntent(intent)

        setContent {
            RecipAppTheme {
                val navController = rememberNavController()

                // First screen - splash
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(onTimeout = {
                            navController.navigate("main") {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }
                    // on timeout, navigate to main screen
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
        handleTimerIntent(intent)
    }

    // Handle pending recipe ID from timer notification
    private fun handleTimerIntent(intent: Intent?) {
        val recipeId = intent?.getLongExtra("LAUNCH_RECIPE_ID", -1L) ?: -1L
        if (recipeId != -1L) {
            _pendingRecipeId.value = recipeId
        }
    }
}