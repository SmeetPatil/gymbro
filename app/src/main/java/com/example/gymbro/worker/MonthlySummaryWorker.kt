package com.example.gymbro.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.gymbro.MainActivity
import com.example.gymbro.data.AttendanceDao
import com.example.gymbro.data.AttendanceStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

@HiltWorker
class MonthlySummaryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val attendanceDao: AttendanceDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val today = LocalDate.now()
            // Safety net: only post the summary on the last calendar day of the month.
            // The scheduler already targets that day, but we guard here in case the OS
            // runs the job earlier or later than requested.
            if (today.dayOfMonth == today.lengthOfMonth()) {
                val currentMonth = YearMonth.now()
                val start = currentMonth.atDay(1)
                val end = currentMonth.atEndOfMonth()

                val attendanceList = attendanceDao.getAttendanceRange(start, end).first()

                val went = attendanceList.count { it.status == AttendanceStatus.WENT }
                val skipped = attendanceList.count { it.status == AttendanceStatus.SKIPPED }
                val excused = attendanceList.count { it.status == AttendanceStatus.EXCUSED }

                sendNotification(went, skipped, excused)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Chain the next month's run so a single missed/late execution never
            // breaks the series.
            schedule(applicationContext)
        }
        return Result.success()
    }

    private fun sendNotification(went: Int, skipped: Int, excused: Int) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "monthly_summary"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Monthly Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Tapping the notification opens the app.
        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Monthly Gym Summary")
            .setContentText("You went $went times, skipped $skipped times, and had $excused excused days.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "MonthlySummary"

        /**
         * Schedules the worker for 23:30 on the last day of the current (or next) month.
         *
         * Uses a self-rescheduling one-time request so it fires on the correct calendar
         * day instead of every 24 hours. Safe to call on every app launch: it replaces any
         * previously scheduled run, which also migrates away from the old (broken) daily
         * periodic schedule.
         */
        fun schedule(context: Context) {
            val delay = computeInitialDelayMillis(LocalDateTime.now())
            val request = OneTimeWorkRequestBuilder<MonthlySummaryWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        /** Milliseconds from [now] until the next "last day of the month at 23:30". */
        internal fun computeInitialDelayMillis(now: LocalDateTime): Long {
            val targetTime = LocalTime.of(23, 30)
            val lastDayThisMonth =
                now.toLocalDate().withDayOfMonth(now.toLocalDate().lengthOfMonth())
            var target = lastDayThisMonth.atTime(targetTime)
            if (!target.isAfter(now)) {
                val nextMonth = now.toLocalDate().plusMonths(1)
                target = nextMonth.withDayOfMonth(nextMonth.lengthOfMonth()).atTime(targetTime)
            }
            return Duration.between(now, target).toMillis()
        }
    }
}
