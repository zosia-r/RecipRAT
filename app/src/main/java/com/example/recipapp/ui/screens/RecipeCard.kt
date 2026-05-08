package com.example.recipapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.recipapp.data.RecipeTag
import com.example.recipapp.data.relation.RecipeWithDetails

@Composable
fun RecipeCard(
    recipeWithDetails: RecipeWithDetails,
    onToggleFavourite: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val recipe = recipeWithDetails.recipe

    val tags = recipe.tags.mapNotNull { name ->
        runCatching { RecipeTag.valueOf(name) }.getOrNull()
    }

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
                IconButton(onClick = onToggleFavourite) {
                    Icon(
                        imageVector = if (recipe.isFavourite)
                            Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favourites",
                        tint = if (recipe.isFavourite)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { tag ->
                        SuggestionChip(
                            onClick = {},
                            label   = { Text(tag.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}