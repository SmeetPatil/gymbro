package com.example.gymbro.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeSettingsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gymbro_theme_prefs", Context.MODE_PRIVATE)
    
    // Default to Light Mode (false for isDarkTheme)
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun toggleTheme() {
        val newValue = !_isDarkTheme.value
        _isDarkTheme.value = newValue
        prefs.edit().putBoolean("is_dark_theme", newValue).apply()
    }
}
