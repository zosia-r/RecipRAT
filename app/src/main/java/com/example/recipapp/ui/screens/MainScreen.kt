package com.example.recipapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.recipapp.Recipapp
import com.example.recipapp.ui.theme.CherryRose
import com.example.recipapp.ui.theme.CherryRoseLight
import com.example.recipapp.ui.theme.DeepTeal
import com.example.recipapp.ui.theme.DeepTealLight
import com.example.recipapp.ui.theme.MintCream
import com.example.recipapp.viewmodel.RecipeViewModel
import com.example.recipapp.viewmodel.RecipeViewModelFactory

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Favourites : Screen("favourites", "Favourites", Icons.Filled.Favorite)
    object Search     : Screen("search",     "Search",     Icons.Filled.Search)
    object New        : Screen("new",        "New",        Icons.Filled.Add)
    object Detail : Screen("detail/{recipeId}", "Detail", Icons.Filled.Favorite) {
        fun createRoute(id: Long) = "detail/$id"
        const val routeWithArgs = "detail/{recipeId}"
    }
    object Edit : Screen("edit/{recipeId}", "Edit", Icons.Filled.Edit) {
        fun createRoute(id: Long) = "edit/$id"
        const val routeWithArgs = "edit/{recipeId}"
    }
    object PhotoViewer : Screen("photo", "Photo", Icons.Filled.Favorite)
}

@Composable
fun MainScreen() {
    val app = androidx.compose.ui.platform.LocalContext.current
        .applicationContext as Recipapp

    val recipeViewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModelFactory(app, app.repository)
    )
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    val showNavBar = currentRoute != Screen.PhotoViewer.route

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }

    Scaffold(
        bottomBar = {
            if (showNavBar) {
                val currentDestination = navBackStackEntry?.destination

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // ── Ulubione ──────────────────────────────────────────────
                    val favSelected = currentDestination?.hierarchy
                        ?.any { it.route == Screen.Favourites.route } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                Screen.Favourites.icon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label    = { Text(Screen.Favourites.label, style = MaterialTheme.typography.labelSmall) },
                        selected = favSelected,
                        onClick  = { navigateToTab(Screen.Favourites.route) },
                        colors   = NavigationBarItemDefaults.colors(
                            selectedIconColor   = CherryRose,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor   = CherryRose,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor      = CherryRoseLight
                        )
                    )

                    // ── Nowy przepis — wyróżniony FAB ─────────────────────────
                    val newSelected = currentDestination?.hierarchy
                        ?.any { it.route == Screen.New.route } == true

                    NavigationBarItem(
                        icon = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(52.dp)
                                    .shadow(8.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(DeepTeal)
                            ) {
                                Icon(
                                    Screen.New.icon,
                                    contentDescription = null,
                                    tint     = MintCream,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        },
                        label    = {
                            Text(
                                Screen.New.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (newSelected) DeepTeal
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selected = newSelected,
                        onClick  = { navigateToTab(Screen.New.route) },
                        colors   = NavigationBarItemDefaults.colors(
                            selectedIconColor   = androidx.compose.ui.graphics.Color.Transparent,
                            unselectedIconColor = androidx.compose.ui.graphics.Color.Transparent,
                            indicatorColor      = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )

                    // ── Szukaj ───────────────────────────────────────────────
                    val searchSelected = currentDestination?.hierarchy
                        ?.any { it.route == Screen.Search.route } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                Screen.Search.icon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label    = { Text(Screen.Search.label, style = MaterialTheme.typography.labelSmall) },
                        selected = searchSelected,
                        onClick  = { navigateToTab(Screen.Search.route) },
                        colors   = NavigationBarItemDefaults.colors(
                            selectedIconColor   = DeepTeal,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor   = DeepTeal,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor      = DeepTealLight
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Favourites.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Favourites.route) {
                FavouritesScreen(
                    viewModel     = recipeViewModel,
                    onRecipeClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
                )
            }
            composable(Screen.New.route) {
                NewRecipeScreen(
                    viewModel      = recipeViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel     = recipeViewModel,
                    onRecipeClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
                )
            }
            composable(
                route     = Screen.Detail.routeWithArgs,
                arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("recipeId") ?: return@composable
                RecipeDetailScreen(
                    recipeId         = id,
                    viewModel        = recipeViewModel,
                    onNavigateBack   = { navController.popBackStack() },
                    onNavigateToEdit = { recipeId -> navController.navigate(Screen.Edit.createRoute(recipeId)) },
                    onPhotoClick     = { initialUri, allUris ->
                        val index = allUris.indexOf(initialUri).coerceAtLeast(0)
                        recipeViewModel.setPhotoViewerData(allUris, index)
                        navController.navigate(Screen.PhotoViewer.route)
                    }
                )
            }
            composable(
                route     = Screen.Edit.routeWithArgs,
                arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("recipeId") ?: return@composable
                EditRecipeScreen(
                    recipeId       = id,
                    viewModel      = recipeViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PhotoViewer.route) {
                val allUris by recipeViewModel.pendingPhotoUris.collectAsState()
                val index   by recipeViewModel.pendingPhotoIndex.collectAsState()
                PhotoViewerScreen(
                    initialIndex   = index,
                    allUris        = allUris,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}