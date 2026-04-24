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
import com.example.recipapp.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRecipeScreen(
    viewModel: RecipeViewModel,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var executionDescription by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf(listOf("")) }
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var titleError by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> photoUris = photoUris + uris }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Recipe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
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

            // Tytuł
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                label = { Text("Title *") },
                isError = titleError,
                supportingText = { if (titleError) Text("Title is required") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Opis
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Składniki
            Text("Ingrediets", style = MaterialTheme.typography.titleSmall)
            ingredients.forEachIndexed { index, ingredient ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ingredient,
                        onValueChange = { newVal ->
                            ingredients = ingredients.toMutableList()
                                .also { it[index] = newVal }
                        },
                        label = { Text("Ingredient ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    if (ingredients.size > 1) {
                        IconButton(onClick = {
                            ingredients = ingredients.toMutableList()
                                .also { it.removeAt(index) }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Usuń")
                        }
                    }
                }
            }
            TextButton(
                onClick = { ingredients = ingredients + "" },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add ingredient")
            }

            // Opis wykonania
            OutlinedTextField(
                value = executionDescription,
                onValueChange = { executionDescription = it },
                label = { Text("Execution description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            // Zdjęcia
            Text("Photos (${photoUris.size})", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(
                onClick = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add photos")
            }
            if (photoUris.isNotEmpty()) {
                photoUris.forEach { uri ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uri.lastPathSegment ?: uri.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            photoUris = photoUris - uri
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Usuń zdjęcie")
                        }
                    }
                }
            }

            // Zapisz
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    viewModel.addRecipe(
                        title = title.trim(),
                        description = description.trim(),
                        executionDescription = executionDescription.trim(),
                        ingredients = ingredients,
                        photoUris = photoUris.map { it.toString() }
                    )
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text("Save Recipe")
            }
        }
    }
}