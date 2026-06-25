package com.example.gymbro.ui.health

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gymbro.ui.theme.*
import java.time.format.DateTimeFormatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord

@Composable
fun HealthScreen(
    viewModel: HealthViewModel = hiltViewModel()
) {
    val weightHistory by viewModel.weightHistory.collectAsState()
    val steps by viewModel.stepsToday.collectAsState()
    val bmi by viewModel.bmi.collectAsState()
    val permissionsGranted by viewModel.permissionsGranted.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(setOf(HealthPermission.getReadPermission(StepsRecord::class)))) {
            viewModel.checkPermissions()
        }
    }

    var weightInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Title ──────────────────────────────────────────────────
        item {
            Column {
                Text(
                    text = "Health Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrutalistColors.text,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .width(200.dp)
                        .height(4.dp)
                        .padding(bottom = 0.dp)
                        .let { mod ->
                            mod.then(
                                Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(4.dp)
                            )
                        }
                )
                // Yellow accent underline
                Box(
                    Modifier
                        .width(180.dp)
                        .height(4.dp)
                        .padding()
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawRect(BrutalistYellow)
                    }
                }
            }
        }

        // ── Steps Card ─────────────────────────────────────────────
        item {
            BrutalistCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Steps Today",
                                style = MaterialTheme.typography.labelLarge,
                                color = BrutalistColors.text,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "$steps",
                                style = MaterialTheme.typography.displayMedium,
                                color = BrutalistBlue,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "GOAL: 10,000",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrutalistColors.textMuted,
                            )
                        }

                        BrutalistCircularProgress(
                            steps = steps.toInt(),
                            goal = 10000,
                        )
                    }

                    if (!permissionsGranted) {
                        Spacer(modifier = Modifier.height(16.dp))
                        BrutalistDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        BrutalistButton(
                            onClick = {
                                permissionLauncher.launch(
                                    setOf(HealthPermission.getReadPermission(StepsRecord::class))
                                )
                            },
                            color = BrutalistBlue,
                            textColor = White,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                "Connect Health Data",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }

        // ── BMI Calculator ─────────────────────────────────────────
        item {
            BrutalistCard(modifier = Modifier.fillMaxWidth(), accentColor = BrutalistPink) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "BMI Calculator",
                        style = MaterialTheme.typography.labelLarge,
                        color = BrutalistPink,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BrutalistTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                        BrutalistTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    BrutalistButton(
                        onClick = {
                            val w = weightInput.toFloatOrNull()
                            val h = heightInput.toFloatOrNull()
                            if (w != null && h != null) {
                                viewModel.calculateBMI(w, h)
                                viewModel.logWeight(w)
                            }
                        },
                        color = BrutalistPink,
                        textColor = White,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "CALCULATE & LOG WEIGHT",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }

                    if (bmi > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        BrutalistDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "YOUR BMI: ",
                                style = MaterialTheme.typography.titleMedium,
                                color = BrutalistColors.text,
                            )
                            Text(
                                String.format("%.1f", bmi),
                                style = MaterialTheme.typography.headlineSmall,
                                color = BrutalistLime,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            getBMICategory(bmi),
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrutalistColors.textMuted,
                        )
                    }
                }
            }
        }

        // ── Weight History ─────────────────────────────────────────
        item {
            Text(
                "Weight History",
                style = MaterialTheme.typography.labelLarge,
                color = BrutalistColors.text,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(weightHistory) { log ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    log.date.format(DateTimeFormatter.ISO_DATE),
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrutalistColors.text,
                    fontFamily = SpaceMono,
                )
                Text(
                    "${log.weightKg} kg",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrutalistLime,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMono,
                )
            }
            BrutalistDivider()
        }
    }
}

// ── Brutalist Circular Step Progress ───────────────────────────────

@Composable
fun BrutalistCircularProgress(
    steps: Int,
    goal: Int,
    size: Dp = 110.dp,
    strokeWidth: Dp = 12.dp,
) {
    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000),
    )

    val borderColor = BrutalistColors.border
    val trackColor = BrutalistColors.textMuted.copy(alpha = 0.2f)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            // Track — thick, muted
            drawCircle(
                color = trackColor,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt),
            )
            // Border ring
            drawCircle(
                color = borderColor,
                style = Stroke(width = 2.dp.toPx()),
            )
            // Progress arc — thick, lime green, square caps for brutalist feel
            drawArc(
                color = BrutalistLime,
                startAngle = -90f,
                sweepAngle = 360 * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt),
            )
        }
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            color = BrutalistColors.text,
            fontWeight = FontWeight.Bold,
            fontFamily = SpaceMono,
        )
    }
}

fun getBMICategory(bmi: Float): String {
    return when {
        bmi < 18.5 -> "Underweight — Eat more calories!"
        bmi < 25   -> "Normal — Keep it up!"
        bmi < 30   -> "Overweight — Watch your calories."
        else       -> "Obese — Consult a doctor."
    }
}
