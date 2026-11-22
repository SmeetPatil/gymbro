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
        val heightUnit = prefs.getString("height_unit", "cm") ?: "cm"
        var height = prefs.getFloat("height", 0f)

        // One-time migration: older builds stored ft-height as a lossy "feet.inches"
        // decimal (e.g. 5'10" -> "5.10" -> 5.1f). Height is now always centimeters.
        // Decode any legacy ft value with the old convention and convert to cm so it
        // stops colliding/corrupting and so BMI reads it correctly.
        if (prefs.getInt("schema_version", 1) < 2) {
            if (heightUnit == "ft" && height > 0f && height < 13f) {
                val feet = height.toInt()
                val inches = height.toString().substringAfter(".", "0").toIntOrNull() ?: 0
                height = ((feet * 12) + inches) * 2.54f
            }
            prefs.edit().putFloat("height", height).putInt("schema_version", 2).apply()
        }

        return UserProfile(
            isCreated = prefs.getBoolean("is_created", false),
            name = prefs.getString("name", "") ?: "",
            age = prefs.getInt("age", 0),
            height = height,
            weight = prefs.getFloat("weight", 0f),
            weightUnit = prefs.getString("weight_unit", "kg") ?: "kg",
            heightUnit = heightUnit
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
