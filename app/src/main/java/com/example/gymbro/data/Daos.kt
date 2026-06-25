package com.example.gymbro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts")
    suspend fun getAllWorkoutsSync(): List<Workout>

    @Query("SELECT * FROM workouts WHERE muscleGroup = :group")
    fun getWorkoutsByMuscle(group: MuscleGroup): Flow<List<Workout>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<Workout>)

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkout(id: Int)
}

@Dao
interface SplitDao {
    @Query("SELECT * FROM splits")
    fun getAllSplits(): Flow<List<Split>>

    @Query("SELECT * FROM splits WHERE isActive = 1 LIMIT 1")
    fun getActiveSplit(): Flow<Split?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplit(split: Split): Long

    @Query("UPDATE splits SET isActive = 0")
    suspend fun deactivateAllSplits()

    @Transaction
    suspend fun setActiveSplit(splitId: Int) {
        deactivateAllSplits()
        setSplitActive(splitId)
    }

    @Query("UPDATE splits SET isActive = 1 WHERE id = :splitId")
    suspend fun setSplitActive(splitId: Int)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplitDay(splitDay: SplitDay)

    @Query("SELECT * FROM split_days WHERE splitId = :splitId AND dayOfWeek = :dayOfWeek")
    suspend fun getWorkoutsForSplitDay(splitId: Int, dayOfWeek: Int): List<SplitDay>
    
    @Transaction
    @Query("SELECT * FROM workouts WHERE id IN (SELECT workoutId FROM split_days WHERE splitId = :splitId AND dayOfWeek = :dayOfWeek)")
    fun getWorkoutsForDay(splitId: Int, dayOfWeek: Int): Flow<List<Workout>>

    @Query("UPDATE split_days SET workoutId = :newId WHERE workoutId = :oldId")
    suspend fun remapWorkoutId(oldId: Int, newId: Int)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendance(date: LocalDate): Flow<Attendance?>

    @Query("SELECT * FROM attendance WHERE date BETWEEN :startDate AND :endDate")
    fun getAttendanceRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAttendance(attendance: Attendance)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs ORDER BY date DESC")
    fun getWeightHistory(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logWeight(log: WeightLog)
}
