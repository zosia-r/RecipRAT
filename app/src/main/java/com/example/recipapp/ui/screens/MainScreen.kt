package com.example.recipapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipapp.Recipapp
import com.example.recipapp.ui.theme.CherryRose
import com.example.recipapp.ui.theme.CherryRoseLight
import com.example.recipapp.ui.theme.DeepTeal
import com.example.recipapp.ui.theme.DeepTealLight
import com.example.recipapp.ui.theme.MintCream
import com.example.recipapp.viewmodel.RecipeViewModel
import com.example.recipapp.viewmodel.RecipeViewModelFactory
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Favourites : Screen("favourites", "Favourites", Icons.Filled.Favorite)
    object Search     : Screen("search",     "Search",     Icons.Filled.Search)
    object New        : Screen("new",        "New",        Icons.Filled.Add)
    object Import     : Screen("import",     "Import",     Icons.Filled.Add)
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

@OptIn(ExperimentalMaterial3Api::class)
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

    // ── Bottom sheet „dodaj / importuj" ──────────────────────────────────────
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState       = sheetState,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor   = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Add recipe",
                    style    = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // ── Nowy ręcznie ──────────────────────────────────────────────
                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showAddSheet = false
                            navController.navigate(Screen.New.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepTeal,
                        contentColor   = MintCream
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Create new recipe", style = MaterialTheme.typography.labelLarge)
                }

                // ── Import ────────────────────────────────────────────────────
                OutlinedButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showAddSheet = false
                            navController.navigate(Screen.Import.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTeal),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                        .copy(brush = androidx.compose.ui.graphics.SolidColor(DeepTeal))
                ) {
                    // ikona „wklej" złożona z dostępnych ikon Material
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Import from text", style = MaterialTheme.typography.labelLarge)
                }
            }
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

                    // ── Przycisk + — otwiera sheet ────────────────────────────
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
                                "New",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selected = false,
                        onClick  = { showAddSheet = true },
                        colors   = NavigationBarItemDefaults.colors(
                            selectedIconColor   = androidx.compose.ui.graphics.Color.Transparent,
                            unselectedIconColor = androidx.compose.ui.graphics.Color.Transparent,
                            indicatorColor      = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )

                    // ── Szukaj ────────────────────────────────────────────────
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
            composable(Screen.Import.route) {
                ImportRecipeScreen(
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