package com.example.recipapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recipapp.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (Long) -> Unit
) {
    val recipes by viewModel.allRecipes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Wszystkie przepisy") })
        }
    ) { padding ->
        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Brak przepisów.\nDodaj pierwszy przepis!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recipes, key = { it.recipe.id }) { recipeWithDetails ->
                    RecipeCard(
                        recipeWithDetails = recipeWithDetails,
                        onToggleFavourite = {
                            viewModel.toggleFavourite(
                                recipeWithDetails.recipe.id,
                                recipeWithDetails.recipe.isFavourite
                            )
                        },
                        onDelete = {
                            viewModel.deleteRecipe(recipeWithDetails.recipe)
                        },
                        onClick = { onRecipeClick(recipeWithDetails.recipe.id) }
                    )
                }
            }
        }
    }
}