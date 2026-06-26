package com.example.gymbro.ui.calendar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gymbro.data.Attendance
import com.example.gymbro.data.AttendanceStatus
import com.example.gymbro.data.SkipReason
import com.example.gymbro.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val attendanceMap by viewModel.attendanceMap.collectAsState()
    val attendanceStats by viewModel.attendanceStats.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Month Header ───────────────────────────────────────────
        BrutalistCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrutalistNavArrow("<") { viewModel.previousMonth() }
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()} ${currentMonth.year}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrutalistColors.text,
                    fontWeight = FontWeight.Bold,
                )
                BrutalistNavArrow(">") { viewModel.nextMonth() }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Days of Week Header ────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = BrutalistColors.textMuted,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Calendar Grid ──────────────────────────────────────────
        val days = viewModel.getDaysInMonth(currentMonth)
        Column(modifier = Modifier.fillMaxWidth()) {
            days.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        Box(modifier = Modifier.weight(1f)) {
                            if (date != null) {
                                val attendance = attendanceMap[date]
                                DayCell(
                                    date = date,
                                    attendance = attendance,
                                    onClick = {
                                        selectedDate = date
                                        showDialog = true
                                    }
                                )
                            } else {
                                Spacer(modifier = Modifier.padding(4.dp).aspectRatio(1f))
                            }
                        }
                    }
                    // Fill remaining empty slots if the last week is incomplete
                    for (i in 0 until (7 - week.size)) {
                        Spacer(modifier = Modifier.weight(1f).padding(4.dp).aspectRatio(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Legend ─────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            BrutalistCard(modifier = Modifier.fillMaxWidth(0.9f)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(BrutalistLime, "Gym")
                    LegendItem(BrutalistRed, "Skip")
                    LegendItem(BrutalistPurple, "Excuse")
                    LegendItem(BrutalistBlue, "Rest")
                }
            }
        }

        AttendancePieChart(stats = attendanceStats, currentMonth = currentMonth)
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showDialog && selectedDate != null) {
        AttendanceDialog(
            date = selectedDate!!,
            onDismiss = { showDialog = false },
            onConfirm = { status, reason ->
                viewModel.markAttendance(selectedDate!!, status, reason)
                showDialog = false
            },
        )
    }
}

// ── Day Cell ───────────────────────────────────────────────────────

@Composable
fun DayCell(
    date: LocalDate,
    attendance: Attendance?,
    onClick: () -> Unit,
) {
    val fillColor = when (attendance?.status) {
        AttendanceStatus.WENT     -> BrutalistLime
        AttendanceStatus.SKIPPED  -> BrutalistRed
        AttendanceStatus.EXCUSED  -> BrutalistPurple
        AttendanceStatus.REST_DAY -> BrutalistBlue
        null -> BrutalistColors.card
    }

    val textColor = when (attendance?.status) {
        AttendanceStatus.WENT, AttendanceStatus.SKIPPED, AttendanceStatus.EXCUSED, AttendanceStatus.REST_DAY ->
            BrutalistLightText
        else -> BrutalistColors.text
    }

    val borderColor = BrutalistColors.border
    val isToday = date == LocalDate.now()

    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .brutalistShadow(offsetX = 3.dp, offsetY = 3.dp)
            .background(fillColor, RoundedCornerShape(4.dp))
            .border(
                width = if (isToday) 3.dp else 1.dp,
                color = if (isToday) BrutalistYellow else borderColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp),
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

// ── Nav Arrow Button ───────────────────────────────────────────────

@Composable
private fun BrutalistNavArrow(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(2.dp, BrutalistColors.border, BrutalistShape)
            .background(BrutalistYellow, BrutalistShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleLarge,
            color = BrutalistLightText,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Legend Item ─────────────────────────────────────────────────────

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, RoundedCornerShape(2.dp))
                .border(1.dp, BrutalistColors.border, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = BrutalistColors.text,
        )
    }
}

// ── Attendance Dialog ──────────────────────────────────────────────

