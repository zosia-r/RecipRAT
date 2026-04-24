package com.example.recipapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.recipapp.data.local.relation.RecipeWithDetails
import com.example.recipapp.ui.viewmodel.RecipeViewModel

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

@Composable
private fun RecipeCard(
    recipeWithDetails: RecipeWithDetails,
    onToggleFavourite: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val recipe = recipeWithDetails.recipe

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onToggleFavourite) {
                        Icon(
                            imageVector = if (recipe.isFavourite)
                                Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Ulubione",
                            tint = if (recipe.isFavourite)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (recipe.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (recipeWithDetails.ingredients.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Składniki: " + recipeWithDetails.ingredients
                        .joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (recipeWithDetails.photos.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "📷 ${recipeWithDetails.photos.size} zdjęcie(-a)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}