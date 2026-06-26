package com.example.gymbro.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbro.data.Attendance
import com.example.gymbro.data.AttendanceDao
import com.example.gymbro.data.AttendanceStatus
import com.example.gymbro.data.SkipReason
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val attendanceDao: AttendanceDao
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _attendanceMap = MutableStateFlow<Map<LocalDate, Attendance>>(emptyMap())
    val attendanceMap: StateFlow<Map<LocalDate, Attendance>> = _attendanceMap.asStateFlow()

    val attendanceStats: StateFlow<AttendanceStats> = combine(_currentMonth, _attendanceMap) { month, map ->
        val now = LocalDate.now()
        val currentYearMonth = YearMonth.now()
        val isFutureMonth = month.isAfter(currentYearMonth)
        
        val totalDays = when {
            isFutureMonth -> 0
            month == currentYearMonth -> now.dayOfMonth
            else -> month.lengthOfMonth()
        }

        var wentCount = 0
        var skippedCount = 0
        var excusedCount = 0
        var restCount = 0

        // Only count up to `totalDays`
        for (i in 1..totalDays) {
            val date = month.atDay(i)
            val att = map[date]
            when (att?.status) {
                AttendanceStatus.WENT -> wentCount++
                AttendanceStatus.SKIPPED -> skippedCount++
                AttendanceStatus.EXCUSED -> excusedCount++
                AttendanceStatus.REST_DAY -> restCount++
                null -> {}
            }
        }

        AttendanceStats(
            totalDays = totalDays,
            went = wentCount,
            skipped = skippedCount,
            excused = excusedCount,
            restDay = restCount,
            isFutureMonth = isFutureMonth
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, AttendanceStats())

    init {
        loadAttendance()
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
        loadAttendance()
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
        loadAttendance()
    }

    private fun loadAttendance() {
        val start = _currentMonth.value.atDay(1)
        val end = _currentMonth.value.atEndOfMonth()
        viewModelScope.launch {
            attendanceDao.getAttendanceRange(start, end).collectLatest { list ->
                _attendanceMap.value = list.associateBy { it.date }
            }
        }
    }

    fun markAttendance(date: LocalDate, status: AttendanceStatus, reason: SkipReason) {
        viewModelScope.launch {
            attendanceDao.setAttendance(Attendance(date, status, reason))
        }
    }

    fun getDaysInMonth(yearMonth: YearMonth): List<LocalDate?> {
        val days = mutableListOf<LocalDate?>()
        val firstDay = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()
        
        // Day of week: 1 (Mon) to 7 (Sun)
        // Adjust so Monday is first column
        val firstDayOfWeek = firstDay.dayOfWeek.value 
        
        // Add empty slots for days before the 1st
        for (i in 1 until firstDayOfWeek) {
            days.add(null)
        }
        
        for (i in 1..daysInMonth) {
            days.add(yearMonth.atDay(i))
        }
        
        return days
    }
}

data class AttendanceStats(
    val totalDays: Int = 0,
    val went: Int = 0,
    val skipped: Int = 0,
    val excused: Int = 0,
    val restDay: Int = 0,
    val isFutureMonth: Boolean = false
) {
    val totalLogged: Int get() = went + skipped + excused + restDay
    val missing: Int get() = (totalDays - totalLogged).coerceAtLeast(0)
}
