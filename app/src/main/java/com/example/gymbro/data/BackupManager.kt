package com.example.gymbro.data

import android.content.Context
import android.net.Uri
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

            val backupData = gson.fromJson(json, BackupData::class.java)

            db.runInTransaction {
                db.clearAllTables()
            }

            // Insert new data
            db.workoutDao().insertWorkouts(backupData.workouts)
            db.splitDao().insertSplits(backupData.splits)
            db.splitDao().insertSplitDays(backupData.splitDays)
            db.attendanceDao().insertAttendances(backupData.attendances)
            db.weightDao().insertWeights(backupData.weightLogs)

            // Save user profile
            userProfileManager.saveProfile(backupData.userProfile)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
