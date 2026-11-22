package com.example.gymbro.data

data class BackupData(
    val userProfile: UserProfile,
    val workouts: List<Workout>,
    val splits: List<Split>,
    val splitDays: List<SplitDay>,
    val attendances: List<Attendance>,
    val weightLogs: List<WeightLog>,
    val workoutLogs: List<WorkoutLog> = emptyList()
)
