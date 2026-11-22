package com.example.gymbro.ui.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbro.data.WeightDao
import com.example.gymbro.data.WeightLog
import com.example.gymbro.data.UserProfileManager
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
    private val userProfileManager: UserProfileManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val weightHistory = weightDao.getWeightHistory().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _stepsToday = MutableStateFlow(0L)
    val stepsToday = _stepsToday.asStateFlow()

    val bmi: StateFlow<Float> = userProfileManager.profile.map { profile ->
        if (profile.height > 0f && profile.weight > 0f) {
            val weightInKg = if (profile.weightUnit == "lbs") profile.weight * 0.453592f else profile.weight
            // Height is stored canonically in centimeters regardless of display unit.
            val heightInM = profile.height / 100f
            weightInKg / (heightInM * heightInM)
        } else {
            0f
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted = _permissionsGranted.asStateFlow()

    private val _sdkStatus = MutableStateFlow(HealthConnectClient.SDK_UNAVAILABLE)
    val sdkStatus = _sdkStatus.asStateFlow()
    
    private val _caloriesToday = MutableStateFlow(0L)
    val caloriesToday = _caloriesToday.asStateFlow()

    private val _distanceToday = MutableStateFlow(0.0)
    val distanceToday = _distanceToday.asStateFlow()
    
    private val _heartRate = MutableStateFlow(0L)
    val heartRate = _heartRate.asStateFlow()

    private val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class)
    )

    init {
        _sdkStatus.value = HealthConnectClient.getSdkStatus(context)
    }

    fun checkPermissions() {
        viewModelScope.launch {
            _sdkStatus.value = HealthConnectClient.getSdkStatus(context)
            if (_sdkStatus.value == HealthConnectClient.SDK_AVAILABLE) {
                val healthConnectClient = HealthConnectClient.getOrCreate(context)
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                _permissionsGranted.value = granted.isNotEmpty()
                if (_permissionsGranted.value) {
                    fetchData()
                }
            }
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
                val healthConnectClient = HealthConnectClient.getOrCreate(context)
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                if (granted.isNotEmpty()) {
                    val startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
                    val endTime = Instant.now()
                    
                    if (granted.contains(HealthPermission.getReadPermission(StepsRecord::class))) {
                        // Use the aggregation API instead of summing raw records.
                        // When several providers (e.g. Google Fit and the device's own
                        // step provider) write overlapping StepsRecords, summing them
                        // double-counts; aggregate() de-duplicates overlapping data by
                        // data-origin priority so each step is counted once.
                        val stepsResponse = healthConnectClient.aggregate(
                            AggregateRequest(
                                metrics = setOf(StepsRecord.COUNT_TOTAL),
                                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                            )
                        )
                        _stepsToday.value = stepsResponse[StepsRecord.COUNT_TOTAL] ?: 0L
                    }
                    
                    if (granted.contains(HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class))) {
                        // Aggregated to avoid double-counting overlapping data from
                        // multiple providers (same reasoning as steps above).
                        val calsResponse = healthConnectClient.aggregate(
                            AggregateRequest(
                                metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                            )
                        )
                        _caloriesToday.value =
                            (calsResponse[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0).toLong()
                    }
                    
                    if (granted.contains(HealthPermission.getReadPermission(DistanceRecord::class))) {
                        // Aggregated to avoid double-counting overlapping data from
                        // multiple providers (same reasoning as steps above).
                        val distResponse = healthConnectClient.aggregate(
                            AggregateRequest(
                                metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                            )
                        )
                        _distanceToday.value = distResponse[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0
                    }
                    
                    if (granted.contains(HealthPermission.getReadPermission(HeartRateRecord::class))) {
                        val hrResponse = healthConnectClient.readRecords(
                            ReadRecordsRequest(
                                recordType = HeartRateRecord::class,
                                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                            )
                        )
                        val allHrSamples = hrResponse.records.flatMap { it.samples }
                        if (allHrSamples.isNotEmpty()) {
                            _heartRate.value = allHrSamples.last().beatsPerMinute
                        }
                    }
                }
            }
        }
    }
    
    fun logWeight(weightKg: Float) {
        viewModelScope.launch {
            weightDao.logWeight(WeightLog(LocalDate.now(), weightKg))
        }
    }

    fun syncWorkout(name: String, durationMinutes: Long) {
        viewModelScope.launch {
            if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
                val healthConnectClient = HealthConnectClient.getOrCreate(context)
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                if (granted.contains(HealthPermission.getWritePermission(ExerciseSessionRecord::class)) &&
                    granted.contains(HealthPermission.getWritePermission(ActiveCaloriesBurnedRecord::class))) {
                    val endTime = Instant.now()
                    val startTime = endTime.minusSeconds(durationMinutes * 60)
                    
                    val session = ExerciseSessionRecord(
                        startTime = startTime,
                        startZoneOffset = ZoneId.systemDefault().rules.getOffset(startTime),
                        endTime = endTime,
                        endZoneOffset = ZoneId.systemDefault().rules.getOffset(endTime),
                        exerciseType = androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
                        title = name
                    )
                    
                    // Estimate 300 kcal per hour
                    val calories = ActiveCaloriesBurnedRecord(
                        startTime = startTime,
                        startZoneOffset = ZoneId.systemDefault().rules.getOffset(startTime),
                        endTime = endTime,
                        endZoneOffset = ZoneId.systemDefault().rules.getOffset(endTime),
                        energy = androidx.health.connect.client.units.Energy.kilocalories((durationMinutes / 60.0) * 300.0)
                    )
                    
                    try {
                        healthConnectClient.insertRecords(listOf(session, calories))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
