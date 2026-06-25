package com.example.gymbro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gymbro.ui.MainScreen
import com.example.gymbro.ui.theme.GymBroTheme
import com.example.gymbro.worker.MonthlySummaryWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleMonthlyWorker()
        setContent {
            GymBroTheme { // follows system dark/light
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    private fun scheduleMonthlyWorker() {
        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(23, 30)
        var nextRun = now.with(targetTime)
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1)
        }
        
        val initialDelay = Duration.between(now, nextRun).toMillis()

        val workRequest = PeriodicWorkRequestBuilder<MonthlySummaryWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MonthlySummary",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
