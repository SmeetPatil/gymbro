package com.example.gymbro.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbro.data.MuscleGroup
import com.example.gymbro.data.SplitDao
import com.example.gymbro.data.UserProfile
import com.example.gymbro.data.UserProfileManager
import com.example.gymbro.data.BackupManager
import android.net.Uri
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileManager: UserProfileManager,
    private val splitDao: SplitDao,
    private val backupManager: BackupManager
) : ViewModel() {

    val profile: StateFlow<UserProfile> = userProfileManager.profile

    @OptIn(ExperimentalCoroutinesApi::class)
    val weeklyMuscleVolume: StateFlow<Map<MuscleGroup, Int>> = splitDao.getActiveSplit()
        .flatMapLatest { split ->
            if (split != null) {
                splitDao.getSplitDaysWithWorkouts(split.id).map { splitDays ->
                    val volumeMap = mutableMapOf<MuscleGroup, Int>()
                    splitDays.forEach { item ->
                        val sets = item.splitDay.sets ?: 0
                        val group = item.workout.muscleGroup
                        volumeMap[group] = volumeMap.getOrDefault(group, 0) + sets
                    }
                    volumeMap
                }
            } else {
                flowOf(emptyMap())
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun saveProfile(userProfile: UserProfile) {
        userProfileManager.saveProfile(userProfile)
    }

    fun exportData(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.exportData(uri)
            onResult(result.isSuccess)
        }
    }

    fun importData(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.importData(uri)
            onResult(result.isSuccess)
        }
    }
}
