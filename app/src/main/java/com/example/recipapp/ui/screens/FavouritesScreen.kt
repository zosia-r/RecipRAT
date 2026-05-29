package com.example.recipapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.recipapp.ui.components.RecipeCard
import com.example.recipapp.viewmodel.RecipeViewModel

/**
 * Screen for displaying a list of favourite recipes.
 * Three possible states:
 * 1. Data not yet loaded -> loading indicator
 * 2. Empty data -> info 'no favourites'
 * 3. Data loaded -> list of recipes
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (Long) -> Unit
) {
    val recipes by viewModel.favouriteRecipes.collectAsState(initial = null)

    val onToggleFavouriteStable = remember(viewModel) {
        { id: Long, currentFav: Boolean ->
            viewModel.toggleFavourite(id, currentFav)
        }
    }

    val onRecipeClickStable = remember(onRecipeClick) {
        { id: Long ->
            onRecipeClick(id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Favourites",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor    = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )


        when {
            // State 1: data not yet loaded -> loading indicator
            recipes == null -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            // State 2: empty data -> no favourites
            recipes!!.isEmpty() -> {
                Box(
                    modifier       = Modifier.weight(1f).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier            = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text  = "No favourites yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text      = "Tap the heart on any recipe\nto save it here",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // State 3: data loaded -> list of recipes
            else -> {
                LazyColumn(
                    modifier            = Modifier.weight(1f).fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recipes!!, key = { it.recipe.id }) { recipeWithDetails ->
                        RecipeCard(
                            recipeWithDetails = recipeWithDetails,
                            onToggleFavourite = {
                                onToggleFavouriteStable(
                                    recipeWithDetails.recipe.id,
                                    recipeWithDetails.recipe.isFavourite
                                )
                            },
                            onClick = { onRecipeClickStable(recipeWithDetails.recipe.id) }
                        )
                    }
                }
            }
        }
    }
}