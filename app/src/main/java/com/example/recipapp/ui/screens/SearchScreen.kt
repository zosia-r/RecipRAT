package com.example.recipapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.recipapp.data.RecipeTag
import com.example.recipapp.data.TagCategory
import com.example.recipapp.ui.theme.DeepTeal
import com.example.recipapp.ui.theme.DeepTealLight
import com.example.recipapp.ui.theme.DustyRose
import com.example.recipapp.ui.theme.DustyRoseLight
import com.example.recipapp.ui.theme.MintCream
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Search",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Pasek wyszukiwania + przycisk filtrów ─────────────────────────
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

                // Przycisk filtrów z odznaką
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

            // ── Aktywne filtry-pigułki pod paskiem ────────────────────────────
            if (!showFilters && selectedTags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                ) {
                    selectedTags.forEach { tag ->
                        FilterChip(
                            selected     = true,
                            onClick      = { viewModel.onTagToggled(tag) },
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

            // ── Panel filtrów (rozwijany) ──────────────────────────────────────
            AnimatedVisibility(visible = showFilters) {
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

                        TagCategory.entries.forEach { category ->
                            val tagsInCategory     = RecipeTag.entries.filter { it.category == category }
                            val selectedInCategory = tagsInCategory.count { it in selectedTags }

                            Text(
                                text = category.label +
                                        if (selectedInCategory > 0) " ($selectedInCategory)" else "",
                                style    = MaterialTheme.typography.labelMedium,
                                color    = if (selectedInCategory > 0) DeepTeal
                                else DustyRose,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                tagsInCategory.forEach { tag ->
                                    val selected = tag in selectedTags
                                    FilterChip(
                                        selected = selected,
                                        onClick  = { viewModel.onTagToggled(tag) },
                                        label    = { Text(tag.label, style = MaterialTheme.typography.labelMedium) },
                                        shape    = RoundedCornerShape(50),
                                        colors   = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DeepTeal,
                                            selectedLabelColor     = MintCream,
                                            containerColor         = DustyRoseLight,
                                            labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    OutlinedButton(
                        onClick  = { showFilters = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTeal),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(DeepTeal)
                        )
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Hide filters", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Wyniki ────────────────────────────────────────────────────────
            if (searchResults.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint     = DustyRose,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (searchQuery.isBlank() && selectedTags.isEmpty())
                                "No recipes yet\nAdd your first recipe!"
                            else
                                "No recipes found\nmatching your search",
                            style     = MaterialTheme.typography.bodyLarge,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
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