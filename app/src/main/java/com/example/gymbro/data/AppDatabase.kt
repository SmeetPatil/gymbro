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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE split_days ADD COLUMN sets INTEGER")
        database.execSQL("ALTER TABLE split_days ADD COLUMN repRange TEXT")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE splits ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0")
    }
}

// Data-only cleanup: remove split_days rows orphaned by past split/workout
// deletions (no schema change, so the Room schema hash is unaffected).
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "DELETE FROM split_days WHERE splitId NOT IN (SELECT id FROM splits) " +
                "OR workoutId NOT IN (SELECT id FROM workouts)"
        )
    }
}

@Database(
    entities = [Workout::class, Split::class, SplitDay::class, Attendance::class, WeightLog::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun splitDao(): SplitDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun weightDao(): WeightDao
}
