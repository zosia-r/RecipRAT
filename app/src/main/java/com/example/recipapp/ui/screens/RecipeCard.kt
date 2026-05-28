package com.example.recipapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.recipapp.data.RecipeTag
import com.example.recipapp.data.relation.RecipeWithDetails
import com.example.recipapp.ui.theme.CherryRose
import com.example.recipapp.ui.theme.DustyRoseLight

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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
            pressedElevation  = 6.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text  = recipe.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                )
                IconButton(
                    onClick  = onToggleFavourite,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (recipe.isFavourite)
                            Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (recipe.isFavourite) "Remove from favourites" else "Add to favourites",
                        tint = if (recipe.isFavourite) CherryRose
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (recipe.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text     = recipe.description,
                    style    = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = DustyRoseLight,
                            modifier = Modifier.height(26.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    text  = tag.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Dekoracyjna linia akcentująca
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(
                modifier  = Modifier.padding(end = 12.dp),
                thickness = 1.dp,
                color     = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}