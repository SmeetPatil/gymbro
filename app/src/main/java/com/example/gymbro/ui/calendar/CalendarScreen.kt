package com.example.gymbro.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
        LazyVerticalGrid(columns = GridCells.Fixed(7)) {
            items(days) { date ->
                if (date != null) {
                    val attendance = attendanceMap[date]
                    DayCell(
                        date = date,
                        attendance = attendance,
                        onClick = {
                            selectedDate = date
                            showDialog = true
                        },
                    )
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Legend ─────────────────────────────────────────────────
        BrutalistCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Legend",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrutalistColors.text,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    LegendItem(BrutalistLime, "Gym")
                    LegendItem(BrutalistRed, "Skip")
                    LegendItem(BrutalistPurple, "Excuse")
                    LegendItem(BrutalistBlue, "Rest")
                }
            }
        }
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
        AttendanceStatus.WENT, AttendanceStatus.SKIPPED, AttendanceStatus.EXCUSED ->
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
