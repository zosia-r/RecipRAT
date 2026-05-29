package com.example.recipapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.recipapp.data.entity.RecipeTag
import com.example.recipapp.data.entity.TagCategory
import com.example.recipapp.ui.theme.DeepTeal
import com.example.recipapp.ui.theme.DustyRose
import com.example.recipapp.ui.theme.DustyRoseLight
import com.example.recipapp.ui.theme.MintCream
import com.example.recipapp.viewmodel.RecipeViewModel

/**
 * Screen for searching recipes.
 * Three possible states:
 * 1. Data not yet loaded -> loading indicator
 * 2. Empty data -> no results
 * 3. Data loaded -> list of recipes
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (Long) -> Unit
) {
    val searchQuery   by viewModel.searchQuery.collectAsState()
    val selectedTags  by viewModel.selectedTags.collectAsState()

    val searchResults by viewModel.searchResults.collectAsState(initial = null)

    var showFilters by remember { mutableStateOf(false) }

    val onToggleFavouriteStable = remember(viewModel) {
        { id: Long, currentFav: Boolean -> viewModel.toggleFavourite(id, currentFav) }
    }
    val onRecipeClickStable = remember(onRecipeClick) {
        { id: Long -> onRecipeClick(id) }
    }
    val onTagToggledStable = remember(viewModel) {
        { tag: RecipeTag -> viewModel.onTagToggled(tag) }
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
                        Icons.Default.Search,
                        contentDescription = null,
                        tint     = DeepTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Search",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor    = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier      = Modifier.weight(1f),
                    placeholder   = { Text("Search recipes…", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon   = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = if (searchQuery.isNotEmpty()) DeepTeal
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = DeepTeal)
                            }
                        }
                    },
                    singleLine = true,
                    shape      = RoundedCornerShape(16.dp),
                    colors     = recipeTextFieldColors()
                )

                IconButton(onClick = { showFilters = !showFilters }) {
                    BadgedBox(
                        badge = {
                            if (selectedTags.isNotEmpty()) {
                                Badge(
                                    containerColor = DeepTeal,
                                    contentColor   = MintCream
                                ) {
                                    Text(selectedTags.size.toString(), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector        = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint               = if (selectedTags.isNotEmpty()) DeepTeal
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!showFilters && selectedTags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                ) {
                    selectedTags.forEach { tag ->
                        FilterChip(
                            selected     = true,
                            onClick      = { onTagToggledStable(tag) },
                            label        = { Text(tag.label, style = MaterialTheme.typography.labelMedium) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            shape  = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepTeal,
                                selectedLabelColor     = MintCream,
                                selectedLeadingIconColor = MintCream,
                                selectedTrailingIconColor = MintCream
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showFilters,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                Column {
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(top = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                            modifier              = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Filter by tags",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (selectedTags.isNotEmpty()) {
                                TextButton(onClick = { viewModel.clearTagFilters() }) {
                                    Text("Clear all", color = DustyRose, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }

                        TagSelector(
                            selectedTags = selectedTags.toList(),
                            onTagToggle  = onTagToggledStable,
                            modifier     = Modifier.padding(top = 4.dp)
                        )

                        if (selectedTags.map { it.category }.distinct().size > 1) {
                            Text(
                                text     = "Showing recipes matching all selected categories",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    OutlinedButton(
                        onClick  = { showFilters = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTeal),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                            .copy(brush = androidx.compose.ui.graphics.SolidColor(DeepTeal))
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Hide filters", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ********** Search results **********
            when {
                // State 1: data not yet loaded -> loading indicator
                searchResults == null -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DeepTeal)
                    }
                }

                // State 2: empty data -> no results
                searchResults!!.isEmpty() -> {
                    Box(
                        modifier       = Modifier.weight(1f).fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier            = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint     = DustyRose,
                                modifier = Modifier.size(48.dp)
                            )

                            if (searchQuery.isBlank() && selectedTags.isEmpty()) {
                                Text(
                                    text  = "No recipes yet",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text      = "Add your first recipe!",
                                    style     = MaterialTheme.typography.bodyMedium,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text      = "No recipes found\nmatching your search",
                                    style     = MaterialTheme.typography.bodyLarge,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // State 3: data loaded -> list of recipes
                else -> {
                    LazyColumn(
                        modifier            = Modifier.weight(1f).fillMaxSize(),
                        contentPadding      = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(searchResults!!, key = { it.recipe.id }) { recipeWithDetails ->
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
}