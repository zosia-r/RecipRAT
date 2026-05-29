package com.example.recipapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipapp.MainActivity
import com.example.recipapp.Recipapp
import com.example.recipapp.ui.components.AddRecipeBottomSheet
import com.example.recipapp.ui.components.MainNavigationBar
import com.example.recipapp.viewmodel.RecipeViewModel
import com.example.recipapp.viewmodel.RecipeViewModelFactory
import kotlinx.coroutines.flow.StateFlow


/**
 * Navigation graph for the main screen.
 */

// Screen routes: simple + with arguments
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Favourites : Screen("favourites", "Favourites", Icons.Filled.Favorite)
    object Search     : Screen("search",     "Search",     Icons.Filled.Search)
    object New        : Screen("new",        "New",        Icons.Filled.Add)
    object Import     : Screen("import",     "Import",     Icons.Filled.Add)
    object Detail : Screen("detail/{recipeId}", "Detail", Icons.Filled.Favorite) {
        fun createRoute(id: Long) = "detail/$id"
        const val ROUTE_WITH_ARGS = "detail/{recipeId}"
    }
    object Edit : Screen("edit/{recipeId}", "Edit", Icons.Filled.Edit) {
        fun createRoute(id: Long) = "edit/$id"
        const val ROUTE_WITH_ARGS = "edit/{recipeId}"
    }
    object PhotoViewer : Screen("photo", "Photo", Icons.Filled.Favorite)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    pendingRecipeId: StateFlow<Long?>
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as Recipapp

    // One view model for all screens
    val recipeViewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModelFactory(app, app.repository)
    )

    // Navigation
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    val showNavBar = currentRoute == Screen.Favourites.route || currentRoute == Screen.Search.route

    // Waits for a pending recipe ID and navigates to it (timer notification)
    val pendingId by pendingRecipeId.collectAsState()
    LaunchedEffect(pendingId) {
        val id = pendingId ?: return@LaunchedEffect
        navController.navigate(Screen.Detail.createRoute(id)) {
            launchSingleTop = true
        }
        MainActivity.clearPendingRecipeId()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val onNavigateToTab = remember(navController) {
        { route: String ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState    = true
            }
        }
    }

    // Bottom sheet for adding a new recipe
    AddRecipeBottomSheet(
        showSheet    = showAddSheet,
        sheetState   = sheetState,
        scope        = scope,
        navController = navController,
        onDismiss    = { showAddSheet = false }
    )

    Scaffold(
        bottomBar = {
            if (showNavBar) {
                MainNavigationBar(
                    currentDestination = navBackStackEntry?.destination,
                    onTabClick = onNavigateToTab,
                    onNewClick = { showAddSheet = true }
                )
            }
        }
    ) { innerPadding ->
        NavHost(    // Container for the screens
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
                RecipeFormScreen(
                    recipeId       = null,
                    isImport       = false,
                    viewModel      = recipeViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Import.route) {
                RecipeFormScreen(
                    recipeId       = null,
                    isImport       = true,
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
                route     = Screen.Detail.ROUTE_WITH_ARGS,
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
                route     = Screen.Edit.ROUTE_WITH_ARGS,
                arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("recipeId") ?: return@composable
                RecipeFormScreen(
                    recipeId       = id,
                    isImport       = false,
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