package com.example.recipapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.recipapp.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    viewModel: RecipeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val recipeWithDetails by viewModel.getRecipeById(recipeId).collectAsState(initial = null)
    val recipe = recipeWithDetails?.recipe

    // stan checkboxów dla składników – key: index składnika, value: czy odhaczony
    val checkedIngredients = remember { mutableStateMapOf<Int, Boolean>() }

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
                    recipe?.let { r ->
                        // Udostępnij
                        IconButton(onClick = {
                            val text = buildShareText(
                                title = r.title,
                                description = r.description,
                                ingredients = recipeWithDetails!!.ingredients.map { it.name },
                                steps = r.executionDescription
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                                putExtra(Intent.EXTRA_SUBJECT, r.title)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share recipe"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }

                        // Edytuj
                        IconButton(onClick = { onNavigateToEdit(r.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }

                        // Ulubione
                        IconButton(onClick = {
                            viewModel.toggleFavourite(r.id, r.isFavourite)
                        }) {
                            Icon(
                                imageVector = if (r.isFavourite)
                                    Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favourite",
                                tint = if (r.isFavourite)
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
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Zdjęcia – poziomy pager ──────────────────────────────────────
            val photos = recipeWithDetails!!.photos
            if (photos.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { photos.size })

                Box {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) { page ->
                        AsyncImage(
                            model = photos[page].uri,
                            contentDescription = "Photo ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Wskaźnik strony (kropki) – widoczny tylko gdy >1 zdjęcie
                    if (photos.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            photos.indices.forEach { index ->
                                val isSelected = pagerState.currentPage == index
                                Surface(
                                    modifier = Modifier.size(if (isSelected) 8.dp else 6.dp),
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                ) {}
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Opis ─────────────────────────────────────────────────────
                if (recipe!!.description.isNotBlank()) {
                    SectionCard(title = "Opis") {
                        Text(
                            text = recipe.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // ── Składniki z checkboxami ──────────────────────────────────
                val ingredients = recipeWithDetails!!.ingredients
                if (ingredients.isNotEmpty()) {
                    SectionCard(title = "Ingredients") {
                        ingredients.forEachIndexed { index, ingredient ->
                            val isChecked = checkedIngredients[index] == true
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checkedIngredients[index] = it }
                                )
                                Text(
                                    text = ingredient.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isChecked)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurface,
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

                        // Przycisk resetowania checkboxów
                        if (checkedIngredients.values.any { it }) {
                            TextButton(
                                onClick = { checkedIngredients.clear() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Reset")
                            }
                        }
                    }
                }

                // ── Sposób wykonania ─────────────────────────────────────────
                if (recipe.executionDescription.isNotBlank()) {
                    SectionCard(title = "Execution description") {
                        Text(
                            text = recipe.executionDescription,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/** Buduje tekst do udostępnienia */
private fun buildShareText(
    title: String,
    description: String,
    ingredients: List<String>,
    steps: String
): String = buildString {
    appendLine("🍴 $title")
    if (description.isNotBlank()) {
        appendLine()
        appendLine(description)
    }
    if (ingredients.isNotEmpty()) {
        appendLine()
        appendLine("Ingredients:")
        ingredients.forEach { appendLine("• $it") }
    }
    if (steps.isNotBlank()) {
        appendLine()
        appendLine("Preparation:")
        appendLine(steps)
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