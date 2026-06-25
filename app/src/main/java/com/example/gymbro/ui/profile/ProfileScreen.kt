package com.example.gymbro.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gymbro.ui.ThemeViewModel
import com.example.gymbro.ui.theme.BrutalistColors
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun ProfileScreen(themeViewModel: ThemeViewModel) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("Profile Screen", style = MaterialTheme.typography.headlineMedium)
    }
}
