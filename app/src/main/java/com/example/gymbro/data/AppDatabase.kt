package com.example.gymbro.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromMuscleGroup(value: String?): MuscleGroup? {
        return value?.let { MuscleGroup.valueOf(it) }
    }

    @TypeConverter
    fun muscleGroupToString(muscleGroup: MuscleGroup?): String? {
        return muscleGroup?.name
    }

    @TypeConverter
    fun fromAttendanceStatus(value: String?): AttendanceStatus? {
        return value?.let { AttendanceStatus.valueOf(it) }
    }

    @TypeConverter
    fun attendanceStatusToString(status: AttendanceStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun fromSkipReason(value: String?): SkipReason? {
        return value?.let { SkipReason.valueOf(it) }
    }

    @TypeConverter
    fun skipReasonToString(reason: SkipReason?): String? {
        return reason?.name
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE workouts ADD COLUMN description TEXT NOT NULL DEFAULT ''")
    }
}

@Database(
    entities = [Workout::class, Split::class, SplitDay::class, Attendance::class, WeightLog::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun splitDao(): SplitDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun weightDao(): WeightDao
}
