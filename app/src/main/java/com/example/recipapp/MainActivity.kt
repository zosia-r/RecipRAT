package com.example.recipapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat
// moje klasy
import com.example.recipapp.ui.screens.SplashScreen
import com.example.recipapp.ui.screens.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "splash") {
                // ekran startowy z filmem
                composable("splash") {
                    SplashScreen(onTimeout = {
                        navController.navigate("main") {
                            popUpTo("splash") { inclusive = true }
                        }
                    })
                }

                composable("main") {
                    MainScreen()
                }
            }
        }
    }
}