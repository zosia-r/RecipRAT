package com.example.recipapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recipapp.data.RecipeTag
import com.example.recipapp.data.TagCategory
import com.example.recipapp.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (Long) -> Unit
) {
    val searchQuery   by viewModel.searchQuery.collectAsState()
    val selectedTags  by viewModel.selectedTags.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Search") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Pasek wyszukiwania + przycisk filtrów ────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier      = Modifier.weight(1f),
                    placeholder   = { Text("Search recipes...") },
                    leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon  = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape      = MaterialTheme.shapes.large
                )

                IconButton(onClick = { showFilters = !showFilters }) {
                    BadgedBox(
                        badge = {
                            if (selectedTags.isNotEmpty()) {
                                Badge { Text(selectedTags.size.toString()) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector        = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint               = if (selectedTags.isNotEmpty())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Aktywne filtry pod paskiem gdy panel schowany ────────────────
            if (!showFilters && selectedTags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                ) {
                    selectedTags.forEach { tag ->
                        FilterChip(
                            selected     = true,
                            onClick      = { viewModel.onTagToggled(tag) },
                            label        = { Text(tag.label) },
                            trailingIcon = {
                                Icon(Icons.Default.Clear, contentDescription = "Remove",
                                    modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }
            }

            // ── Panel filtrów (rozwijany, scrollowalny) ──────────────────────
            AnimatedVisibility(visible = showFilters) {
                Column {
                    // Scrollowalna część z tagami
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
                            Text("Filter by tags", style = MaterialTheme.typography.titleSmall)
                            if (selectedTags.isNotEmpty()) {
                                TextButton(onClick = { viewModel.clearTagFilters() }) {
                                    Text("Clear all")
                                }
                            }
                        }

                        // Każda kategoria osobno
                        TagCategory.entries.forEach { category ->
                            val tagsInCategory = RecipeTag.entries.filter { it.category == category }
                            val selectedInCategory = tagsInCategory.count { it in selectedTags }

                            Text(
                                text     = category.label +
                                        if (selectedInCategory > 0) " ($selectedInCategory)" else "",
                                style    = MaterialTheme.typography.labelMedium,
                                color    = if (selectedInCategory > 0)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                tagsInCategory.forEach { tag ->
                                    FilterChip(
                                        selected = tag in selectedTags,
                                        onClick  = { viewModel.onTagToggled(tag) },
                                        label    = { Text(tag.label) }
                                    )
                                }
                            }
                        }

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

                    // Przycisk zamknięcia filtrów – zawsze widoczny na dole panelu
                    HorizontalDivider()
                    OutlinedButton(
                        onClick  = { showFilters = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Hide filters")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Wyniki ───────────────────────────────────────────────────────
            if (searchResults.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text  = if (searchQuery.isBlank() && selectedTags.isEmpty())
                            "No recipes dound.\nAdd your first recipe!"
                        else
                            "No recipes found matching your search",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults, key = { it.recipe.id }) { recipeWithDetails ->
                        RecipeCard(
                            recipeWithDetails = recipeWithDetails,
                            onToggleFavourite = {
                                viewModel.toggleFavourite(
                                    recipeWithDetails.recipe.id,
                                    recipeWithDetails.recipe.isFavourite
                                )
                            },
                            onDelete = { viewModel.deleteRecipe(recipeWithDetails.recipe) },
                            onClick  = { onRecipeClick(recipeWithDetails.recipe.id) }
                        )
                    }
                }
            }
        }
    }
}