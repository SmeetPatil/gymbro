package com.example.gymbro.ui.workouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gymbro.data.MuscleGroup
import com.example.gymbro.data.Split
import com.example.gymbro.data.Workout
import com.example.gymbro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    viewModel: WorkoutsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("MY SPLIT", "ALL SPLITS", "EXERCISES")

    LaunchedEffect(Unit) {
        viewModel.seedWorkouts()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Brutalist Tab Bar ──────────────────────────────────────
        val borderColor = BrutalistColors.border

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrutalistColors.card),
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = index }
                            .then(
                                if (isSelected)
                                    Modifier.background(BrutalistYellow)
                                else Modifier
                            )
                            .border(
                                width = 1.dp,
                                color = borderColor.copy(alpha = 0.3f),
                            )
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) BrutalistLightText else BrutalistColors.textMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
            // Thick bottom border
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(borderColor)
            )
        }

        when (selectedTab) {
            0 -> MySplitTab(viewModel)
            1 -> AllSplitsTab(viewModel)
            2 -> ExercisesTab(viewModel)
        }
    }
}

// ── My Split Tab ───────────────────────────────────────────────────

@Composable
fun MySplitTab(viewModel: WorkoutsViewModel) {
    val activeSplit by viewModel.activeSplit.collectAsState()

    if (activeSplit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            BrutalistCard(modifier = Modifier.padding(32.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "NO ACTIVE SPLIT",
                        style = MaterialTheme.typography.headlineSmall,
                        color = BrutalistColors.text,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Go to 'ALL SPLITS' to create or select one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrutalistColors.textMuted,
                    )
                }
            }
        }
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            BrutalistCard(modifier = Modifier.fillMaxWidth(), accentColor = BrutalistLime) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "ACTIVE SPLIT",
                        style = MaterialTheme.typography.labelLarge,
                        color = BrutalistLime,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        activeSplit!!.name.uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = BrutalistColors.text,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp))
                    BrutalistDivider()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Your weekly schedule will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrutalistColors.textMuted,
                    )
                }
            }
        }
    }
}

// ── All Splits Tab ─────────────────────────────────────────────────

@Composable
fun AllSplitsTab(viewModel: WorkoutsViewModel) {
    val splits by viewModel.allSplits.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(splits) { split ->
                SplitItem(split = split, onActivate = { viewModel.setActiveSplit(split.id) })
            }
        }

        // Brutalist square FAB
        val fabShadowColor = BrutalistColors.shadow
        val fabShadowOffset = 5.dp
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(end = fabShadowOffset, bottom = fabShadowOffset)
                .size(56.dp)
                .drawBehind {
                    drawRoundRect(
                        color = fabShadowColor,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            fabShadowOffset.toPx(),
                            fabShadowOffset.toPx(),
                        ),
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                    )
                }
                .background(BrutalistYellow, BrutalistShape)
                .border(2.dp, BrutalistColors.border, BrutalistShape)
                .clickable { showCreateDialog = true },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Create Split",
                tint = BrutalistLightText,
                modifier = Modifier.size(28.dp),
            )
        }
    }

    if (showCreateDialog) {
        CreateSplitDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createSplit(name)
                showCreateDialog = false
            },
        )
    }
}

// ── Split Item ─────────────────────────────────────────────────────

