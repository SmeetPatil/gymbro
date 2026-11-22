package com.example.gymbro.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbro.data.Attendance
import com.example.gymbro.data.AttendanceDao
import com.example.gymbro.data.AttendanceStatus
import com.example.gymbro.data.SkipReason
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

    // Month + its loaded attendance, kept together so downstream state is always a
    // consistent pair (never a new month combined with the previous month's map).
    private data class MonthData(val month: YearMonth, val map: Map<LocalDate, Attendance>)

    // Single reactive pipeline keyed on the selected month. flatMapLatest cancels the
    // previous month's query when the month changes, so there is never more than one
    // live collector, and the emitted value carries the month it belongs to.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val monthData: StateFlow<MonthData> = _currentMonth
        .flatMapLatest { month ->
            attendanceDao.getAttendanceRange(month.atDay(1), month.atEndOfMonth())
                .map { list -> MonthData(month, list.associateBy { it.date }) }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, MonthData(YearMonth.now(), emptyMap()))

    val attendanceMap: StateFlow<Map<LocalDate, Attendance>> = monthData
        .map { it.map }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    // Derived from the SAME monthData emission, so stats always use the map that
    // matches the month (fixes the transient count-drop on navigation).
    val attendanceStats: StateFlow<AttendanceStats> = monthData
        .map { data -> computeStats(data.month, data.map) }
        .stateIn(viewModelScope, SharingStarted.Lazily, AttendanceStats())

    private fun computeStats(month: YearMonth, map: Map<LocalDate, Attendance>): AttendanceStats {
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
            when (map[month.atDay(i)]?.status) {
                AttendanceStatus.WENT -> wentCount++
                AttendanceStatus.SKIPPED -> skippedCount++
                AttendanceStatus.EXCUSED -> excusedCount++
                AttendanceStatus.REST_DAY -> restCount++
                null -> {}
            }
        }

        return AttendanceStats(
            totalDays = totalDays,
            went = wentCount,
            skipped = skippedCount,
            excused = excusedCount,
            restDay = restCount,
            isFutureMonth = isFutureMonth
        )
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
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

        // Day of week: 1 (Mon) to 7 (Sun). Adjust so Monday is the first column.
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
