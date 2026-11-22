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

    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()
}

@Dao
interface SplitDao {
    @Query("SELECT * FROM splits ORDER BY orderIndex ASC")
    fun getAllSplits(): Flow<List<Split>>

    @Query("SELECT * FROM splits")
    suspend fun getAllSplitsSync(): List<Split>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplits(splits: List<Split>)

    @Query("SELECT * FROM splits WHERE isActive = 1 LIMIT 1")
    fun getActiveSplit(): Flow<Split?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplit(split: Split): Long

    @androidx.room.Update
    suspend fun updateSplits(splits: List<Split>)

    @Query("DELETE FROM splits WHERE id = :id")
    suspend fun deleteSplit(id: Int)

    @Query("DELETE FROM split_days WHERE splitId = :splitId")
    suspend fun deleteSplitDaysForSplit(splitId: Int)

    /** Deletes a split and all of its split_days together so no orphaned rows remain. */
    @Transaction
    suspend fun deleteSplitWithDays(splitId: Int) {
        deleteSplitDaysForSplit(splitId)
        deleteSplit(splitId)
    }

    @Query("DELETE FROM splits")
    suspend fun deleteAllSplits()

    @Query("DELETE FROM split_days")
    suspend fun deleteAllSplitDays()

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
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplitDays(splitDays: List<SplitDay>)

    @Query("SELECT * FROM split_days")
    suspend fun getAllSplitDaysSync(): List<SplitDay>

    @Query("SELECT * FROM split_days WHERE splitId = :splitId AND dayOfWeek = :dayOfWeek")
    suspend fun getWorkoutsForSplitDay(splitId: Int, dayOfWeek: Int): List<SplitDay>
    
    @Transaction
    @Query("SELECT * FROM workouts WHERE id IN (SELECT workoutId FROM split_days WHERE splitId = :splitId AND dayOfWeek = :dayOfWeek)")
    fun getWorkoutsForDay(splitId: Int, dayOfWeek: Int): Flow<List<Workout>>

    @Transaction
    @Query("SELECT * FROM split_days WHERE splitId = :splitId ORDER BY rowid ASC")
    fun getSplitDaysWithWorkouts(splitId: Int): Flow<List<SplitDayWithWorkout>>

    @Query("UPDATE split_days SET sets = :sets, repRange = :repRange WHERE splitId = :splitId AND dayOfWeek = :dayOfWeek AND workoutId = :workoutId")
    suspend fun updateSplitDaySetsAndReps(splitId: Int, dayOfWeek: Int, workoutId: Int, sets: Int, repRange: String)

    @Query("DELETE FROM split_days WHERE splitId = :splitId AND dayOfWeek = :dayOfWeek AND workoutId = :workoutId")
    suspend fun deleteSplitDayWorkout(splitId: Int, dayOfWeek: Int, workoutId: Int)

    @Query("UPDATE split_days SET workoutId = :newId WHERE workoutId = :oldId")
    suspend fun remapWorkoutId(oldId: Int, newId: Int)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendance(date: LocalDate): Flow<Attendance?>

    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendanceSync(): List<Attendance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendances: List<Attendance>)

    @Query("SELECT * FROM attendance WHERE date BETWEEN :startDate AND :endDate")
    fun getAttendanceRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAttendance(attendance: Attendance)

    @Query("DELETE FROM attendance")
    suspend fun deleteAllAttendance()
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs ORDER BY date DESC")
    fun getWeightHistory(): Flow<List<WeightLog>>

    @Query("SELECT * FROM weight_logs")
    suspend fun getAllWeightsSync(): List<WeightLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeights(weights: List<WeightLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logWeight(log: WeightLog)

    @Query("DELETE FROM weight_logs")
    suspend fun deleteAllWeights()
}