@Composable
fun SplitItem(split: Split, onActivate: () -> Unit) {
    BrutalistCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = if (split.isActive) BrutalistLime else null,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                split.name.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = BrutalistColors.text,
                fontWeight = FontWeight.Bold,
            )
            if (split.isActive) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(BrutalistLime, BrutalistShape)
                        .border(2.dp, BrutalistColors.border, BrutalistShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Active",
                        tint = BrutalistLightText,
                        modifier = Modifier.size(20.dp),
                    )
                }
            } else {
                BrutalistButton(
                    onClick = onActivate,
                    color = BrutalistBlue,
                    textColor = White,
                ) {
                    Text("ACTIVATE", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

// ── Create Split Dialog ────────────────────────────────────────────

@Composable
fun CreateSplitDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    BrutalistDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "CREATE NEW SPLIT",
                style = MaterialTheme.typography.labelLarge,
            )
        },
        text = {
            BrutalistTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Split Name") },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            BrutalistButton(
                onClick = { onCreate(name) },
                color = BrutalistLime,
            ) {
                Text("CREATE", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            BrutalistOutlinedButton(onClick = onDismiss) {
                Text("CANCEL", style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}

// ── Exercises Tab ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesTab(viewModel: WorkoutsViewModel) {
    val workouts by viewModel.filteredWorkouts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val splits by viewModel.allSplits.collectAsState()

    var showAddToSplitDialog by remember { mutableStateOf<Workout?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        BrutalistTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("SEARCH EXERCISES...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = BrutalistBlue,
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = BrutalistColors.textMuted,
                        )
                    }
                }
            },
        )

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                BrutalistFilterChip(
                    selected = selectedCategory == null,
                    onClick = { viewModel.onCategorySelected(null) },
                    label = { Text("ALL", style = MaterialTheme.typography.labelMedium) },
                )
            }
            items(MuscleGroup.values()) { group ->
                BrutalistFilterChip(
                    selected = selectedCategory == group,
                    onClick = {
                        viewModel.onCategorySelected(
                            if (selectedCategory == group) null else group
                        )
                    },
                    label = {
                        Text(
                            group.name.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(workouts) { workout ->
                ExerciseItem(
                    workout = workout,
                    onAddClick = { showAddToSplitDialog = workout },
                )
            }
        }
    }

    if (showAddToSplitDialog != null) {
        AddToSplitDialog(
            workout = showAddToSplitDialog!!,
            splits = splits,
            onDismiss = { showAddToSplitDialog = null },
            onConfirm = { splitId, day ->
                viewModel.addWorkoutToSplit(splitId, day, showAddToSplitDialog!!.id)
                showAddToSplitDialog = null
            },
        )
    }
}

// ── Exercise Item ──────────────────────────────────────────────────

@Composable
fun ExerciseItem(workout: Workout, onAddClick: () -> Unit) {
    var showInfoDialog by remember { mutableStateOf(false) }

    BrutalistCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon in thick-bordered circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(2.dp, BrutalistColors.border, CircleShape)
                    .background(BrutalistBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = BrutalistBlue,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    workout.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = BrutalistColors.text,
                    fontWeight = FontWeight.Bold,
                )
                // Muscle group as monospace tag
                Text(
                    workout.muscleGroup.name.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = BrutalistColors.textMuted,
                )
            }
            // Info button
            IconButton(onClick = { showInfoDialog = true }) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Info",
                    tint = BrutalistColors.textMuted
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Add button — thick bordered small box
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(BrutalistLime, BrutalistShape)
                    .border(2.dp, BrutalistColors.border, BrutalistShape)
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add to Split",
                    tint = BrutalistLightText,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }

    if (showInfoDialog) {
        BrutalistDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(workout.name, fontWeight = FontWeight.Bold) },
            text = { Text(workout.description.ifEmpty { "No description available." }) },
            confirmButton = {
                BrutalistButton(onClick = { showInfoDialog = false }) {
                    Text("GOT IT")
                }
            }
        )
    }
}

// ── Add to Split Dialog ────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToSplitDialog(
    workout: Workout,
    splits: List<Split>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    var selectedSplit by remember { mutableStateOf(splits.firstOrNull()) }
    var selectedDay by remember { mutableStateOf(1) }
    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    if (splits.isEmpty()) {
        BrutalistDialog(
            onDismissRequest = onDismiss,
            title = { Text("NO SPLITS FOUND", style = MaterialTheme.typography.labelLarge) },
            text = {
                Text(
                    "Please create a split first in the 'ALL SPLITS' tab.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                BrutalistOutlinedButton(onClick = onDismiss) {
                    Text("OK", style = MaterialTheme.typography.labelLarge)
                }
            },
        )
        return
    }

    BrutalistDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "ADD ${workout.name.uppercase()} TO SPLIT",
                style = MaterialTheme.typography.labelLarge,
            )
        },
        text = {
            Column {
                Text(
                    "SELECT SPLIT:",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrutalistColors.text,
                )
                Spacer(Modifier.height(4.dp))
                splits.forEach { split ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSplit = split }
                            .border(
                                1.dp,
                                if (selectedSplit == split) BrutalistYellow else BrutalistColors.border.copy(alpha = 0.3f),
                                BrutalistShape,
                            )
                            .then(
                                if (selectedSplit == split)
                                    Modifier.background(BrutalistYellow.copy(alpha = 0.15f), BrutalistShape)
                                else Modifier
                            )
                            .padding(8.dp),
                    ) {
                        RadioButton(
                            selected = (selectedSplit == split),
                            onClick = { selectedSplit = split },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = BrutalistYellow,
                                unselectedColor = BrutalistColors.border,
                            ),
                        )
                        Text(
                            split.name.uppercase(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = BrutalistColors.text,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                BrutalistDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "SELECT DAY:",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrutalistColors.text,
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(days.size) { index ->
                        val dayNum = index + 1
                        BrutalistFilterChip(
                            selected = selectedDay == dayNum,
                            onClick = { selectedDay = dayNum },
                            label = {
                                Text(days[index], style = MaterialTheme.typography.labelMedium)
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            BrutalistButton(
                onClick = { selectedSplit?.let { onConfirm(it.id, selectedDay) } },
                color = BrutalistLime,
            ) {
                Text("ADD", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            BrutalistOutlinedButton(onClick = onDismiss) {
                Text("CANCEL", style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}

fun String.capitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
}
