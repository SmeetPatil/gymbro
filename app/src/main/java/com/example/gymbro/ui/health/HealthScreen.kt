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
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gymbro.ui.theme.*
import com.example.gymbro.R
import android.widget.Toast
import java.time.format.DateTimeFormatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.HealthConnectClient
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.Icons

@Composable
fun HealthScreen(
    viewModel: HealthViewModel = hiltViewModel()
) {
    val weightHistory by viewModel.weightHistory.collectAsState()
    val steps by viewModel.stepsToday.collectAsState()
    val bmi by viewModel.bmi.collectAsState()
    val permissionsGranted by viewModel.permissionsGranted.collectAsState()

    val calories by viewModel.caloriesToday.collectAsState()
    val distance by viewModel.distanceToday.collectAsState()
    val heartRate by viewModel.heartRate.collectAsState()
    val sdkStatus by viewModel.sdkStatus.collectAsState()
    val context = LocalContext.current

    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class)
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        viewModel.checkPermissions()
    }

    // Inputs removed, BMI now uses UserProfileManager

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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Health Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        color = BrutalistColors.text,
                        fontWeight = FontWeight.Bold,
                    )
                    if (permissionsGranted) {
                        IconButton(onClick = { viewModel.fetchData() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Data", tint = BrutalistColors.text)
                        }
                    }
                }
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

                    
                    if (permissionsGranted) {
                        Spacer(modifier = Modifier.height(24.dp))
                        BrutalistDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            MetricItem(icon = Icons.Default.LocalFireDepartment, label = "Calories", value = "$calories kcal")
                            MetricItem(icon = Icons.Default.DirectionsWalk, label = "Distance", value = "${String.format("%.1f", distance / 1000.0)} km")
                            MetricItem(icon = Icons.Default.Favorite, label = "Heart Rate", value = if (heartRate > 0) "$heartRate bpm" else "--")
                        }
                    }

                    if (!permissionsGranted) {
                        Spacer(modifier = Modifier.height(16.dp))
                        BrutalistDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (sdkStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED || sdkStatus == HealthConnectClient.SDK_UNAVAILABLE) {
                            BrutalistButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.healthdata"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                color = BrutalistPink,
                                textColor = White,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Install Health Connect", style = MaterialTheme.typography.labelLarge)
                            }
                        } else {
                            BrutalistButton(
                                onClick = {
                                    try {
                                        Toast.makeText(context, "Launching Google Fit...", Toast.LENGTH_SHORT).show()
                                        permissionLauncher.launch(requiredPermissions)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        e.printStackTrace()
                                    }
                                },
                                color = BrutalistBlue,
                                textColor = White,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painter = painterResource(id = R.drawable.ic_google_fit), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Connect Google Fit",
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    try {
                                        val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open settings: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Open Settings Manually", color = BrutalistBlue, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }

        // ── BMI Chart ─────────────────────────────────────────
        item {
            BrutalistCard(modifier = Modifier.fillMaxWidth(), accentColor = BrutalistPink) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Body Mass Index",
                        style = MaterialTheme.typography.labelLarge,
                        color = BrutalistPink,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (bmi > 0) {
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
                    } else {
                        Text(
                            "Set up your profile to see your BMI.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrutalistColors.textMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = label, tint = BrutalistColors.textMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = BrutalistColors.text, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = BrutalistColors.textMuted)
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
