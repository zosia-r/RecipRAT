package com.example.recipapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.recipapp.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (Long) -> Unit
) {
    val searchQuery  by viewModel.searchQuery.collectAsState()
    val selectedTag  by viewModel.selectedTag.collectAsState()
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

                // Ikona filtra – podświetlona gdy aktywny tag
                IconButton(onClick = { showFilters = !showFilters }) {
                    BadgedBox(
                        badge = {
                            if (selectedTag != null) Badge()   // czerwona kropka gdy filtr aktywny
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = if (selectedTag != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Panel filtrów (rozwijany) ────────────────────────────────────
            AnimatedVisibility(visible = showFilters) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        Text("Filter by tag", style = MaterialTheme.typography.titleSmall)
                        if (selectedTag != null) {
                            TextButton(onClick = { viewModel.onTagSelected(null) }) {
                                Text("Clear filter")
                            }
                        }
                    }

                    // Wszystkie tagi jako jedna pozioma lista chipów
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(RecipeTag.entries) { tag ->
                            FilterChip(
                                selected = tag == selectedTag,
                                onClick  = {
                                    // kliknięcie aktywnego taga = odznacz
                                    viewModel.onTagSelected(if (tag == selectedTag) null else tag)
                                },
                                label = { Text(tag.label) }
                            )
                        }
                    }
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Pokaż aktywny filtr pod paskiem gdy panel jest schowany
            if (!showFilters && selectedTag != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Text("Tag: ", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FilterChip(
                        selected = true,
                        onClick  = { viewModel.onTagSelected(null) },
                        label    = { Text(selectedTag!!.label) },
                        trailingIcon = {
                            Icon(Icons.Default.Clear, contentDescription = "Remove filter",
                                modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Wyniki ───────────────────────────────────────────────────────
            if (searchResults.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = when {
                            searchQuery.isBlank() && selectedTag == null ->
                                "Brak przepisów.\nDodaj pierwszy przepis!"
                            else ->
                                "Brak wyników dla podanych kryteriów"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
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