@Composable
fun AttendanceDialog(
    date: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (AttendanceStatus, SkipReason) -> Unit,
) {
    var selectedStatus by remember { mutableStateOf<AttendanceStatus?>(null) }

    BrutalistDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Attendance · $date",
                style = MaterialTheme.typography.labelLarge,
            )
        },
        text = {
            Column {
                Text(
                    "Did you hit the gym?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrutalistColors.text,
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Status Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BrutalistButton(
                        onClick = { onConfirm(AttendanceStatus.WENT, SkipReason.NONE) },
                        color = BrutalistLime,
                        modifier = Modifier.weight(1f),
                    ) { Text("Yes", style = MaterialTheme.typography.labelLarge) }

                    BrutalistButton(
                        onClick = { selectedStatus = AttendanceStatus.SKIPPED },
                        color = BrutalistRed,
                        textColor = White,
                        modifier = Modifier.weight(1f),
                    ) { Text("No", style = MaterialTheme.typography.labelLarge) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                BrutalistButton(
                    onClick = { onConfirm(AttendanceStatus.REST_DAY, SkipReason.NONE) },
                    color = BrutalistBlue,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Rest Day", style = MaterialTheme.typography.labelLarge) }

                if (selectedStatus == AttendanceStatus.SKIPPED) {
                    Spacer(modifier = Modifier.height(16.dp))
                    BrutalistDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Reason for skipping:",
                        style = MaterialTheme.typography.labelLarge,
                        color = BrutalistColors.text,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SkipReason.values().filter { it != SkipReason.NONE }.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val finalStatus = if (isValidExcuse(reason)) AttendanceStatus.EXCUSED else AttendanceStatus.SKIPPED
                                    onConfirm(finalStatus, reason)
                                }
                                .border(
                                    1.dp,
                                    BrutalistColors.border.copy(alpha = 0.3f),
                                    BrutalistShape,
                                )
                                .padding(10.dp),
                        ) {
                            RadioButton(
                                selected = false,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = BrutalistYellow,
                                    unselectedColor = BrutalistColors.border,
                                ),
                            )
                            Text(
                                text = reason.name.lowercase().capitalize(),
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = BrutalistColors.text,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            BrutalistOutlinedButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}

fun isValidExcuse(reason: SkipReason): Boolean {
    return when (reason) {
        SkipReason.ILLNESS, SkipReason.EXAM, SkipReason.FUNCTION, SkipReason.TRAVELLING -> true
        else -> false
    }
}

fun String.capitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

// ── Attendance Graph ───────────────────────────────────────────────

@Composable
fun AttendancePieChart(stats: AttendanceStats, currentMonth: YearMonth) {
    if (stats.isFutureMonth) return

    val progress = remember { Animatable(0f) }
    LaunchedEffect(currentMonth) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }
    
    val total = stats.totalDays.toFloat()
    if (total == 0f) return

    val gymAngle = (stats.went / total) * 360f
    val skippedAngle = (stats.skipped / total) * 360f
    val excusedAngle = (stats.excused / total) * 360f
    val restAngle = (stats.restDay / total) * 360f
    val missingAngle = (stats.missing / total) * 360f
    
    val missingColor = BrutalistColors.card
    val borderColor = BrutalistColors.border
    val cardColor = BrutalistColors.card
    val textColor = BrutalistColors.text
    val textMutedColor = BrutalistColors.textMuted
    
    // Drawing angles
    val angles = listOf(
        gymAngle to BrutalistLime,
        skippedAngle to BrutalistRed,
        excusedAngle to BrutalistPurple,
        restAngle to BrutalistBlue,
        missingAngle to missingColor
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 32.dp.toPx()
                val radius = size.minDimension / 2 - strokeWidth / 2
                
                var startAngle = -90f
                
                // Draw background circle first
                drawCircle(
                    color = cardColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                    radius = radius
                )
                // Draw border for the entire ring
                drawCircle(
                    color = borderColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth + 4.dp.toPx()),
                    radius = radius
                )

                for ((sweep, color) in angles) {
                    if (sweep > 0f) {
                        val animatedSweep = sweep * progress.value
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = animatedSweep,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                        )
                        startAngle += sweep // important: advance startAngle by the full sweep
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${((stats.totalLogged / total) * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "Logged",
                    style = MaterialTheme.typography.labelMedium,
                    color = textMutedColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        AttendanceQuips(stats, textColor)
    }
}

@Composable
fun AttendanceQuips(stats: AttendanceStats, textColor: androidx.compose.ui.graphics.Color) {
    val total = stats.totalDays.toFloat()
    if (total == 0f) return

    val gymPct = stats.went / total
    val skippedPct = stats.skipped / total
    val restPct = stats.restDay / total
    val missingPct = stats.missing / total

    val text = when {
        missingPct > 0.5f -> "You haven't logged much for this month. Start tracking!"
        skippedPct > 0.3f -> "Don't let the skipped days pile up! Let's get back on track."
        gymPct > 0.7f -> "Unstoppable! You're crushing it."
        gymPct > 0.4f -> "Consistency is key. Keep showing up!"
        restPct > 0.3f -> "Lots of rest. Make sure you're balancing it with action!"
        else -> "Keep at it! Every day counts."
    }

    BrutalistCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💡", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
