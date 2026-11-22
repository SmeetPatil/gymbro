package com.example.gymbro.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val db: AppDatabase,
    private val userProfileManager: UserProfileManager,
    @ApplicationContext private val context: Context
) {
    private val gson: Gson = GsonBuilder().create()

    suspend fun exportData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val workouts = db.workoutDao().getAllWorkoutsSync()
            val splits = db.splitDao().getAllSplitsSync()
            val splitDays = db.splitDao().getAllSplitDaysSync()
            val attendances = db.attendanceDao().getAllAttendanceSync()
            val weightLogs = db.weightDao().getAllWeightsSync()
            val profile = userProfileManager.profile.value

            val backupData = BackupData(
                userProfile = profile,
                workouts = workouts,
                splits = splits,
                splitDays = splitDays,
                attendances = attendances,
                weightLogs = weightLogs
            )

            val json = gson.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
            } ?: return@withContext Result.failure(Exception("Could not open output stream"))

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun importData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure(Exception("Could not open input stream"))

            // Parse and validate BEFORE touching the database, so a malformed/empty
            // file can never wipe the user's existing data.
            val backupData: BackupData? = gson.fromJson(json, BackupData::class.java)
            if (backupData == null) {
                return@withContext Result.failure(Exception("Backup file is empty or invalid"))
            }

            // Atomic replace: clear + re-insert inside ONE transaction. If any step
            // fails (including a null field from a corrupt backup) the whole thing
            // rolls back, leaving the current data intact instead of wiped/partial.
            db.withTransaction {
                db.splitDao().deleteAllSplitDays()
                db.splitDao().deleteAllSplits()
                db.workoutDao().deleteAllWorkouts()
                db.attendanceDao().deleteAllAttendance()
                db.weightDao().deleteAllWeights()

                db.workoutDao().insertWorkouts(backupData.workouts)
                db.splitDao().insertSplits(backupData.splits)
                db.splitDao().insertSplitDays(backupData.splitDays)
                db.attendanceDao().insertAttendances(backupData.attendances)
                db.weightDao().insertWeights(backupData.weightLogs)
            }

            // User profile lives in SharedPreferences, not Room — apply it only
            // after the database transaction has successfully committed.
            userProfileManager.saveProfile(backupData.userProfile)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
