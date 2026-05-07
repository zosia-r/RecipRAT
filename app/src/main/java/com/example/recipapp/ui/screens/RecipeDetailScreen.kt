package com.example.recipapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.recipapp.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    viewModel: RecipeViewModel,
    onNavigateBack: () -> Unit
) {
    val recipeWithDetails by viewModel.getRecipeById(recipeId)
        .collectAsState(initial = null)

    val recipe = recipeWithDetails?.recipe

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    recipe?.let {
                        IconButton(onClick = {
                            viewModel.toggleFavourite(it.id, it.isFavourite)
                        }) {
                            Icon(
                                imageVector = if (it.isFavourite)
                                    Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favourites",
                                tint = if (it.isFavourite)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (recipeWithDetails == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Zdjęcia
            val photos = recipeWithDetails!!.photos
            if (photos.isNotEmpty()) {
                AsyncImage(
                    model = photos.first().uri,
                    contentDescription = "Recipe photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )

            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Opis
                if (recipe!!.description.isNotBlank()) {
                    SectionCard(title = "Opis") {
                        Text(
                            text = recipe.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Składniki
                val ingredients = recipeWithDetails!!.ingredients
                if (ingredients.isNotEmpty()) {
                    SectionCard(title = "Ingredients") {
                        ingredients.forEachIndexed { index, ingredient ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "• ",
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = ingredient.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (ingredient.amount.isNotBlank()) {
                                    Text(
                                        text = ingredient.amount,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (index < ingredients.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }

                // Sposób wykonania
                if (recipe.executionDescription.isNotBlank()) {
                    SectionCard(title = "Execution description") {
                        Text(
                            text = recipe.executionDescription,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Pozostałe zdjęcia
                if (photos.size > 1) {
                    SectionCard(title = "Photos") {
                        photos.drop(1).forEach { photo ->
                            AsyncImage(
                                model = photo.uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}