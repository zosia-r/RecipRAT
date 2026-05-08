package com.example.recipapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recipapp.data.RecipeTag
import com.example.recipapp.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    recipeId: Long,
    viewModel: RecipeViewModel,
    onNavigateBack: () -> Unit
) {
    val recipeWithDetails by viewModel.getRecipeById(recipeId).collectAsState(initial = null)

    if (recipeWithDetails == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val recipe = recipeWithDetails!!.recipe

    var title by remember { mutableStateOf(recipe.title) }
    var description by remember { mutableStateOf(recipe.description) }
    var executionDescription by remember { mutableStateOf(recipe.executionDescription) }
    var ingredients by remember {
        mutableStateOf(recipeWithDetails!!.ingredients.map { it.name }.ifEmpty { listOf("") })
    }
    var existingPhotoPaths by remember {
        mutableStateOf(recipeWithDetails!!.photos.map { it.uri })
    }
    var newPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Wypełnij tagi z bazy – zamieniamy nazwy enumów z powrotem na obiekty RecipeTag
    var selectedTags by remember {
        mutableStateOf(
            recipe.tags.mapNotNull { name ->
                runCatching { RecipeTag.valueOf(name) }.getOrNull()
            }
        )
    }

    var titleError by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> newPhotoUris = newPhotoUris + uris }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Recipe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("Title *") },
                isError = titleError,
                supportingText = { if (titleError) Text("Title is required") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // ── Tagi ─────────────────────────────────────────────────────────
            Text("Tags", style = MaterialTheme.typography.titleSmall)
            TagSelector(
                selectedTags = selectedTags,
                onTagToggle  = { tag ->
                    selectedTags = if (tag in selectedTags)
                        selectedTags - tag
                    else
                        selectedTags + tag
                }
            )

            // ── Składniki ────────────────────────────────────────────────────
            Text("Ingredients", style = MaterialTheme.typography.titleSmall)
            ingredients.forEachIndexed { index, ingredient ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ingredient,
                        onValueChange = { newVal ->
                            ingredients = ingredients.toMutableList().also { it[index] = newVal }
                        },
                        label = { Text("Ingredient ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    if (ingredients.size > 1) {
                        IconButton(onClick = {
                            ingredients = ingredients.toMutableList().also { it.removeAt(index) }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove")
                        }
                    }
                }
            }
            TextButton(
                onClick  = { ingredients = ingredients + "" },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add ingredient")
            }

            OutlinedTextField(
                value = executionDescription,
                onValueChange = { executionDescription = it },
                label = { Text("Execution description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            // ── Zdjęcia ──────────────────────────────────────────────────────
            val totalPhotos = existingPhotoPaths.size + newPhotoUris.size
            Text("Photos ($totalPhotos)", style = MaterialTheme.typography.titleSmall)

            if (existingPhotoPaths.isNotEmpty()) {
                Text("Current photos:", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                existingPhotoPaths.forEach { path ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(path.substringAfterLast("/"),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f))
                        IconButton(onClick = { existingPhotoPaths = existingPhotoPaths - path }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove photo")
                        }
                    }
                }
            }

            if (newPhotoUris.isNotEmpty()) {
                Text("New photos:", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                newPhotoUris.forEach { uri ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(uri.lastPathSegment ?: uri.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f))
                        IconButton(onClick = { newPhotoUris = newPhotoUris - uri }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove photo")
                        }
                    }
                }
            }

            OutlinedButton(
                onClick  = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add photos")
            }

            Button(
                onClick = {
                    if (title.isBlank()) { titleError = true; return@Button }
                    val removedPaths = recipeWithDetails!!.photos
                        .map { it.uri }
                        .filter { it !in existingPhotoPaths }
                    viewModel.updateRecipe(
                        recipe               = recipe.copy(
                            title                = title.trim(),
                            description          = description.trim(),
                            executionDescription = executionDescription.trim()
                        ),
                        ingredients          = ingredients,
                        newPhotoUris         = newPhotoUris,
                        removedPhotoPaths    = removedPaths,
                        tags                 = selectedTags
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Text("Save changes")
            }
        }
    }
}