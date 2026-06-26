package com.example.gymbro.di

import android.content.Context
import androidx.room.Room
import com.example.gymbro.data.AppDatabase
import com.example.gymbro.data.AttendanceDao
import com.example.gymbro.data.SplitDao
import com.example.gymbro.data.WeightDao
import com.example.gymbro.data.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gymbro_db"
        )
        .addMigrations(
            com.example.gymbro.data.MIGRATION_1_2, 
            com.example.gymbro.data.MIGRATION_2_3,
            com.example.gymbro.data.MIGRATION_3_4
        )
        .build()
    }

    @Provides
    fun provideWorkoutDao(db: AppDatabase): WorkoutDao = db.workoutDao()

    @Provides
    fun provideSplitDao(db: AppDatabase): SplitDao = db.splitDao()

    @Provides
    fun provideAttendanceDao(db: AppDatabase): AttendanceDao = db.attendanceDao()

    @Provides
    fun provideWeightDao(db: AppDatabase): WeightDao = db.weightDao()
}
