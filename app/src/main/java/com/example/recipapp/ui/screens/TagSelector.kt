package com.example.recipapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recipapp.data.RecipeTag
import com.example.recipapp.data.TagCategory
import com.example.recipapp.ui.theme.DeepTeal
import com.example.recipapp.ui.theme.DustyRose
import com.example.recipapp.ui.theme.DustyRoseLight
import com.example.recipapp.ui.theme.MintCream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelector(
    selectedTags: List<RecipeTag>,
    onTagToggle: (RecipeTag) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TagCategory.entries.forEach { category ->
            val tagsInCategory = RecipeTag.entries.filter { it.category == category }

            Text(
                text  = category.label,
                style = MaterialTheme.typography.labelMedium,
                color = DustyRose
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tagsInCategory) { tag ->
                    val selected = tag in selectedTags
                    FilterChip(
                        selected = selected,
                        onClick  = { onTagToggle(tag) },
                        label    = {
                            Text(
                                tag.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape  = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor   = DeepTeal,
                            selectedLabelColor       = MintCream,
                            containerColor           = DustyRoseLight,
                            labelColor               = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled          = true,
                            selected         = selected,
                            selectedBorderColor = DeepTeal,
                            borderColor      = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
    }
}