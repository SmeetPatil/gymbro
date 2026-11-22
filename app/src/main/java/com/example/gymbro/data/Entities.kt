package com.example.gymbro.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class MuscleGroup {
    CHEST, BACK, ABS, LEGS, BICEPS, TRICEPS, SHOULDERS, CARDIO
}

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val muscleGroup: MuscleGroup,
    val silhouetteIconName: String, // Resource name or path
    val description: String = ""
)

@Entity(tableName = "splits")
data class Split(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isActive: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "split_days", primaryKeys = ["splitId", "dayOfWeek", "workoutId"])
data class SplitDay(
    val splitId: Int,
    val dayOfWeek: Int, // 1 = Monday, 7 = Sunday
    val workoutId: Int,
    val sets: Int? = null,
    val repRange: String? = null
)

data class SplitDayWithWorkout(
    @androidx.room.Embedded val splitDay: SplitDay,
    @androidx.room.Relation(
        parentColumn = "workoutId",
        entityColumn = "id"
    )
    val workout: Workout
)

enum class AttendanceStatus {
    WENT, // Green
    SKIPPED, // Red
    EXCUSED, // Purple
    REST_DAY // Grey
}

enum class SkipReason {
    NONE,
    ILLNESS,
    EXAM,
    FUNCTION,
    TRAVELLING,
    OTHER
}

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey val date: LocalDate,
    val status: AttendanceStatus,
    val reason: SkipReason = SkipReason.NONE
)

@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey val date: LocalDate,
    val weightKg: Float
)

@Entity(
    tableName = "workout_logs",
    primaryKeys = ["date", "splitId", "workoutId", "setNumber"]
)
data class WorkoutLog(
    val date: LocalDate,
    val splitId: Int,
    val workoutId: Int,
    val setNumber: Int,     // 1-based set number
    val reps: Int,          // actual reps performed
    val weightKg: Float     // weight lifted in kg (0 for bodyweight)
)

data class WorkoutLogWithDetails(
    @androidx.room.Embedded val log: WorkoutLog,
    @androidx.room.Relation(
        parentColumn = "workoutId",
        entityColumn = "id"
    )
    val workout: Workout
)
