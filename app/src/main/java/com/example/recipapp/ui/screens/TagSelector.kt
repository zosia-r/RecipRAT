package com.example.recipapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recipapp.data.RecipeTag
import com.example.recipapp.data.TagCategory

/**
 * Wielokrotnego użytku komponent wyboru tagów.
 * Wyświetla tagi pogrupowane po kategoriach jako poziomy scroll chipów.
 *
 * @param selectedTags  aktualnie wybrane tagi
 * @param onTagToggle   callback gdy użytkownik kliknie tag
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelector(
    selectedTags: List<RecipeTag>,
    onTagToggle: (RecipeTag) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TagCategory.entries.forEach { category ->
            val tagsInCategory = RecipeTag.entries.filter { it.category == category }

            Text(
                text  = category.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tagsInCategory) { tag ->
                    FilterChip(
                        selected = tag in selectedTags,
                        onClick  = { onTagToggle(tag) },
                        label    = { Text(tag.label) }
                    )
                }
            }
        }
    }
}