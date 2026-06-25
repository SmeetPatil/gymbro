package com.example.gymbro.ui

import androidx.lifecycle.ViewModel
import com.example.gymbro.data.ThemeSettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeSettingsManager: ThemeSettingsManager
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = themeSettingsManager.isDarkTheme

    fun toggleTheme() {
        themeSettingsManager.toggleTheme()
    }
}
