package com.example.gymbro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.gymbro.ui.MainScreen
import com.example.gymbro.ui.theme.GymBroTheme
import com.example.gymbro.worker.MonthlySummaryWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // (Re)schedule the monthly summary notification. Done on every launch so a stale
        // or previously broken schedule is always corrected.
        MonthlySummaryWorker.schedule(this)
        requestNotificationPermissionIfNeeded()

        val themeViewModel: com.example.gymbro.ui.ThemeViewModel by viewModels()

        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            GymBroTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(themeViewModel = themeViewModel)
                }
            }
        }
    }

    // On Android 13+ posting notifications requires a runtime permission; without it the
    // monthly summary is silently dropped. Request it once on launch if not granted.
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
