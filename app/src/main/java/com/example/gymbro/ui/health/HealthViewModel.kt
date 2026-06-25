package com.example.gymbro.ui.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbro.data.WeightDao
import com.example.gymbro.data.WeightLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val weightDao: WeightDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val weightHistory = weightDao.getWeightHistory().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _stepsToday = MutableStateFlow(0L)
    val stepsToday = _stepsToday.asStateFlow()

    private val _bmi = MutableStateFlow(0f)
    val bmi = _bmi.asStateFlow()

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted = _permissionsGranted.asStateFlow()

    fun logWeight(weight: Float) {
        viewModelScope.launch {
            weightDao.logWeight(WeightLog(LocalDate.now(), weight))
        }
    }

    fun calculateBMI(weight: Float, heightCm: Float) {
        if (heightCm > 0) {
            val heightM = heightCm / 100
            _bmi.value = weight / (heightM * heightM)
        }
    }

    fun checkPermissions() {
        viewModelScope.launch {
            if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
                val healthConnectClient = HealthConnectClient.getOrCreate(context)
                val permissions = setOf(
                    HealthPermission.getReadPermission(StepsRecord::class)
                )
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                _permissionsGranted.value = granted.containsAll(permissions)
                if (_permissionsGranted.value) {
                    fetchSteps()
                }
            }
        }
    }

    fun fetchSteps() {
        viewModelScope.launch {
            if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
                val healthConnectClient = HealthConnectClient.getOrCreate(context)
                val permissions = setOf(
                    HealthPermission.getReadPermission(StepsRecord::class)
                )
                
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                if (granted.containsAll(permissions)) {
                    val startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
                    val endTime = Instant.now()
                    
                    val response = healthConnectClient.readRecords(
                        ReadRecordsRequest(
                            recordType = StepsRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                        )
                    )
                    
                    val totalSteps = response.records.sumOf { it.count }
                    _stepsToday.value = totalSteps
                }
            }
        }
    }
}
