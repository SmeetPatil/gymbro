package com.example.gymbro.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class UserProfile(
    val isCreated: Boolean = false,
    val name: String = "",
    val age: Int = 0,
    val height: Float = 0f,
    val weight: Float = 0f,
    val weightUnit: String = "kg", // "kg" or "lbs"
    val heightUnit: String = "cm"  // "cm" or "ft"
)

@Singleton
class UserProfileManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gymbro_user_profile", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<UserProfile> = _profile

    private fun loadProfile(): UserProfile {
        return UserProfile(
            isCreated = prefs.getBoolean("is_created", false),
            name = prefs.getString("name", "") ?: "",
            age = prefs.getInt("age", 0),
            height = prefs.getFloat("height", 0f),
            weight = prefs.getFloat("weight", 0f),
            weightUnit = prefs.getString("weight_unit", "kg") ?: "kg",
            heightUnit = prefs.getString("height_unit", "cm") ?: "cm"
        )
    }

    fun saveProfile(profile: UserProfile) {
        prefs.edit().apply {
            putBoolean("is_created", true)
            putString("name", profile.name)
            putInt("age", profile.age)
            putFloat("height", profile.height)
            putFloat("weight", profile.weight)
            putString("weight_unit", profile.weightUnit)
            putString("height_unit", profile.heightUnit)
        }.apply()
        
        _profile.value = profile.copy(isCreated = true)
    }
    
    fun clearProfile() {
        prefs.edit().clear().apply()
        _profile.value = UserProfile()
    }
}
