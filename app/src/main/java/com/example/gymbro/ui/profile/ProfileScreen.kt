package com.example.gymbro.ui.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.gymbro.data.MuscleGroup
import com.example.gymbro.data.UserProfile
import com.example.gymbro.ui.ThemeViewModel
import com.example.gymbro.ui.theme.*

@Composable
fun ProfileScreen(
    themeViewModel: ThemeViewModel,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val weeklyMuscleVolume by viewModel.weeklyMuscleVolume.collectAsState()

    var showForm by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.exportData(it) { success ->
                val msg = if (success) "Data exported successfully" else "Export failed"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importData(it) { success ->
                val msg = if (success) "Data imported successfully" else "Import failed"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrutalistColors.text,
                    fontWeight = FontWeight.Bold,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { themeViewModel.toggleTheme() },
                        thumbContent = {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                tint = BrutalistColors.background
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BrutalistColors.text,
                            checkedTrackColor = BrutalistColors.card,
                            uncheckedThumbColor = BrutalistColors.text,
                            uncheckedTrackColor = BrutalistColors.background,
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box {
                        IconButton(onClick = { showSettingsMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = BrutalistColors.text
                            )
                        }
                        DropdownMenu(
                            expanded = showSettingsMenu,
                            onDismissRequest = { showSettingsMenu = false },
                            modifier = Modifier.background(BrutalistColors.card).border(2.dp, BrutalistColors.border)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export Data", color = BrutalistColors.text) },
                                onClick = {
                                    showSettingsMenu = false
                                    exportLauncher.launch("gymbro_backup.json")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Import Data", color = BrutalistColors.text) },
                                onClick = {
                                    showSettingsMenu = false
                                    importLauncher.launch(arrayOf("application/json"))
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            BrutalistDivider()
            Spacer(Modifier.height(16.dp))
        }

        if (!profile.isCreated || showForm) {
            item {
                ProfileForm(
                    initialProfile = profile,
                    onSave = { updatedProfile ->
                        viewModel.saveProfile(updatedProfile)
                        showForm = false
                    },
                    onCancel = {
                        if (profile.isCreated) showForm = false
                    }
                )
            }
        } else {
            item {
                ProfileDashboard(
                    profile = profile,
                    weeklyMuscleVolume = weeklyMuscleVolume,
                    onEditClick = { showForm = true }
                )
            }
        }
    }
}

@Composable
fun ProfileForm(
    initialProfile: UserProfile,
    onSave: (UserProfile) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialProfile.name) }
    var age by remember { mutableStateOf(if (initialProfile.age > 0) initialProfile.age.toString() else "") }
    var height by remember { mutableStateOf(if (initialProfile.height > 0) initialProfile.height.toString() else "") }
    var weight by remember { mutableStateOf(if (initialProfile.weight > 0) initialProfile.weight.toString() else "") }
    var weightUnit by remember { mutableStateOf(initialProfile.weightUnit) }
    var heightUnit by remember { mutableStateOf(initialProfile.heightUnit) }

    BrutalistCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Set up Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BrutalistColors.text)
            Spacer(modifier = Modifier.height(16.dp))

            BrutalistTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            BrutalistTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (heightUnit == "cm") {
                    BrutalistTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    val ftOptions = (4..8).map { it.toString() }
                    val inOptions = (0..11).map { it.toString() }
                    var feet by remember(height) { mutableStateOf(if (height.contains(".")) height.substringBefore(".") else "5") }
                    var inches by remember(height) { mutableStateOf(if (height.contains(".")) height.substringAfter(".") else "0") }

                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BrutalistDropdownMenu(
                            value = feet,
                            options = ftOptions,
                            onValueChange = { feet = it; height = "$it.$inches" },
                            label = "Ft",
                            modifier = Modifier.weight(1f)
                        )
                        BrutalistDropdownMenu(
                            value = inches,
                            options = inOptions,
                            onValueChange = { inches = it; height = "$feet.$it" },
                            label = "In",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                BrutalistButton(
                    onClick = { 
                        if (heightUnit == "cm") {
                            heightUnit = "ft"
                            val cm = height.toFloatOrNull() ?: 170f
                            val totalInches = cm / 2.54f
                            val f = (totalInches / 12).toInt()
                            val i = (totalInches % 12).toInt()
                            height = "$f.$i"
                        } else {
                            heightUnit = "cm"
                            val f = height.substringBefore(".").toIntOrNull() ?: 5
                            val i = height.substringAfter(".").toIntOrNull() ?: 0
                            val totalInches = (f * 12) + i
                            val cm = totalInches * 2.54f
                            height = cm.toInt().toString()
                        }
                    },
                    color = BrutalistYellow,
                    textColor = Color.Black,
                    modifier = Modifier.weight(0.5f).align(Alignment.Bottom)
                ) {
                    Text(heightUnit.uppercase(), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BrutalistTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                BrutalistButton(
                    onClick = { weightUnit = if (weightUnit == "kg") "lbs" else "kg" },
                    color = BrutalistPink,
                    textColor = Color.White,
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text(weightUnit.uppercase(), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (initialProfile.isCreated) {
                    BrutalistButton(
                        onClick = onCancel,
                        color = BrutalistColors.card,
                        textColor = BrutalistColors.text,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL")
                    }
                }
                BrutalistButton(
                    onClick = {
                        val parsedAge = age.toIntOrNull() ?: 0
                        val parsedHeight = height.toFloatOrNull() ?: 0f
                        val parsedWeight = weight.toFloatOrNull() ?: 0f
                        onSave(
                            UserProfile(
                                isCreated = true,
                                name = name,
                                age = parsedAge,
                                height = parsedHeight,
                                weight = parsedWeight,
                                weightUnit = weightUnit,
                                heightUnit = heightUnit
                            )
                        )
                    },
                    color = BrutalistBlue,
                    textColor = Color.White,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("SAVE")
                }
            }
        }
    }
}

@Composable
fun ProfileDashboard(
    profile: UserProfile,
    weeklyMuscleVolume: Map<MuscleGroup, Int>,
    onEditClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // User Info Card
        BrutalistCard(modifier = Modifier.fillMaxWidth(), accentColor = BrutalistBlue) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.name.ifEmpty { "User" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrutalistColors.text
                    )
                    TextButton(onClick = onEditClick) {
                        Text("EDIT", color = BrutalistBlue, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ProfileStatItem(label = "AGE", value = "${profile.age}")
                    ProfileStatItem(label = "HEIGHT", value = "${profile.height} ${profile.heightUnit}")
                    ProfileStatItem(label = "WEIGHT", value = "${profile.weight} ${profile.weightUnit}")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Weekly Volume Tracking",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BrutalistColors.text
        )
        Text(
            "Goal: 12 - 20 sets per muscle group",
            style = MaterialTheme.typography.bodySmall,
            color = BrutalistColors.textMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bars Grid
        val muscleGroups = listOf(
            MuscleGroup.BACK, MuscleGroup.CHEST, MuscleGroup.LEGS,
            MuscleGroup.SHOULDERS, MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.ABS
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            muscleGroups.chunked(2).forEach { rowGroups ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowGroups.forEach { group ->
                        val sets = weeklyMuscleVolume[group] ?: 0
                        val color = when (group) {
                            MuscleGroup.CHEST -> BrutalistBlue
                            MuscleGroup.BACK -> BrutalistPink
                            MuscleGroup.LEGS -> BrutalistLime
                            MuscleGroup.SHOULDERS -> BrutalistYellow
                            MuscleGroup.BICEPS -> Color(0xFF9C27B0) // Purple
                            MuscleGroup.TRICEPS -> Color(0xFFFF5722) // Deep Orange
                            MuscleGroup.ABS -> Color(0xFF00BCD4) // Cyan
                            else -> BrutalistColors.text
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            MuscleVolumeProgress(
                                muscleGroup = group.name,
                                sets = sets,
                                color = color
                            )
                        }
                    }
                    if (rowGroups.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = BrutalistColors.textMuted)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrutalistColors.text)
    }
}

@Composable
fun MuscleVolumeProgress(
    muscleGroup: String,
    sets: Int,
    color: Color,
    size: Dp = 100.dp,
    strokeWidth: Dp = 12.dp
) {
    val goalMax = 20f
    val goalMin = 12f
    val progress = (sets.toFloat() / goalMax).coerceIn(0f, 1f)
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000)
    )

    val borderColor = BrutalistColors.border
    val trackColor = BrutalistColors.textMuted.copy(alpha = 0.2f)
    val indicatorColor = if (sets >= goalMin) color else color.copy(alpha = 0.6f) // Dim if under goal
    val textColor = BrutalistColors.text

    BrutalistCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = muscleGroup,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    )
                    
                    // Border ring
                    drawCircle(
                        color = borderColor,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    )
                    
                    // Progress arc
                    drawArc(
                        color = indicatorColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    )
                    
                    // Mark at 12 sets (60%)
                    val sweep12 = 360f * (goalMin / goalMax)
                    val markAngle = -90f + sweep12
                    val radius = (size.toPx() - strokeWidth.toPx()) / 2f
                    val angleRad = Math.toRadians(markAngle.toDouble())
                    val centerX = center.x
                    val centerY = center.y
                    
                    // Draw a small line indicating the minimum sweet spot
                    drawLine(
                        color = textColor,
                        start = Offset(
                            x = centerX + (radius - 10f) * kotlin.math.cos(angleRad).toFloat(),
                            y = centerY + (radius - 10f) * kotlin.math.sin(angleRad).toFloat()
                        ),
                        end = Offset(
                            x = centerX + (radius + 10f) * kotlin.math.cos(angleRad).toFloat(),
                            y = centerY + (radius + 10f) * kotlin.math.sin(angleRad).toFloat()
                        ),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$sets",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrutalistColors.text
                    )
                    Text(
                        text = "sets",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrutalistColors.textMuted
                    )
                }
            }
        }
    }
}

// ── Dropdown Component ─────────────────────────────────────────────

@Composable
fun BrutalistDropdownMenu(
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = BrutalistColors.text)
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .brutalistShadow()
                    .background(BrutalistColors.card, BrutalistShape)
                    .border(2.dp, BrutalistColors.border, BrutalistShape)
                    .clickable { expanded = true }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = value, color = BrutalistColors.text, style = MaterialTheme.typography.bodyLarge)
                Text(text = "▼", color = BrutalistColors.text, fontSize = 12.sp)
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(BrutalistColors.card).border(2.dp, BrutalistColors.border)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = BrutalistColors.text) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
