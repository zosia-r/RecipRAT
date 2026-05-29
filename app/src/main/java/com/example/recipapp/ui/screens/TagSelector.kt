package com.example.recipapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recipapp.data.entity.RecipeTag
import com.example.recipapp.data.entity.TagCategory


/**
 * Composable for displaying a list of tags grouped by category.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelector(
    selectedTags: List<RecipeTag>,
    onTagToggle: (RecipeTag) -> Unit,
    modifier: Modifier = Modifier
) {
    val tagsByCategory = remember {
        RecipeTag.entries.groupBy { it.category }
    }

    val onTagToggleStable = remember(onTagToggle) { onTagToggle }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TagCategory.entries.forEach { category ->
            val tagsInCategory = tagsByCategory[category] ?: emptyList()

            val selectedInCategory = tagsInCategory.count { it in selectedTags }

            Text(
                text  = category.label + if (selectedInCategory > 0) " ($selectedInCategory)" else "",
                style = MaterialTheme.typography.labelMedium,
                color = if (selectedInCategory > 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tagsInCategory, key = { it.name }) { tag ->
                    val selected = tag in selectedTags
                    FilterChip(
                        selected = selected,
                        onClick  = { onTagToggleStable(tag) },
                        label    = {
                            Text(
                                tag.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape  = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor   = MaterialTheme.colorScheme.primary,
                            selectedLabelColor       = MaterialTheme.colorScheme.onPrimary,
                            containerColor           = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor               = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = selected,
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            borderColor         = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
    }
}