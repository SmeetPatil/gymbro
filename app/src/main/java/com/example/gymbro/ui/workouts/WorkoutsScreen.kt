package com.example.gymbro.ui.workouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gymbro.data.MuscleGroup
import com.example.gymbro.data.Split
import com.example.gymbro.data.Workout
import com.example.gymbro.ui.theme.*

import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.CloudSync

import androidx.compose.material.icons.filled.Menu
import org.burnoutcrew.reorderable.*

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

private fun shareSplit(
    context: android.content.Context,
    split: com.example.gymbro.data.Split, 
    splitDaysWithWorkouts: List<com.example.gymbro.data.SplitDayWithWorkout>,
    viewModel: com.example.gymbro.ui.workouts.WorkoutsViewModel
) {
    val width = 1080
    var height = 250
    
    val daysMap = splitDaysWithWorkouts.groupBy { it.splitDay.dayOfWeek }
    val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    
    val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 40f
    }
    val paintTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#CDDC39") // Lime color
        textSize = 70f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val paintSubTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#B0B0B0")
        textSize = 45f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    
    val lines = mutableListOf<Pair<String, Paint>>()
    lines.add(Pair(split.name.uppercase(), paintTitle))
    lines.add(Pair("---------------------------------------", paintText))
    
    daysMap.entries.sortedBy { it.key }.forEach { (dayIndex, dayItems) ->
        val dayName = dayNames.getOrElse(dayIndex - 1) { "Day $dayIndex" }
        val workouts = dayItems.map { it.workout }
        val dayTitle = viewModel.getDayTitle(workouts)
        
        val titleStr = if (dayTitle.isNotEmpty()) " > $dayTitle" else ""
        lines.add(Pair("$dayName$titleStr", paintSubTitle))
        
        dayItems.forEach { item ->
            val sets = item.splitDay.sets?.let { "$it sets" } ?: "0 sets"
            val reps = item.splitDay.repRange?.let { "$it rep range" } ?: "No rep range"
            lines.add(Pair("- ${item.workout.name}  | $sets | $reps", paintText))
        }
        lines.add(Pair("", paintText))
    }
    
    height += lines.size * 65
    
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.parseColor("#1C1C1E")) // Dark background
    
    var yPos = 120f
    for ((text, paint) in lines) {
        canvas.drawText(text, 60f, yPos, paint)
        yPos += paint.textSize + 25f
    }
    
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "shared_split.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Split via"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    viewModel: WorkoutsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active Split", "All Splits", "Exercises")

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
fun MySplitTab(
    viewModel: WorkoutsViewModel,
    healthViewModel: com.example.gymbro.ui.health.HealthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val activeSplit by viewModel.activeSplit.collectAsState()
    val splitDaysWithWorkouts by viewModel.activeSplitDaysWithWorkouts.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var selectedWorkoutForDialog by remember { mutableStateOf<com.example.gymbro.data.SplitDayWithWorkout?>(null) }

    if (activeSplit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            BrutalistCard(modifier = Modifier.padding(32.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "No Active Split",
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                BrutalistCard(modifier = Modifier.fillMaxWidth(), accentColor = BrutalistLime) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Active Split",
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
                            }
                            Row {
                                IconButton(onClick = { healthViewModel.syncWorkout(activeSplit!!.name, 60L) }) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = "Sync to Google Fit",
                                        tint = BrutalistColors.text
                                    )
                                }
                                IconButton(onClick = { shareSplit(context, activeSplit!!, splitDaysWithWorkouts, viewModel) }) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Split",
                                        tint = BrutalistColors.text
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        BrutalistDivider()
                        Spacer(Modifier.height(16.dp))

                        val daysMap = splitDaysWithWorkouts.groupBy { it.splitDay.dayOfWeek }
                        
                        if (daysMap.isEmpty()) {
                            Text(
                                "No exercises added to this split yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BrutalistColors.textMuted,
                            )
                        } else {
                            val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                            daysMap.entries.sortedBy { it.key }.forEach { (dayIndex, splitDays) ->
                                val dayName = dayNames.getOrElse(dayIndex - 1) { "Day $dayIndex" }
                                val workouts = splitDays.map { it.workout }
                                val dayTitle = viewModel.getDayTitle(workouts)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        dayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = BrutalistColors.text
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    if (dayTitle.isNotEmpty()) {
                                        val titleShadow = BrutalistColors.shadow
                                        Box(
                                            modifier = Modifier
                                                .drawBehind {
                                                    drawRoundRect(
                                                        color = titleShadow,
                                                        topLeft = androidx.compose.ui.geometry.Offset(2.dp.toPx(), 2.dp.toPx()),
                                                        size = size,
                                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                                                    )
                                                }
                                                .background(Color(0xFFFF9800), BrutalistShape)
                                                .border(2.dp, BrutalistColors.border, BrutalistShape)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                dayTitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = androidx.compose.ui.graphics.Color.Black
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                // Row of icons
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(splitDays) { splitDayWithWorkout ->
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clickable { selectedWorkoutForDialog = splitDayWithWorkout }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .align(Alignment.TopStart)
                                                    .border(2.dp, BrutalistColors.border, CircleShape)
                                                    .background(BrutalistBlue.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                MuscleGroupIcon(
                                                    muscleGroup = splitDayWithWorkout.workout.muscleGroup,
                                                    tint = BrutalistBlue,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            if (splitDayWithWorkout.splitDay.sets != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .background(BrutalistYellow, CircleShape)
                                                        .border(2.dp, BrutalistColors.border, CircleShape)
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        "x${splitDayWithWorkout.splitDay.sets}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = androidx.compose.ui.graphics.Color.Black,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    selectedWorkoutForDialog?.let { splitDay ->
        ExerciseConfigurationDialog(
            splitDay = splitDay,
            onDismiss = { selectedWorkoutForDialog = null },
            onRemove = { 
                viewModel.removeWorkoutFromSplitDay(splitDay.splitDay.splitId, splitDay.splitDay.dayOfWeek, splitDay.workout.id)
                selectedWorkoutForDialog = null
            },
            onSet = { sets, repRange ->
                viewModel.updateSplitDaySetsAndReps(splitDay.splitDay.splitId, splitDay.splitDay.dayOfWeek, splitDay.workout.id, sets, repRange)
                selectedWorkoutForDialog = null
            }
        )
    }
}

// ── All Splits Tab ─────────────────────────────────────────────────

@Composable
fun AllSplitsTab(viewModel: WorkoutsViewModel) {
    val splits by viewModel.allSplits.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var splitToDelete by remember { mutableStateOf<Split?>(null) }
    
    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            val mutableSplits = splits.toMutableList()
            val moved = mutableSplits.removeAt(from.index)
            mutableSplits.add(to.index, moved)
            // Update orderIndex
            val updatedSplits = mutableSplits.mapIndexed { index, split ->
                split.copy(orderIndex = index)
            }
            viewModel.updateSplitOrder(updatedSplits)
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(state),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(splits, { it.id }) { split ->
                ReorderableItem(state, key = split.id) { isDragging ->
                    SplitItem(
                        split = split, 
                        isDragging = isDragging,
                        onActivate = { viewModel.setActiveSplit(split.id) },
                        onDelete = { splitToDelete = split },
                        dragModifier = Modifier.detectReorder(state)
                    )
                }
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

    if (splitToDelete != null) {
        BrutalistDialog(
            onDismissRequest = { splitToDelete = null },
            title = { Text("Delete Split", style = MaterialTheme.typography.labelLarge) },
            text = { Text("Are you sure you want to delete '${splitToDelete?.name}'? This action cannot be undone.", style = MaterialTheme.typography.bodyMedium, color = BrutalistColors.textMuted) },
            confirmButton = {
                BrutalistButton(onClick = {
                    splitToDelete?.id?.let { viewModel.deleteSplit(it) }
                    splitToDelete = null
                }, color = BrutalistRed) {
                    Text("Delete", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { splitToDelete = null }) {
                    Text("Cancel", color = BrutalistColors.text)
                }
            }
        )
    }
}

// ── Split Item ─────────────────────────────────────────────────────

@Composable
fun SplitItem(
    split: Split, 
    isDragging: Boolean = false, 
    onActivate: () -> Unit, 
    onDelete: () -> Unit,
    dragModifier: Modifier = Modifier
) {
    val elevation = if (isDragging) 8.dp else 0.dp
    
    BrutalistCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onActivate() },
                    onLongPress = { onDelete() }
                )
            },
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
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Text("Activate", style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Drag to reorder",
                    tint = BrutalistColors.textMuted,
                    modifier = Modifier.then(dragModifier)
                )
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
                "Create New Split",
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
                Text("Create", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            BrutalistOutlinedButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
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
                    label = { Text("All", style = MaterialTheme.typography.labelMedium) },
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
            onConfirm = { splitId, days ->
                days.forEach { day ->
                    viewModel.addWorkoutToSplit(splitId, day, showAddToSplitDialog!!.id)
                }
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
                MuscleGroupIcon(
                    muscleGroup = workout.muscleGroup,
                    tint = BrutalistBlue,
                    modifier = Modifier.size(24.dp)
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
                    Text("Got It")
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
    onConfirm: (Int, Set<Int>) -> Unit,
) {
    var selectedSplit by remember { mutableStateOf(splits.firstOrNull()) }
    val selectedDays = remember { mutableStateListOf<Int>() }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    if (splits.isEmpty()) {
        BrutalistDialog(
            onDismissRequest = onDismiss,
            title = { Text("No Splits Found", style = MaterialTheme.typography.labelLarge) },
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
                    "SELECT DAYS:",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrutalistColors.text,
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(days.size) { index ->
                        val dayNum = index + 1
                        val isSelected = selectedDays.contains(dayNum)
                        BrutalistFilterChip(
                            selected = isSelected,
                            onClick = { 
                                if (isSelected) selectedDays.remove(dayNum) 
                                else selectedDays.add(dayNum) 
                            },
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
                onClick = { 
                    selectedSplit?.let { split ->
                        if (selectedDays.isNotEmpty()) {
                            onConfirm(split.id, selectedDays.toSet()) 
                        }
                    }
                },
                enabled = selectedSplit != null && selectedDays.isNotEmpty(),
                color = BrutalistLime,
            ) {
                Text("ADD", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            BrutalistOutlinedButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}

fun String.capitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
}

@Composable
fun MuscleGroupIcon(muscleGroup: MuscleGroup, tint: Color, modifier: Modifier = Modifier) {
    when (muscleGroup) {
        MuscleGroup.CHEST -> {
            Canvas(modifier = modifier) {
                val stroke = 3.dp.toPx()
                val squareSize = size.width * 0.45f
                val yOffset = (size.height - squareSize) / 2
                // Left square
                drawRect(
                    color = tint, 
                    topLeft = Offset(size.width/2 - squareSize, yOffset), 
                    size = Size(squareSize, squareSize), 
                    style = Stroke(stroke)
                )
                // Right square
                drawRect(
                    color = tint, 
                    topLeft = Offset(size.width/2, yOffset), 
                    size = Size(squareSize, squareSize), 
                    style = Stroke(stroke)
                )
            }
        }
        MuscleGroup.BACK -> {
            Canvas(modifier = modifier) {
                val path = Path().apply {
                    val w = size.width
                    val h = size.height
                    val topWidth = w * 0.8f
                    val bottomWidth = w * 0.4f
                    val height = h * 0.7f
                    val xCenter = w / 2
                    val yTop = (h - height) / 2
                    val yBottom = yTop + height
                    
                    moveTo(xCenter - topWidth/2, yTop)
                    lineTo(xCenter + topWidth/2, yTop)
                    lineTo(xCenter + bottomWidth/2, yBottom)
                    lineTo(xCenter - bottomWidth/2, yBottom)
                    close()
                }
                drawPath(path, color = tint, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                // Draw line of symmetry down to 3/4 of height
                drawLine(
                    color = tint, 
                    start = Offset(size.width/2, size.height * 0.15f), 
                    end = Offset(size.width/2, size.height * 0.15f + size.height * 0.7f * 0.75f), 
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
        MuscleGroup.LEGS -> {
            Canvas(modifier = modifier) {
                val stroke = 3.dp.toPx()
                val h = size.height * 0.55f
                val w = size.width * 0.25f
                val yTop = size.height * 0.25f
                
                // Left L (tilted more forward)
                withTransform({
                    rotate(25f, pivot = Offset(size.width * 0.2f, yTop + h/2))
                }) {
                    val leftX = size.width * 0.2f
                    drawLine(tint, Offset(leftX, yTop), Offset(leftX, yTop + h), strokeWidth = stroke, cap = StrokeCap.Round)
                    drawLine(tint, Offset(leftX, yTop + h), Offset(leftX + w, yTop + h), strokeWidth = stroke, cap = StrokeCap.Round)
                }
                
                // Right L (tilted less forward, with space in between)
                withTransform({
                    rotate(10f, pivot = Offset(size.width * 0.7f, yTop + h/2))
                }) {
                    val rightX = size.width * 0.7f
                    drawLine(tint, Offset(rightX, yTop), Offset(rightX, yTop + h), strokeWidth = stroke, cap = StrokeCap.Round)
                    drawLine(tint, Offset(rightX, yTop + h), Offset(rightX + w, yTop + h), strokeWidth = stroke, cap = StrokeCap.Round)
                }
            }
        }
        MuscleGroup.SHOULDERS -> {
            Canvas(modifier = modifier) {
                val stroke = 3.dp.toPx()
                val yTop = size.height * 0.35f
                val yBottom = size.height * 0.75f
                
                // Left shoulder (inverted L) - widened gap
                drawLine(tint, start = Offset(size.width * 0.15f, yBottom), end = Offset(size.width * 0.15f, yTop), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(tint, start = Offset(size.width * 0.15f, yTop), end = Offset(size.width * 0.35f, yTop), strokeWidth = stroke, cap = StrokeCap.Round)
                
                // Right shoulder (laterally inverted L) - widened gap
                drawLine(tint, start = Offset(size.width * 0.85f, yBottom), end = Offset(size.width * 0.85f, yTop), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(tint, start = Offset(size.width * 0.85f, yTop), end = Offset(size.width * 0.65f, yTop), strokeWidth = stroke, cap = StrokeCap.Round)
            }
        }
        else -> {
            val icon = when (muscleGroup) {
                MuscleGroup.BICEPS, MuscleGroup.TRICEPS -> Icons.Default.FitnessCenter
                MuscleGroup.ABS -> Icons.Default.ViewCompact
                MuscleGroup.CARDIO -> Icons.Default.DirectionsRun
                else -> Icons.Default.Info // fallback
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = modifier,
                tint = tint,
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ExerciseConfigurationDialog(
    splitDay: com.example.gymbro.data.SplitDayWithWorkout,
    onDismiss: () -> Unit,
    onRemove: () -> Unit,
    onSet: (sets: Int, repRange: String) -> Unit
) {
    var setsInput by remember { mutableStateOf(splitDay.splitDay.sets?.toString() ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var selectedRepRange by remember { mutableStateOf(splitDay.splitDay.repRange ?: "1-5") }
    val repOptions = listOf("1-5", "5-10", "10-15", "15-20", "20-25", "25-30", "30-50", "50+")

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        BrutalistCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    splitDay.workout.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = BrutalistColors.text,
                    fontWeight = FontWeight.Bold,
                )
                if (splitDay.workout.description.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        splitDay.workout.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = BrutalistColors.textMuted,
                        maxLines = 4,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(16.dp))
                
                // Sets input
                Text("Sets", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = setsInput,
                    onValueChange = { setsInput = it.filter { char -> char.isDigit() } },
                    textStyle = androidx.compose.ui.text.TextStyle(color = BrutalistColors.text, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, BrutalistColors.border, BrutalistShape)
                        .background(BrutalistColors.background, BrutalistShape)
                        .padding(12.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Rep Range dropdown
                Text("Rep Range", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                androidx.compose.material3.ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    androidx.compose.material3.TextField(
                        readOnly = true,
                        value = selectedRepRange,
                        onValueChange = {},
                        trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = androidx.compose.material3.ExposedDropdownMenuDefaults.textFieldColors(
                            focusedContainerColor = BrutalistColors.background,
                            unfocusedContainerColor = BrutalistColors.background,
                            focusedTextColor = BrutalistColors.text,
                            unfocusedTextColor = BrutalistColors.text,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .border(2.dp, BrutalistColors.border, BrutalistShape)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(BrutalistColors.background).border(2.dp, BrutalistColors.border)
                    ) {
                        repOptions.forEach { option ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(option, color = BrutalistColors.text) },
                                onClick = {
                                    selectedRepRange = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dialogBtnPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                    
                    BrutalistButton(
                        onClick = onRemove, 
                        color = BrutalistRed, 
                        contentPadding = dialogBtnPadding,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("REMOVE", color = BrutalistLightText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    BrutalistOutlinedButton(
                        onClick = onDismiss, 
                        contentPadding = dialogBtnPadding,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CLOSE", color = BrutalistColors.text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    BrutalistButton(
                        onClick = { 
                            val sets = setsInput.toIntOrNull()
                            if (sets != null) {
                                onSet(sets, selectedRepRange)
                            }
                        }, 
                        color = BrutalistLime,
                        enabled = setsInput.isNotBlank(),
                        contentPadding = dialogBtnPadding,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SET", color = BrutalistColors.text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }
}
