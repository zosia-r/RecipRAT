package com.example.recipapp.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.recipapp.data.RecipeTag
import com.example.recipapp.data.sharing.buildShareText
import com.example.recipapp.timer.TimerService
import com.example.recipapp.timer.TimerState
import com.example.recipapp.timer.toTimeString
import com.example.recipapp.ui.theme.CherryRose
import com.example.recipapp.ui.theme.CherryRoseLight
import com.example.recipapp.ui.theme.DeepTeal
import com.example.recipapp.ui.theme.DeepTealLight
import com.example.recipapp.ui.theme.DustyRose
import com.example.recipapp.ui.theme.DustyRoseLight
import com.example.recipapp.ui.theme.MintCream
import com.example.recipapp.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    viewModel: RecipeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onPhotoClick: (initialUri: String, allUris: List<String>) -> Unit
) {
    val context = LocalContext.current
    val recipeWithDetails by viewModel.getRecipeById(recipeId).collectAsState(initial = null)
    val recipe = recipeWithDetails?.recipe

    val checkedIngredients = remember { mutableStateMapOf<Int, Boolean>() }

    val allTimers by TimerService.timers.collectAsState()
    val timerState       = allTimers[recipeId]
    var showTimerDialog  by remember { mutableStateOf(false) }
    var showMenu         by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val showAlarmDialog  = timerState is TimerState.Finished

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        recipe?.title ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    recipe?.let { r ->

                        // ── Timer z odliczaniem ───────────────────────────────
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnimatedVisibility(
                                visible = timerState is TimerState.Running,
                                enter   = fadeIn(),
                                exit    = fadeOut()
                            ) {
                                if (timerState is TimerState.Running) {
                                    Text(
                                        text  = timerState.remainingSec.toTimeString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = DeepTeal
                                    )
                                }
                            }
                            IconButton(onClick = { showTimerDialog = true }) {
                                Icon(
                                    imageVector = if (timerState is TimerState.Running)
                                        Icons.Filled.Timer else Icons.Outlined.Timer,
                                    contentDescription = "Timer",
                                    tint = if (timerState is TimerState.Running) DeepTeal
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // ── Serduszko ─────────────────────────────────────────
                        IconButton(onClick = { viewModel.toggleFavourite(r.id, r.isFavourite) }) {
                            Icon(
                                imageVector = if (r.isFavourite)
                                    Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favourite",
                                tint = if (r.isFavourite) CherryRose
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // ── Menu 3 kropki ─────────────────────────────────────
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                            }
                            DropdownMenu(
                                expanded         = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text        = { Text("Edit", style = MaterialTheme.typography.bodyMedium) },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = DeepTeal) },
                                    onClick     = { showMenu = false; onNavigateToEdit(r.id) }
                                )
                                DropdownMenuItem(
                                    text        = { Text("Share", style = MaterialTheme.typography.bodyMedium) },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = DeepTeal) },
                                    onClick     = {
                                        showMenu = false
                                        val text = buildShareText(
                                            title       = r.title,
                                            description = r.description,
                                            ingredients = recipeWithDetails!!.ingredients.map { it.name },
                                            steps       = r.executionDescription
                                        )
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, text)
                                            putExtra(Intent.EXTRA_SUBJECT, r.title)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share recipe"))
                                    }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                DropdownMenuItem(
                                    text        = { Text("Delete", color = CherryRose, style = MaterialTheme.typography.bodyMedium) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CherryRose) },
                                    onClick     = { showMenu = false; showDeleteDialog = true }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor            = MaterialTheme.colorScheme.background,
                    titleContentColor         = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor    = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (recipeWithDetails == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = DeepTeal)
            }
            return@Scaffold
        }

        val photos      = recipeWithDetails!!.photos
        val allPhotoUris = photos.map { it.uri }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Karuzela zdjęć ────────────────────────────────────────────────
            if (photos.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { photos.size })
                Box {
                    HorizontalPager(
                        state    = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) { page ->
                        AsyncImage(
                            model              = photos[page].uri,
                            contentDescription = "Photo ${page + 1}",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier
                                .fillMaxSize()
                                .clickable { onPhotoClick(photos[page].uri, allPhotoUris) }
                        )
                    }

                    // Wskaźniki stron
                    if (photos.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            photos.indices.forEach { index ->
                                val selected = pagerState.currentPage == index
                                Surface(
                                    modifier = Modifier.size(if (selected) 8.dp else 6.dp),
                                    shape    = CircleShape,
                                    color    = if (selected) MintCream
                                    else MintCream.copy(alpha = 0.45f)
                                ) {}
                            }
                        }
                    }
                }
            }

            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Tagi ──────────────────────────────────────────────────────
                val tags = recipe!!.tags.mapNotNull { name ->
                    runCatching { RecipeTag.valueOf(name) }.getOrNull()
                }
                if (tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement   = Arrangement.spacedBy(4.dp)
                    ) {
                        tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = DustyRoseLight,
                                modifier = Modifier.height(26.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    Text(
                                        text  = tag.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Opis ──────────────────────────────────────────────────────
                if (recipe.description.isNotBlank()) {
                    DetailSectionCard(title = "Description") {
                        Text(
                            recipe.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // ── Składniki ─────────────────────────────────────────────────
                val ingredients = recipeWithDetails!!.ingredients
                if (ingredients.isNotEmpty()) {
                    DetailSectionCard(title = "Ingredients") {
                        ingredients.forEachIndexed { index, ingredient ->
                            val isChecked = checkedIngredients[index] == true
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked         = isChecked,
                                    onCheckedChange = { checkedIngredients[index] = it },
                                    colors          = CheckboxDefaults.colors(
                                        checkedColor   = DeepTeal,
                                        checkmarkColor = MintCream
                                    )
                                )
                                Text(
                                    text     = ingredient.name,
                                    style    = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = if (isChecked) TextDecoration.LineThrough
                                        else TextDecoration.None
                                    ),
                                    color    = if (isChecked)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (ingredient.amount.isNotBlank()) {
                                    Text(
                                        ingredient.amount,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DeepTeal
                                    )
                                }
                            }
                            if (index < ingredients.lastIndex) {
                                HorizontalDivider(
                                    modifier  = Modifier.padding(vertical = 2.dp),
                                    color     = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                        if (checkedIngredients.values.any { it }) {
                            TextButton(
                                onClick  = { checkedIngredients.clear() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Reset", color = DustyRose, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                // ── Sposób wykonania ──────────────────────────────────────────
                if (recipe.executionDescription.isNotBlank()) {
                    DetailSectionCard(title = "Instructions") {
                        Text(
                            recipe.executionDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // ── Dialog timera ─────────────────────────────────────────────────────────
    if (showTimerDialog) {
        TimerSetDialog(
            currentTimer = timerState as? TimerState.Running,
            recipeTitle  = recipe?.title ?: "",
            onStart      = { minutes, seconds ->
                TimerService.startTimer(context, recipeId, recipe?.title ?: "", minutes * 60 + seconds)
                showTimerDialog = false
            },
            onStop    = { TimerService.stopTimer(context, recipeId); showTimerDialog = false },
            onDismiss = { showTimerDialog = false }
        )
    }

    // ── Dialog alarmu ─────────────────────────────────────────────────────────
    if (showAlarmDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("⏰ Timer finished!", style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Text(
                    "${recipe?.title} is ready!",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { TimerService.dismissAlarm(context, recipeId) },
                    colors  = ButtonDefaults.buttonColors(containerColor = DeepTeal, contentColor = MintCream),
                    shape   = RoundedCornerShape(12.dp)
                ) {
                    Text("OK, got it!", style = MaterialTheme.typography.labelLarge)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ── Dialog potwierdzenia usunięcia ────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete recipe", style = MaterialTheme.typography.headlineSmall) },
            text  = {
                Text(
                    "Are you sure you want to delete \"${recipe?.title}\"?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRecipe(recipe!!)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CherryRose,
                        contentColor   = MintCream
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = DeepTeal, style = MaterialTheme.typography.labelLarge)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ── Dialog timera ─────────────────────────────────────────────────────────────

@Composable
private fun TimerSetDialog(
    currentTimer: TimerState.Running?,
    recipeTitle: String,
    onStart: (minutes: Int, seconds: Int) -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit
) {
    var minutes by remember {
        mutableStateOf(if (currentTimer != null) (currentTimer.remainingSec / 60).toString() else "5")
    }
    var seconds by remember {
        mutableStateOf(if (currentTimer != null) (currentTimer.remainingSec % 60).toString() else "0")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set timer", style = MaterialTheme.typography.headlineSmall) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (currentTimer != null) {
                    Text(
                        text  = "Running: ${currentTimer.remainingSec.toTimeString()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepTeal
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                Text(
                    "Set new timer for $recipeTitle:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value           = minutes,
                        onValueChange   = { if (it.length <= 2) minutes = it.filter { c -> c.isDigit() } },
                        label           = { Text("min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.width(80.dp),
                        singleLine      = true,
                        shape           = RoundedCornerShape(12.dp),
                        colors          = recipeTextFieldColors()
                    )
                    Text(":", style = MaterialTheme.typography.headlineMedium, color = DeepTeal)
                    OutlinedTextField(
                        value           = seconds,
                        onValueChange   = { if (it.length <= 2) seconds = it.filter { c -> c.isDigit() } },
                        label           = { Text("sec") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.width(80.dp),
                        singleLine      = true,
                        shape           = RoundedCornerShape(12.dp),
                        colors          = recipeTextFieldColors()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = minutes.toIntOrNull() ?: 0
                    val s = seconds.toIntOrNull() ?: 0
                    if (m > 0 || s > 0) onStart(m, s)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepTeal, contentColor = MintCream),
                shape  = RoundedCornerShape(12.dp)
            ) {
                Text("Start", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            Row {
                if (currentTimer != null) {
                    TextButton(onClick = onStop) {
                        Text("Stop timer", color = CherryRose, style = MaterialTheme.typography.labelLarge)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = DeepTeal, style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ── SectionCard ───────────────────────────────────────────────────────────────

@Composable
private fun DetailSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = DeepTeal
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}